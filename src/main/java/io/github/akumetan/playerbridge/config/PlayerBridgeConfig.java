package io.github.akumetan.playerbridge.config;

public record PlayerBridgeConfig(
        DatabaseConfig database,
        MessagesConfig messages,
        OwnershipConfig ownership,
        PlayerDataConfig data
) {
}