package io.github.akumetan.playerbridge.config;

import java.time.Duration;

public record PlayerDataConfig(
        Duration loginTimeout,
        Duration retryInterval,
        Duration finalSaveTimeout
) {
}