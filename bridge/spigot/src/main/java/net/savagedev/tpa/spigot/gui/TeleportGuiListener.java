package net.savagedev.tpa.spigot.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;

public class TeleportGuiListener implements Listener {
    private final TeleportGuiManager manager;

    public TeleportGuiListener(TeleportGuiManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Map<Integer, String> map = this.manager.getTargets(player);
        if (map == null) {
            return;
        }
        event.setCancelled(true);
        String target = map.get(event.getRawSlot());
        if (target != null) {
            player.closeInventory();
            player.chat("/tp " + target);
            this.manager.close(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            this.manager.close((Player) event.getPlayer());
        }
    }
}
