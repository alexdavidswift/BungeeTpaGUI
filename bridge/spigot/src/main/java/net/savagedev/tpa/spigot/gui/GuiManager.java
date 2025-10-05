package net.savagedev.tpa.spigot.gui;

import net.savagedev.tpa.common.messaging.messages.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.List;

public class GuiManager implements Listener {
    private static final String INVENTORY_TITLE = "Teleport to a player...";

    public void openTeleportGui(Player player, List<PlayerInfo> players) {
        final Inventory gui = Bukkit.createInventory(null, 54, INVENTORY_TITLE);

        for (PlayerInfo playerInfo : players) {
            if (player.getUniqueId().equals(playerInfo.getUuid())) {
                continue;
            }

            final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta meta = (SkullMeta) skull.getItemMeta();

            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerInfo.getUuid()));
                meta.setDisplayName(ChatColor.GREEN + playerInfo.getName());
                meta.setLore(Collections.singletonList(ChatColor.YELLOW + "Server: " + playerInfo.getServer()));
                skull.setItemMeta(meta);
            }

            gui.addItem(skull);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(INVENTORY_TITLE)) {
            return;
        }

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();

        if (meta == null || meta.getOwningPlayer() == null) {
            return;
        }

        final String targetName = ChatColor.stripColor(meta.getDisplayName());

        player.performCommand("tpa " + targetName);
        player.closeInventory();
    }
} 