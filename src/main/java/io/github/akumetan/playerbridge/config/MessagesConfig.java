package io.github.akumetan.playerbridge.config;

public record MessagesConfig(
        String noPermission,
        String reloadConfig,
        String checkVersion,
        String kickLoadingBusy,
        String kickLoadingError,
        String playerNotExist,
        String databaseError
) {
}