package io.github.akumetan.playerbridge.gui;

import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.config.PlayerBridgeConfig;
import io.github.akumetan.playerbridge.notification.MessageFormatter;
import io.github.akumetan.playerbridge.notification.Sounds;
import io.github.akumetan.playerbridge.profile.data.LoadResult;
import io.github.akumetan.playerbridge.profile.data.PlayerData;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import io.github.akumetan.playerbridge.profile.lookup.Fetcher;
import io.github.akumetan.playerbridge.profile.ownership.OwnershipResult;
import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

public final class EditorService {

    private final PlayerBridge plugin;
    private final Fetcher fetcher;
    private final PlayerOwnershipService ownerService;
    private final PlayerDataService dataService;
    private final PlayerBridgeConfig config;

    public EditorService(PlayerBridge plugin, Fetcher fetcher, PlayerOwnershipService ownerService, PlayerDataService dataService, PlayerBridgeConfig config) {
        this.plugin = plugin;
        this.fetcher = fetcher;
        this.ownerService = ownerService;
        this.dataService = dataService;
        this.config = config;
    }

    public void open(Player player, String target, Editor editor) {
        fetcher.lookupUUID(target).whenComplete((uuid, throwable) -> {
            if (throwable != null || uuid == null) {
                Bukkit.getScheduler().runTask(plugin, () -> MessageFormatter.sendFormatted(player, this.config.messages().playerNotExist()));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> loadAndOpen(player, target, uuid, editor));
        });
    }

    private void loadAndOpen(Player player, String target, UUID uuid, Editor editor) {
        if (ownerService.owns(uuid)) {
            Bukkit.getScheduler().runTask(plugin, () -> MessageFormatter.sendFormatted(player, this.config.messages().ownershipLocal(), parsed("player", target)));
            return;
        }

        if (ownerService.find(uuid) != null) {
            Bukkit.getScheduler().runTask(plugin, () -> MessageFormatter.sendFormatted(player, this.config.messages().ownershipGlobal(), parsed("player", target)));
            return;
        }

        OwnershipResult ownerResult = ownerService.acquire(uuid);
        if (!(ownerResult instanceof OwnershipResult.Acquired)) {
            Bukkit.getScheduler().runTask(plugin, () -> MessageFormatter.sendFormatted(player, this.config.messages().ownershipAcquirementFailed(), parsed("player", target)));
            return;
        }

        LoadResult result = dataService.load(uuid, this.config.data().loginTimeout());
        if (result.status() == LoadResult.Status.BUSY) {
            Sounds.playError(player);
            return;
        }
        if (result.status() == LoadResult.Status.FAILURE) {
            Sounds.playError(player);
            return;
        }

        PlayerData data = result.data();
        if (data == null) {
            ownerService.releaseLocalOnly(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> MessageFormatter.sendFormatted(player, this.config.messages().dataNotFound(), parsed("player", target)));
            return;
        }

        EditorSession session = new EditorSession(uuid, target, data, editor);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                ownerService.release(uuid);
                return;
            }

            player.openInventory(new EditorInventoryHolder(session).getInventory());
        });

    }

}
