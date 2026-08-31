package io.github.akumetan.playerbridge;

import io.github.akumetan.playerbridge.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerBridge extends JavaPlugin {

    private ConfigManager cfgManager;

    @Override
    public void onEnable() {
        this.cfgManager = new ConfigManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
