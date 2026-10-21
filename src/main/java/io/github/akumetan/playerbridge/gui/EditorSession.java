package io.github.akumetan.playerbridge.gui;

import io.github.akumetan.playerbridge.profile.data.PlayerData;

import java.util.UUID;

public record EditorSession(UUID targetUUID, String targetName, PlayerData snapshot, Editor editor) {
}
