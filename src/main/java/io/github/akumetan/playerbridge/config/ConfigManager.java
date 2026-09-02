package io.github.akumetan.playerbridge.config;

import io.github.akumetan.playerbridge.PlayerBridge;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.UUID;

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

        var serverId = yml.getString("server-id");
        if (serverId == null || serverId.isBlank()) {
            serverId = UUID.randomUUID().toString();
            yml.set("server-id", serverId);
            plugin.saveDefaultConfig();
        }

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
                yml.getString("messages.check-version", "<gray>You are running version <white><version></white> (<white><server_version></white>).</gray>"),
                yml.getString("messages.kick-loading-busy", "<red>Your data is currently saving on another server. Please try reconnecting.</red>"),
                yml.getString("messages.kick-loading-error", "<red>Failed to load your data. Please try again later.</red>")
        );

        var modules = new ModulesConfig(
                yml.getBoolean("modules.inventory", true),
                yml.getBoolean("modules.ender-chest", true),
                yml.getBoolean("modules.health-and-food", true),
                yml.getBoolean("modules.experience", true),
                yml.getBoolean("modules.fire-ticks", true),
                yml.getBoolean("modules.game-mode", true),
                yml.getBoolean("modules.active-effects", true)
        );

        var ownership = new OwnershipConfig(
                Duration.ofSeconds(yml.getLong("ownership.lease-duration-seconds", 60L))
        );

        var data = new PlayerDataConfig(
                Duration.ofSeconds(yml.getLong("player-data.login-timeout-seconds", 5L)),
                Duration.ofMillis(yml.getLong("player-data.retry-interval-ms", 250L)),
                Duration.ofSeconds(yml.getLong("player-data.final-save-timeout-seconds", 15L))
        );

        this.config = new PlayerBridgeConfig(serverId, database, messages, modules, ownership, data);
    }

    public PlayerBridgeConfig getConfig() {
        return this.config;
    }
}