package io.github.akumetan.playerbridge.config;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password,
        int maxPoolSize,
        int minPoolSize,
        int connectionTimeoutMs
) {
}