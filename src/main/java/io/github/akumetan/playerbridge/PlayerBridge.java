package io.github.akumetan.playerbridge;

import io.github.akumetan.playerbridge.command.CommandManager;
import io.github.akumetan.playerbridge.command.PlayerBridgeCommand;
import io.github.akumetan.playerbridge.command.PlayerBridgeTabCompleter;
import io.github.akumetan.playerbridge.command.sub.ReloadCommand;
import io.github.akumetan.playerbridge.command.sub.VersionCommand;
import io.github.akumetan.playerbridge.config.ConfigManager;
import io.github.akumetan.playerbridge.database.DatabaseManager;
import io.github.akumetan.playerbridge.listener.PlayerConnectionListener;
import io.github.akumetan.playerbridge.profile.data.PlayerDataCache;
import io.github.akumetan.playerbridge.profile.data.PlayerDataRepository;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipRepository;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class PlayerBridge extends JavaPlugin {

    private CommandManager cmdManager;
    private ConfigManager cfgManager;
    private DatabaseManager dbManager;
    private PlayerOwnershipRepository ownershipRepository;
    private PlayerOwnershipService ownershipService;
    private PlayerDataRepository dataRepository;
    private PlayerDataService dataService;
    private PlayerDataCache dataCache;


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
            return;
        }

        // Register player profile services
        this.ownershipRepository = new PlayerOwnershipRepository(dbManager);
        this.ownershipService = new PlayerOwnershipService(ownershipRepository, cfgManager.getConfig());

        this.dataRepository = new PlayerDataRepository(dbManager);
        this.dataService = new PlayerDataService(dataRepository, ownershipService, cfgManager.getConfig());
        this.dataCache = new PlayerDataCache();

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this, dataService, dataCache, cfgManager.getConfig()), this);

        // Register commands
        this.cmdManager = new CommandManager();
        this.cmdManager.register(new ReloadCommand(cfgManager));
        this.cmdManager.register(new VersionCommand(this, cfgManager.getConfig().messages()));
        this.getCommand("playerbridge").setExecutor(new PlayerBridgeCommand(cmdManager, cfgManager.getConfig().messages()));
        this.getCommand("playerbridge").setTabCompleter(new PlayerBridgeTabCompleter(cmdManager));
    }

    @Override
    public void onDisable() {
        dbManager.close();
    }
}
