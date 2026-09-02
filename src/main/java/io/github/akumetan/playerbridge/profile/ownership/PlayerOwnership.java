package io.github.akumetan.playerbridge.profile.ownership;

import java.time.Instant;
import java.util.UUID;

public record PlayerOwnership(
        UUID uuid,
        String serverId,
        String token,
        Instant expiresAt
) {
}