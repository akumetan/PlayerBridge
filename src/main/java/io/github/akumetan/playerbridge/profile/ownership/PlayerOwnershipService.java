package io.github.akumetan.playerbridge.profile.ownership;

import io.github.akumetan.playerbridge.config.PlayerBridgeConfig;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class PlayerOwnershipService {

    private final PlayerOwnershipRepository repository;
    private final PlayerBridgeConfig config;

    private final ConcurrentHashMap<UUID, PlayerOwnership> ownerships = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    public PlayerOwnershipService(PlayerOwnershipRepository repository, PlayerBridgeConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public OwnershipResult acquire(UUID uuid) {
        ReentrantLock local = this.lockLocallyFor(uuid);
        local.lock();

        try {
            PlayerOwnership existing = this.ownerships.get(uuid);
            if (existing != null)
                return new OwnershipResult.Acquired(existing);

            String token = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plus(this.config.ownership().leaseDuration());

            try {
                if (!this.repository.tryAcquire(uuid, this.config.serverId(), token, expiresAt)) {
                    PlayerOwnership current = this.repository.find(uuid);
                    if (current == null)
                        return new OwnershipResult.Failed();

                    return new OwnershipResult.Unavailable(current.serverId());
                }
                PlayerOwnership ownership = new PlayerOwnership(uuid, this.config.serverId(), token, expiresAt);
                this.ownerships.put(uuid, ownership);

                return new OwnershipResult.Acquired(ownership);

            } catch (SQLException exception) {
                return new OwnershipResult.Failed();
            }

        } finally {
            local.unlock();
        }
    }

    public boolean renew(UUID uuid) {
        ReentrantLock local = this.lockLocallyFor(uuid);
        local.lock();

        try {
            PlayerOwnership ownership = this.ownerships.get(uuid);
            if (ownership == null)
                return false;

            Instant newExpiresAt = Instant.now().plus(config.ownership().leaseDuration());
            try {
                if (!this.repository.renew(uuid, this.config.serverId(), ownership.token(), newExpiresAt)) {
                    this.ownerships.remove(uuid, ownership);
                    return false;
                }
                this.ownerships.replace(uuid, ownership, new PlayerOwnership(uuid, this.config.serverId(), ownership.token(), newExpiresAt));
                return true;

            } catch (SQLException exception) {
                return false;
            }

        } finally {
            local.unlock();
        }
    }

    public boolean owns(UUID uuid) {
        return this.ownerships.containsKey(uuid);
    }

    public PlayerOwnership current(UUID uuid) {
        return this.ownerships.get(uuid);
    }

    public List<PlayerOwnership> currentOwnerships() {
        return new ArrayList<>(ownerships.values());
    }

    public void renewAll() {
        for (PlayerOwnership ownership : this.currentOwnerships())
            this.renew(ownership.uuid());
    }

    public boolean release(UUID uuid) {
        ReentrantLock local = this.lockLocallyFor(uuid);
        local.lock();

        try {
            PlayerOwnership ownership = this.ownerships.get(uuid);
            if (ownership == null)
                return false;

            try {
                boolean released = this.repository.release(uuid, this.config.serverId(), ownership.token());
                if (released)
                    this.ownerships.remove(uuid, ownership);

                return released;

            } catch (SQLException exception) {
                return false;
            }

        } finally {
            local.unlock();
        }
    }

    public void releaseAll() {
        for (PlayerOwnership ownership : this.currentOwnerships())
            this.release(ownership.uuid());
    }

    public void releaseLocalOnly(UUID uuid) {
        ReentrantLock local = this.lockLocallyFor(uuid);
        local.lock();

        try {
            this.ownerships.remove(uuid);
        } finally {
            local.unlock();
        }
    }

    private ReentrantLock lockLocallyFor(UUID uuid) {
        return this.localLocks.computeIfAbsent(uuid, ignored -> new ReentrantLock());
    }

}