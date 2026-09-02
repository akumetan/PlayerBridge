package io.github.akumetan.playerbridge.profile.data;

import io.github.akumetan.playerbridge.config.PlayerDataConfig;
import io.github.akumetan.playerbridge.profile.ownership.OwnershipResult;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnership;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;

import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

public final class PlayerDataService {

    private final PlayerDataRepository repository;
    private final PlayerOwnershipService ownershipService;
    private final PlayerDataConfig config;
    private final String serverId;

    public PlayerDataService(PlayerDataRepository repository, PlayerOwnershipService service, PlayerDataConfig config, String serverId) {
        this.repository = repository;
        this.ownershipService = service;
        this.config = config;
        this.serverId = serverId;
    }

    public LoadResult load(UUID uuid, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            OwnershipResult result = this.ownershipService.acquire(uuid);

            if (result instanceof OwnershipResult.Acquired) {
                try {
                    PlayerData data = this.repository.load(uuid);
                    return LoadResult.success(data);

                } catch (SQLException | RuntimeException exception) {
                    this.ownershipService.releaseLocalOnly(uuid);
                    return LoadResult.failure();
                }
            }

            if (result instanceof OwnershipResult.Failed)
                return LoadResult.failure();

            if (timeout.isZero())
                return LoadResult.busy();

            long remaining = deadline - System.nanoTime();
            if (remaining <= 0)
                return LoadResult.busy();


            long retryMillis = Math.max(1L, config.retryInterval().toMillis());
            long remainingMillis = Math.max(1L, remaining / 1_000_000L);
            long sleepMillis = Math.min(retryMillis, remainingMillis);

            try {
                Thread.sleep(sleepMillis); // Ƶƶ(￣▵—▵￣)
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return LoadResult.failure();
            }
        }
    }

    public boolean save(PlayerData data) {
        PlayerOwnership ownership = ownershipService.current(data.uuid());
        if (ownership == null)
            return false;

        try {
            boolean saved = repository.save(data, serverId, ownership.token());
            if (!saved)
                ownershipService.releaseLocalOnly(data.uuid());

            return saved;
        } catch (SQLException exception) {
            return false;
        }
    }

    public boolean saveAndRelease(UUID uuid, PlayerData data) {
        PlayerOwnership ownership = ownershipService.current(uuid);
        if (ownership == null)
            return false;

        long deadline = System.nanoTime() + config.finalSaveTimeout().toNanos();
        while (true) {
            try {
                boolean saved = repository.saveAndRelease(data, serverId, ownership.token());
                ownershipService.releaseLocalOnly(uuid);
                return saved;

            } catch (SQLException exception) {
                long remaining = deadline - System.nanoTime();

                if (remaining <= 0) {
                    ownershipService.releaseLocalOnly(uuid);
                    return false;
                }

                long retryMillis = Math.max(1L, config.retryInterval().toMillis());
                long remainingMillis = Math.max(1L, remaining / 1_000_000L);
                long sleepMillis = Math.min(retryMillis, remainingMillis);

                try {
                    Thread.sleep(sleepMillis); // Ƶƶ(￣▵—▵￣)
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    ownershipService.releaseLocalOnly(uuid);
                    return false;
                }
            }
        }
    }
}