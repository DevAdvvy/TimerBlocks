package com.timerplugin.commands;

import com.timerplugin.Main;
import com.timerplugin.model.TimerModel;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatInputListener implements Listener {
    private final Main plugin;

    public ChatInputListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String timerName = plugin.getEditorManager().getPendingTimer(player.getUniqueId());

        if (timerName == null) return;

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.equalsIgnoreCase("cancel")) {
            plugin.getEditorManager().removePendingInput(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Entrada de tiempo cancelada.");
            return;
        }

        long duration = plugin.getTimerManager().parseTime(message);
        if (duration <= 0) {
            player.sendMessage(ChatColor.RED + "Formato de tiempo inválido. Usa: 30d, 3h, 10m, 10s");
            return;
        }

        plugin.getEditorManager().removePendingInput(player.getUniqueId());

        // Update timer on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            TimerModel timer = plugin.getTimerManager().getCurrentTimer();
            if (timer != null && timer.getName().equalsIgnoreCase(timerName)) {
                timer.setEndTime(System.currentTimeMillis() + duration);
                timer.setActive(true);
                plugin.getTimerManager().start(timer);
                plugin.getSqliteManager().saveTimer(timer);
                player.sendMessage(ChatColor.GREEN + "¡Tiempo actualizado correctamente!");
                plugin.getEditorManager().openTimerEditor(player, timer);
            } else {
                player.sendMessage(ChatColor.RED + "Error: El timer ya no está cargado.");
            }
        });
    }
}
