package io.github.akumetan.playerbridge.config;

public record PlayerBridgeConfig(
        DatabaseConfig database,
        MessagesConfig messages,
        ModulesConfig modules,
        OwnershipConfig ownership,
        PlayerDataConfig data
) {
}