package io.github.akumetan.playerbridge.profile.data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerDataCache {

    private final ConcurrentMap<UUID, PlayerData> pending = new ConcurrentHashMap<>();

    public void put(UUID uuid, PlayerData data) {
        this.pending.put(uuid, data);
    }

    public PlayerData take(UUID uuid) {
        return this.pending.remove(uuid);
    }

    public void remove(UUID uuid) {
        this.pending.remove(uuid);
    }
}