package io.github.akumetan.playerbridge.config;

import io.github.akumetan.playerbridge.PlayerBridge;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final PlayerBridge plugin;
    private PlayerBridgeConfig config;

    public ConfigManager(PlayerBridge plugin) {
        this.plugin = plugin;
        this.loadConfiguration();
    }

    public void loadConfiguration() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration file = plugin.getConfig();

        var database = new DatabaseConfig(
                file.getString("database.host", "127.0.0.1"),
                file.getInt("database.port", 3306),
                file.getString("database.database", "player_bridge"),
                file.getString("database.username", "root"),
                file.getString("database.password", ""),
                file.getInt("database.max-pool-size", 10),
                file.getInt("database.min-pool-size", 2),
                file.getInt("database.connection-timeout-ms", 5000)
        );

        var messages = new MessagesConfig(
                file.getString("messages.no-permission", "<red>You do not have permission to use this command.</red>"),
                file.getString("messages.reload-config", "<green>Configuration successfully reloaded.</green>"),
                file.getString("messages.check-version", "<gray>You are running version <white><version></white> (<white><server_version></white>).</gray>")
        );

        this.config = new PlayerBridgeConfig(database, messages);
    }

    public PlayerBridgeConfig getConfig() {
        return this.config;
    }
}