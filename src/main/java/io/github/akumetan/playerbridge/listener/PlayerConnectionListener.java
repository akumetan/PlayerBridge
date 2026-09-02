package io.github.akumetan.playerbridge.listener;

import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.config.PlayerBridgeConfig;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import io.github.akumetan.playerbridge.notification.Sounds;
import io.github.akumetan.playerbridge.profile.data.LoadResult;
import io.github.akumetan.playerbridge.profile.data.PlayerData;
import io.github.akumetan.playerbridge.profile.data.PlayerDataCache;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public final class PlayerConnectionListener implements Listener {

    private final PlayerBridge plugin;
    private final PlayerDataService service;
    private final PlayerDataCache cache;
    private final PlayerBridgeConfig config;

    public PlayerConnectionListener(PlayerBridge plugin, PlayerDataService service, PlayerDataCache cache, PlayerBridgeConfig config) {
        this.plugin = plugin;
        this.service = service;
        this.cache = cache;
        this.config = config;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        LoadResult result = service.load(uuid, config.data().loginTimeout());
        if (result.status() == LoadResult.Status.BUSY) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageFormatter.formatRaw(config.messages().kickLoadingBusy()));
            return;
        }
        if (result.status() == LoadResult.Status.FAILURE) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageFormatter.formatRaw(config.messages().kickLoadingError()));
            return;
        }

        PlayerData data = result.data();
        if (data != null) {
            this.cache.put(uuid, data);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData data = this.cache.take(player.getUniqueId());
        if (data == null) {
            Sounds.playSyncComplete(player);
            return;
        }

        if (this.config.modules().inventory())
            player.getInventory().setContents(ItemStack.deserializeItemsFromBytes(data.inventory()));

        if (this.config.modules().enderChest())
            player.getEnderChest().setContents(ItemStack.deserializeItemsFromBytes(data.enderChest()));

        if (this.config.modules().healthAndFood()) {
            player.setHealth(data.health());
            player.setAbsorptionAmount(data.absorption());
            player.setFoodLevel(data.foodLevel());
            player.setSaturation(data.saturation());
        }

        if (this.config.modules().experience())
            player.setTotalExperience(data.experience());

        if (this.config.modules().fireTicks()) {
            player.setFireTicks(data.fireTicks());
        }

        if (this.config.modules().gameMode()) {
            player.setGameMode(data.gameMode());
        }

        if (this.config.modules().activeEffects()) {
            player.clearActivePotionEffects();
            for (PotionEffect effect : data.activeEffects()) {
                player.addPotionEffect(effect);
            }
        }
        Sounds.playSyncComplete(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        cache.remove(uuid);

        PlayerData snapshot = PlayerData.snapshot(player);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> service.saveAndRelease(uuid, snapshot));
    }
}