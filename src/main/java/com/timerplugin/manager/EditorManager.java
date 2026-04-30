package com.timerplugin.manager;

import com.timerplugin.Main;
import com.timerplugin.model.TimerModel;
import com.timerplugin.render.DigitRenderer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class EditorManager {
    private final Main plugin;
    private Material currentMaterial;
    private final java.util.Map<java.util.UUID, String> pendingTimeInputs = new java.util.HashMap<>();

    public EditorManager(Main plugin) {
        this.plugin = plugin;
        String defaultMat = plugin.getConfig().getString("default-material", "ORANGE_CONCRETE");
        currentMaterial = Material.matchMaterial(defaultMat);
    }

    public void startChatInput(Player player, String timerName) {
        pendingTimeInputs.put(player.getUniqueId(), timerName);
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "========================================");
        player.sendMessage(ChatColor.GOLD + "ESCRIBE EL TIEMPO EN EL CHAT:");
        player.sendMessage(ChatColor.WHITE + "Ejemplo: 30d, 3h, 10m, 10s");
        player.sendMessage(ChatColor.GRAY + "Escribe 'cancel' para abortar.");
        player.sendMessage(ChatColor.YELLOW + "========================================");
    }

    public String getPendingTimer(java.util.UUID uuid) {
        return pendingTimeInputs.get(uuid);
    }

    public void removePendingInput(java.util.UUID uuid) {
        pendingTimeInputs.remove(uuid);
    }

    public void openMainEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Timer Editor - Lista");

        // Simple list: just showing the current active timer for now as per requirement "menu de lista de timers"
        // In a more complex system, we'd list all from DB, but requirement says "active 1 active"
        TimerModel current = plugin.getTimerManager().getCurrentTimer();
        if (current != null) {
            ItemStack item = new ItemStack(Material.CLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + current.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click para editar este timer.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(13, item);
        } else {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "No hay timers activos.");
            item.setItemMeta(meta);
            inv.setItem(13, item);
        }

        player.openInventory(inv);
    }

    public void openTimerEditor(Player player, TimerModel timer) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Editando: " + timer.getName());

        // Slot 10: Color (Material)
        inv.setItem(10, createGuiItem(Material.ORANGE_CONCRETE, ChatColor.GOLD + "Cambiar Color", ChatColor.GRAY + "Click para rotar material."));
        
        // Slot 12: Stop/Start
        inv.setItem(12, createGuiItem(timer.isActive() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK, 
                timer.isActive() ? ChatColor.RED + "Stop Timer" : ChatColor.GREEN + "Start Timer", 
                ChatColor.GRAY + "Click para cambiar estado."));

        // Slot 14: Time (+1m)
        inv.setItem(14, createGuiItem(Material.CLOCK, ChatColor.AQUA + "Añadir 1 Minuto", ChatColor.GRAY + "Click para añadir tiempo."));

        // Slot 16: Delete
        inv.setItem(16, createGuiItem(Material.TNT, ChatColor.DARK_RED + "Eliminar Timer", ChatColor.GRAY + "Click para borrar permanentemente."));

        // Slot 22: Guardar
        inv.setItem(22, createGuiItem(Material.ANVIL, ChatColor.YELLOW + "Guardar en DB", ChatColor.GRAY + "Click para forzar guardado."));

        player.openInventory(inv);
    }

    public Material getCurrentMaterial() {
        return currentMaterial;
    }

    public void setCurrentMaterial(Material material) {
        this.currentMaterial = material;
        DigitRenderer.setDigitMaterial(material);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> l = new ArrayList<>();
        for (String s : lore) l.add(s);
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }
}
