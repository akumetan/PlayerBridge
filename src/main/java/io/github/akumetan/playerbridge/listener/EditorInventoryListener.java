package io.github.akumetan.playerbridge.listener;


import io.github.akumetan.playerbridge.PlayerBridge;
import io.github.akumetan.playerbridge.gui.EditorInventoryHolder;
import io.github.akumetan.playerbridge.gui.EditorSession;
import io.github.akumetan.playerbridge.notification.Sounds;
import io.github.akumetan.playerbridge.profile.data.PlayerData;
import io.github.akumetan.playerbridge.profile.data.PlayerDataService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class EditorInventoryListener implements Listener {

    private final PlayerBridge plugin;
    private final PlayerDataService service;

    public EditorInventoryListener(PlayerBridge plugin, PlayerDataService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;

        if (!(event.getInventory().getHolder() instanceof EditorInventoryHolder editor))
            return;

        EditorSession session = editor.getSession();
        ItemStack[] editedContents = event.getInventory().getContents();
        PlayerData updated = session.editor().applyEdit(session.snapshot(), editedContents);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean saved = service.saveAndRelease(session.targetUUID(), updated);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (saved)
                    Sounds.playSyncComplete(player);
                else
                    Sounds.playError(player);
            });
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EditorInventoryHolder editor))
            return;

        if (event.getClickedInventory() == event.getInventory() && editor.isPaddingSlot(event.getSlot()))
            event.setCancelled(true);
    }
}
