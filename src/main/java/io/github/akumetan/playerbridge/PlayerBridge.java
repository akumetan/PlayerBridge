package io.github.akumetan.playerbridge;

import io.github.akumetan.playerbridge.config.ConfigManager;
import io.github.akumetan.playerbridge.database.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class PlayerBridge extends JavaPlugin {

    private ConfigManager cfgManager;
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        // Init config
        this.cfgManager = new ConfigManager(this);

        // Init database
        this.dbManager = new DatabaseManager(cfgManager.getConfig().database());
        try {
            this.dbManager.connect();
            this.getComponentLogger().info(Component.text("Database connection successful.").color(NamedTextColor.GREEN));

        } catch (SQLException e) {
            this.getComponentLogger().error(Component.text("Failed to initialize database connection. Disabling...").color(NamedTextColor.RED));
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        dbManager.close();
    }
}
