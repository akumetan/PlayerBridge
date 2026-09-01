package io.github.akumetan.playerbridge.profile.ownership;

public sealed interface OwnershipResult permits
        OwnershipResult.Acquired,
        OwnershipResult.Unavailable,
        OwnershipResult.Failed {

    record Acquired(String token) implements OwnershipResult {
    }

    record Unavailable(String serverId) implements OwnershipResult {
    }

    record Failed() implements OwnershipResult {
    }
}