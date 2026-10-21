package io.github.akumetan.playerbridge.gui;

import io.github.akumetan.playerbridge.profile.data.PlayerData;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public enum Editor {
    STORAGE(36, "inventory") {
        @Override
        public ItemStack[] extract(PlayerData data) {
            ItemStack[] inventory = ItemStack.deserializeItemsFromBytes(data.inventory());
            return Arrays.copyOfRange(inventory, 0, size());
        }

        @Override
        public PlayerData applyEdit(PlayerData original, ItemStack[] editedContents) {
            ItemStack[] inventory = ItemStack.deserializeItemsFromBytes(original.inventory());
            System.arraycopy(editedContents, 0, inventory, 0, Math.min(size(), editedContents.length));
            return original.withInventory(ItemStack.serializeItemsAsBytes(inventory));
        }
    },

    ARMOR(9, "armor & offhand") {
        private static final int STORAGE_OFFSET = 36;
        private static final int ACTIVE_SLOTS = 5;

        @Override
        public ItemStack[] extract(PlayerData data) {
            ItemStack[] inventory = ItemStack.deserializeItemsFromBytes(data.inventory());
            return Arrays.copyOfRange(inventory, STORAGE_OFFSET, STORAGE_OFFSET + ACTIVE_SLOTS);
        }

        @Override
        public PlayerData applyEdit(PlayerData original, ItemStack[] editedContents) {
            ItemStack[] inventory = ItemStack.deserializeItemsFromBytes(original.inventory());
            System.arraycopy(editedContents, 0, inventory, STORAGE_OFFSET, Math.min(ACTIVE_SLOTS, editedContents.length));
            return original.withInventory(ItemStack.serializeItemsAsBytes(inventory));
        }
    },

    ENDER_CHEST(27, "ender chest") {
        @Override
        public ItemStack[] extract(PlayerData data) {
            ItemStack[] inventory = ItemStack.deserializeItemsFromBytes(data.enderChest());

            return Arrays.copyOf(inventory, size());
        }

        @Override
        public PlayerData applyEdit(PlayerData original, ItemStack[] editedContents) {
            ItemStack[] enderChest = Arrays.copyOf(editedContents, size());

            return original.withEnderChest(ItemStack.serializeItemsAsBytes(enderChest));
        }
    };

    private final int size;
    private final String title;

    Editor(int size, String title) {
        this.size = size;
        this.title = title;
    }

    public int size() {
        return size;
    }

    public String title() {
        return title;
    }

    public abstract ItemStack[] extract(PlayerData data);

    public abstract PlayerData applyEdit(PlayerData original, ItemStack[] editedContents);
}
