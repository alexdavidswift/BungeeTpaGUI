package net.savagedev.tpa.spigot.gui;

import net.savagedev.tpa.spigot.BungeeTpSpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeleportGuiManager {
    private final BungeeTpSpigotPlugin plugin;
    private final Map<UUID, Map<Integer, String>> targetMap = new HashMap<>();

    public TeleportGuiManager(BungeeTpSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGui(Player player, Map<UUID, String> targets) {
        int size = ((targets.size() - 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Teleport");
        Map<Integer, String> slotMap = new HashMap<>();
        List<Map.Entry<UUID, String>> entries = new ArrayList<>(targets.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<UUID, String> entry = entries.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            OfflinePlayer off = Bukkit.getOfflinePlayer(entry.getKey());
            meta.setOwningPlayer(off);
            meta.setDisplayName(ChatColor.YELLOW + entry.getValue());
            head.setItemMeta(meta);
            inv.setItem(i, head);
            slotMap.put(i, entry.getValue());
        }
        this.targetMap.put(player.getUniqueId(), slotMap);
        player.openInventory(inv);
    }

    public Map<Integer, String> getTargets(Player player) {
        return this.targetMap.get(player.getUniqueId());
    }

    public void close(Player player) {
        this.targetMap.remove(player.getUniqueId());
    }
}
