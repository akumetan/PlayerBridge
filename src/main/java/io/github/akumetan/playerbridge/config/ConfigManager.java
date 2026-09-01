package io.github.akumetan.playerbridge.config;

import io.github.akumetan.playerbridge.PlayerBridge;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;

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
        FileConfiguration yml = plugin.getConfig();

        var database = new DatabaseConfig(
                yml.getString("database.host", "127.0.0.1"),
                yml.getInt("database.port", 3306),
                yml.getString("database.database", "player_bridge"),
                yml.getString("database.username", "root"),
                yml.getString("database.password", ""),
                yml.getInt("database.max-pool-size", 10),
                yml.getInt("database.min-pool-size", 2),
                yml.getInt("database.connection-timeout-ms", 5000)
        );

        var messages = new MessagesConfig(
                yml.getString("messages.no-permission", "<red>You do not have permission to use this command.</red>"),
                yml.getString("messages.reload-config", "<green>Configuration successfully reloaded.</green>"),
                yml.getString("messages.check-version", "<gray>You are running version <white><version></white> (<white><server_version></white>).</gray>")
        );

        var ownership = new OwnershipConfig(
                Duration.ofSeconds(yml.getLong("ownership.lease-duration-seconds", 60L))
        );

        this.config = new PlayerBridgeConfig(database, messages, ownership);
    }

    public PlayerBridgeConfig getConfig() {
        return this.config;
    }
}