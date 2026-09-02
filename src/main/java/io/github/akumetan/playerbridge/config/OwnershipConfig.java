package io.github.akumetan.playerbridge.config;

import java.time.Duration;

public record OwnershipConfig(
        Duration leaseDuration,
        Duration renewalInterval
) {
}
