package io.github.akumetan.playerbridge.profile.data;

public record LoadResult(Status status, PlayerData data) {

    public static LoadResult success(PlayerData data) {
        return new LoadResult(Status.SUCCESS, data);
    }

    public static LoadResult busy() {
        return new LoadResult(Status.BUSY, null);
    }

    public static LoadResult failure() {
        return new LoadResult(Status.FAILURE, null);
    }

    public boolean successful() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        BUSY,
        FAILURE
    }
}