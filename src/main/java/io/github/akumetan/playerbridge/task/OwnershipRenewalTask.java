package io.github.akumetan.playerbridge.task;

import io.github.akumetan.playerbridge.profile.ownership.PlayerOwnershipService;

public final class OwnershipRenewalTask implements Runnable {

    private final PlayerOwnershipService ownershipService;

    public OwnershipRenewalTask(PlayerOwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @Override
    public void run() {
        this.ownershipService.renewAll();
    }
}