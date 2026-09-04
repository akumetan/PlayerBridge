package io.github.akumetan.playerbridge;

import io.github.akumetan.playerbridge.command.CommandManager;
import io.github.akumetan.playerbridge.command.PlayerBridgeCommand;
import io.github.akumetan.playerbridge.command.PlayerBridgeTabCompleter;
import io.github.akumetan.playerbridge.command.sub.ReloadCommand;
import io.github.akumetan.playerbridge.command.sub.StatusCommand;
import io.github.akumetan.playerbridge.command.sub.VersionCommand;
import io.github.akumetan.playerbridge.config.ConfigManager;
import io.github.akumetan.playerbridge.database.DatabaseManager;
import io.github.akumetan.playerbridge.listener.PlayerConnectionListener;
import io.github.akumetan.playerbridge.profile.data.PlayerData;
import io.github.akumetan.playerbridge.profile.data.PlayerDataCache;
import io.github.akumetan.playerbridge.profile.data.PlayerDataRepository;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import io.github.akumetan.playerbridge.profile.lookup.Fetcher;
import io.github.akumetan.playerbridge.profile.lookup.UUIDCache;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipRepository;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;
import io.github.akumetan.playerbridge.task.OwnershipRenewalTask;
import io.github.akumetan.playerbridge.task.PlayerAutoSaveTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.UUID;

public final class PlayerBridge extends JavaPlugin {

    private CommandManager cmdManager;
    private ConfigManager cfgManager;
    private DatabaseManager dbManager;
    private PlayerOwnershipRepository ownershipRepository;
    private PlayerOwnershipService ownershipService;
    private PlayerDataRepository dataRepository;
    private PlayerDataService dataService;
    private PlayerDataCache dataCache;
    private UUIDCache uuidCache;
    private Fetcher fetcher;
    private OwnershipRenewalTask renewalTask;
    private PlayerAutoSaveTask autoSaveTask;


    @Override
    public void onEnable() {
        this.cfgManager = new ConfigManager(this);

        this.dbManager = new DatabaseManager(cfgManager.getConfig().database());
        try {
            this.dbManager.connect();
            this.getComponentLogger().info(Component.text("Database connection successful.").color(NamedTextColor.GREEN));

        } catch (SQLException e) {
            this.getComponentLogger().error(Component.text("Failed to initialize database connection. Disabling...").color(NamedTextColor.RED));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.ownershipRepository = new PlayerOwnershipRepository(dbManager);
        this.ownershipService = new PlayerOwnershipService(ownershipRepository, cfgManager.getConfig());

        this.dataRepository = new PlayerDataRepository(dbManager);
        this.dataService = new PlayerDataService(dataRepository, ownershipService, cfgManager.getConfig());
        this.dataCache = new PlayerDataCache();

        this.uuidCache = new UUIDCache();
        this.fetcher = new Fetcher(this.uuidCache);

        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this, dataService, dataCache, cfgManager.getConfig()), this);

        this.cmdManager = new CommandManager();
        this.cmdManager.register(new ReloadCommand(cfgManager));
        this.cmdManager.register(new StatusCommand(this, cfgManager.getConfig().messages(), ownershipService, fetcher));
        this.cmdManager.register(new VersionCommand(this, cfgManager.getConfig().messages()));
        this.getCommand("playerbridge").setExecutor(new PlayerBridgeCommand(cmdManager, cfgManager.getConfig().messages()));
        this.getCommand("playerbridge").setTabCompleter(new PlayerBridgeTabCompleter(cmdManager));

        long renewalTicks = cfgManager.getConfig().ownership().renewalInterval().toMillis() / 50L;
        this.renewalTask = new OwnershipRenewalTask(ownershipService);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, renewalTask, renewalTicks, renewalTicks);

        long autoSaveTicks = cfgManager.getConfig().data().autoSaveInterval().toMillis() / 50L;
        this.autoSaveTask = new PlayerAutoSaveTask(this, dataService);
        this.getServer().getScheduler().runTaskTimer(this, autoSaveTask, autoSaveTicks, autoSaveTicks);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData snapshot = PlayerData.snapshot(player);
            dataService.saveAndRelease(uuid, snapshot);
        }
        ownershipService.releaseAll();

        dbManager.close();
    }
}
