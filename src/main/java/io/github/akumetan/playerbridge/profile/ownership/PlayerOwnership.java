package io.github.akumetan.playerbridge.profile.ownership;

import java.time.Instant;

public record PlayerOwnership(
        String serverId,
        String token,
        Instant expiresAt
) {
}