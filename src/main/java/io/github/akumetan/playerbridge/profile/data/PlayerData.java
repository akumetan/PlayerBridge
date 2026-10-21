package io.github.akumetan.playerbridge.profile.data;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public record PlayerData(
        UUID uuid,
        byte[] inventory,
        byte[] enderChest,
        double health,
        double absorption,
        int foodLevel,
        float saturation,
        int experience,
        int fireTicks,
        GameMode gameMode,
        Collection<PotionEffect> activeEffects
) {

    public static PlayerData snapshot(Player player) {
        return new PlayerData(
                player.getUniqueId(),
                ItemStack.serializeItemsAsBytes(player.getInventory().getContents()),
                ItemStack.serializeItemsAsBytes(player.getEnderChest().getContents()),
                player.getHealth(),
                player.getAbsorptionAmount(),
                player.getFoodLevel(),
                player.getSaturation(),
                player.calculateTotalExperiencePoints(),
                player.getFireTicks(),
                player.getGameMode(),
                player.getActivePotionEffects());
    }

    public PlayerData withInventory(byte[] inventory) {
        return new PlayerData(this.uuid, inventory, this.enderChest, this.health, this.absorption, this.foodLevel, this.saturation, this.experience, this.fireTicks, this.gameMode, this.activeEffects);
    }

    public PlayerData withEnderChest(byte[] enderChest) {
        return new PlayerData(this.uuid, this.inventory, enderChest, this.health, this.absorption, this.foodLevel, this.saturation, this.experience, this.fireTicks, this.gameMode, this.activeEffects);
    }
}