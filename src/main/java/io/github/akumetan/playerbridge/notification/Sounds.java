package io.github.akumetan.playerbridge.notification;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {

    public static void playSyncComplete(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.5f, 1.8f);
    }

    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.2f);
    }
}
