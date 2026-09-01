package io.github.akumetan.playerbridge.profile.ownership;

import io.github.akumetan.playerbridge.config.OwnershipConfig;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class PlayerOwnershipService {

    private final PlayerOwnershipRepository repository;
    private final OwnershipConfig config;
    private final String serverId;

    public PlayerOwnershipService(PlayerOwnershipRepository repository, OwnershipConfig config, String serverId) {
        this.repository = repository;
        this.config = config;
        this.serverId = serverId;
    }

    public OwnershipResult acquire(UUID uuid) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(config.leaseDurationSeconds());

        try {
            if (repository.tryAcquire(uuid, serverId, token, expiresAt))
                return new OwnershipResult.Acquired(token);

            PlayerOwnership current = repository.find(uuid);
            if (current == null)
                return new OwnershipResult.Failed();

            return new OwnershipResult.Unavailable(current.serverId());

        } catch (SQLException exception) {
            return new OwnershipResult.Failed();
        }
    }

    public boolean renew(UUID uuid, String token) {
        Instant newExpiresAt = Instant.now().plus(config.leaseDurationSeconds());
        try {
            return repository.renew(uuid, serverId, token, newExpiresAt);

        } catch (SQLException exception) {
            return false;
        }
    }

    public boolean release(UUID uuid, String token) {
        try {
            return repository.release(uuid, serverId, token);

        } catch (SQLException exception) {
            return false;
        }
    }
}