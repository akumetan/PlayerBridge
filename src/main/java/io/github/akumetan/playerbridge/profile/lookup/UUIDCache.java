package io.github.akumetan.playerbridge.profile.lookup;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class UUIDCache {

    private final ConcurrentNavigableMap<String, UUID> storedUUIDs = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public void put(String username, UUID uuid) {
        if (username != null)
            this.storedUUIDs.put(username, uuid);
    }

    public UUID get(String username) {
        if (username == null)
            return null;
        return this.storedUUIDs.get(username);
    }

    public Collection<String> getExactUsernames() {
        return List.copyOf(this.storedUUIDs.keySet());
    }
}
