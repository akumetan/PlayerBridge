package io.github.akumetan.playerbridge.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

public final class EditorInventoryHolder implements InventoryHolder {

    private static final int ARMOR_ACTIVE_SLOTS = 5;

    private final EditorSession session;
    private final Inventory inventory;

    public EditorInventoryHolder(EditorSession session) {
        this.session = session;
        Editor editor = session.editor();
        this.inventory = Bukkit.createInventory(this, editor.size(), Component.text(session.targetName() + "'s " + editor.title()));
        this.inventory.setContents(editor.extract(session.snapshot()));

        if (editor == Editor.ARMOR)
            fillPadding();
    }

    private void fillPadding() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);

        for (int slot = ARMOR_ACTIVE_SLOTS; slot < inventory.getSize(); slot++)
            this.inventory.setItem(slot, filler);
    }

    public boolean isPaddingSlot(int slot) {
        return this.session.editor() == Editor.ARMOR && slot >= ARMOR_ACTIVE_SLOTS;
    }

    public EditorSession getSession() {
        return this.session;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return this.inventory;
    }
}
