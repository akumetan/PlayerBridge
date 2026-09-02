package io.github.akumetan.playerbridge.task;

import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.profile.data.PlayerData;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PlayerAutoSaveTask implements Runnable {

    private final PlayerBridge plugin;
    private final PlayerDataService service;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PlayerAutoSaveTask(PlayerBridge plugin, PlayerDataService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public void run() {
        if (!this.running.compareAndSet(false, true))
            return;

        List<PlayerData> snapshots = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
            snapshots.add(PlayerData.snapshot(player));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                for (PlayerData snapshot : snapshots)
                    this.service.save(snapshot);

            } finally {
                this.running.set(false);
            }
        });
    }
}