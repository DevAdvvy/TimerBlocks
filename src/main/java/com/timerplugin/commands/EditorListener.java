package com.timerplugin.commands;

import com.timerplugin.Main;
import com.timerplugin.model.TimerModel;
import com.timerplugin.render.DigitRenderer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EditorListener implements Listener {
    private final Main plugin;

    public EditorListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (!title.contains("Timer Editor") && !title.contains("Editando:")) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        TimerModel timer = plugin.getTimerManager().getCurrentTimer();

        if (title.contains("Lista")) {
            if (event.getSlot() == 13 && timer != null) {
                plugin.getEditorManager().openTimerEditor(player, timer);
            }
            return;
        }

        if (title.contains("Editando:")) {
            if (timer == null) return;

            switch (event.getSlot()) {

                case 10 -> {

                    java.util.List<Material> materials = plugin.getAvailableMaterials();
                    if (materials.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "No hay materiales configurados.");
                        return;
                    }

                    Material current = plugin.getEditorManager().getCurrentMaterial();
                    int index = materials.indexOf(current);
                    int nextIndex = (index + 1) % materials.size();

                    Material next = materials.get(nextIndex);

                    plugin.getEditorManager().setCurrentMaterial(next);

                    plugin.getTimerManager().start(timer);

                    player.sendMessage(ChatColor.GREEN + "Material cambiado a: " + next.name());

                    plugin.getEditorManager().openTimerEditor(player, timer);
                }

                case 12 -> { // Start/Stop
                    if (timer.isActive()) {
                        timer.setActive(false);
                        plugin.getTimerManager().render(0);
                        plugin.getTimerManager().stopTaskOnly();
                        player.sendMessage(ChatColor.YELLOW + "Timer detenido.");
                    } else {
                        timer.setActive(true);
                        plugin.getTimerManager().start(timer);
                        player.sendMessage(ChatColor.GREEN + "Timer iniciado.");
                    }
                    plugin.getEditorManager().openTimerEditor(player, timer);
                }

                case 14 -> { // Chat input
                    plugin.getEditorManager().startChatInput(player, timer.getName());
                }

                case 16 -> { // Delete
                    plugin.getTimerManager().stopCurrent();
                    plugin.getSqliteManager().deleteTimer(timer.getName());
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Timer eliminado.");
                }

                case 22 -> { // Save
                    plugin.getSqliteManager().saveTimer(timer);
                    player.sendMessage(ChatColor.YELLOW + "Datos guardados en SQLite.");
                }
            }
        }
    }
}
