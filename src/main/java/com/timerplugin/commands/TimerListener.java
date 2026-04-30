package com.timerplugin.commands;

import com.timerplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TimerListener implements Listener {
    private final Main plugin;

    public TimerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.BLAZE_ROD) return;
        if (!event.getItem().hasItemMeta() || !event.getItem().getItemMeta().getDisplayName().contains("Timer Wand")) return;

        event.setCancelled(true);
        if (event.getClickedBlock() == null) return;

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        plugin.getTimerManager().setCenter(event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation());
        
        event.getPlayer().getInventory().remove(event.getItem());
        event.getPlayer().sendMessage(ChatColor.GREEN + "¡Centro seleccionado en: " +
                event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ() + "!");
        event.getPlayer().sendMessage(ChatColor.GOLD + "El wand ha sido removido. Ahora puedes usar /timer create <nombre>");
    }
}
