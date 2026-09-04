package io.github.akumetan.playerbridge.profile.lookup;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class Fetcher {

    private final UUIDCache cache;

    public Fetcher(UUIDCache cache) {
        this.cache = cache;
    }

    public CompletableFuture<UUID> lookupUUID(String username) {
        UUID cachedUUID = this.cache.get(username);
        if (cachedUUID != null) {
            return CompletableFuture.completedFuture(cachedUUID);
        }

        Player onlinePlayer = Bukkit.getPlayerExact(username);
        if (onlinePlayer != null) {
            UUID onlineUUID = onlinePlayer.getUniqueId();
            cache.put(username, onlineUUID);
            return CompletableFuture.completedFuture(onlineUUID);
        }

        OfflinePlayer knownPlayer = Bukkit.getOfflinePlayerIfCached(username);
        if (knownPlayer != null) {
            UUID localUUID = knownPlayer.getUniqueId();
            cache.put(username, localUUID);
            return CompletableFuture.completedFuture(localUUID);
        }

        return CompletableFuture.supplyAsync(() -> {
            UUID mojangUUID = Bukkit.getOfflinePlayer(username).getUniqueId();
            cache.put(username, mojangUUID);
            return mojangUUID;
        });
    }
}
