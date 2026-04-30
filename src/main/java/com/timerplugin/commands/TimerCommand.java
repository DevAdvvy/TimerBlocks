package com.timerplugin.commands;

import com.timerplugin.Main;
import com.timerplugin.manager.TimerManager;
import com.timerplugin.model.TimerModel;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class TimerCommand implements CommandExecutor {
    private final Main plugin;

    public TimerCommand(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("Solo jugadores.");
                return true;
            }

            if (!player.hasPermission("timerplugin.admin")) {
                player.sendMessage(ChatColor.RED + "No tienes permisos.");
                return true;
            }

            plugin.reloadPluginConfig();

            if (plugin.getTimerManager().getCurrentTimer() != null) {
                plugin.getTimerManager().start(plugin.getTimerManager().getCurrentTimer());
            }

            player.sendMessage(ChatColor.GREEN + "Configuración recargada correctamente.");

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        if (!player.hasPermission("timerplugin.admin")) {
            player.sendMessage(ChatColor.RED + "No tienes permisos.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        TimerManager manager = plugin.getTimerManager();

        switch (args[0].toLowerCase()) {
            case "help" -> {
                sendHelp(player);
            }
            case "wand" -> {
                ItemStack wand = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = wand.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Timer Wand");
                wand.setItemMeta(meta);
                player.getInventory().addItem(wand);
                player.sendMessage(ChatColor.GREEN + "Has recibido el Timer Wand.");
            }
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer create <name>");
                    return true;
                }
                if (plugin.getSqliteManager().timerExists(args[1])) {
                    player.sendMessage(ChatColor.RED + "Ya existe un timer con ese nombre.");
                    return true;
                }
                if (manager.getCurrentTimer() != null) {
                    player.sendMessage(ChatColor.RED + "Ya existe un timer activo. Debes eliminarlo primero con /timer delete " + manager.getCurrentTimer().getName());
                    return true;
                }

                Location center = manager.getCenter(player.getUniqueId());
                if (center == null) {
                    player.sendMessage(ChatColor.RED + "Debes seleccionar el centro con el wand primero.");
                    return true;
                }
                
                TimerModel timer = new TimerModel(args[1], center, 0);
                manager.start(timer);
                plugin.getSqliteManager().saveTimer(timer);
                player.sendMessage(ChatColor.GREEN + "Timer '" + args[1] + "' creado exitosamente usando el punto central.");
            }
            case "editor" -> {
                plugin.getEditorManager().openMainEditor(player);
                if (!player.hasPermission("timerplugin.admin")) {
                    player.sendMessage(ChatColor.RED + "No tienes permisos.");
                    return true;
                }
            }

            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer delete <name>");
                    return true;

                }
                
                String timerName = args[1];
                TimerModel current = manager.getCurrentTimer();
                
                // Si es el timer activo, lo paramos y limpiamos visualmente
                if (current != null && current.getName().equalsIgnoreCase(timerName)) {
                    manager.stopCurrent();
                    plugin.getSqliteManager().deleteTimer(timerName);
                    player.sendMessage(ChatColor.GREEN + "Timer activo '" + timerName + "' detenido y eliminado.");
                } else {
                    // Si no es el activo, verificamos si existe en la DB para borrarlo
                    if (plugin.getSqliteManager().timerExists(timerName)) {
                        plugin.getSqliteManager().deleteTimer(timerName);
                        player.sendMessage(ChatColor.GREEN + "Timer '" + timerName + "' eliminado de la base de datos.");
                    } else {
                        player.sendMessage(ChatColor.RED + "No se encontró ningún timer con el nombre '" + timerName + "'.");
                    }
                }
            }
            case "stop" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer stop <name>");
                    return true;
                }
                TimerModel current = manager.getCurrentTimer();
                if (current != null && current.getName().equalsIgnoreCase(args[1])) {
                    if (!current.isActive()) {
                        player.sendMessage(ChatColor.RED + "El timer ya está detenido.");
                        return true;
                    }
                    current.setActive(false);
                    manager.render(0); // Force visual reset to 0
                    manager.stopTaskOnly(); // Stop scheduler
                    player.sendMessage(ChatColor.YELLOW + "Timer '" + args[1] + "' detenido y puesto a 0.");
                } else {
                    player.sendMessage(ChatColor.RED + "Ese timer no está activo actualmente.");
                }
            }
            case "start" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer start <name>");
                    return true;
                }
                TimerModel current = manager.getCurrentTimer();
                if (current != null && current.getName().equalsIgnoreCase(args[1])) {
                    if (current.isActive()) {
                        player.sendMessage(ChatColor.RED + "El timer ya está iniciado.");
                        return true;
                    }
                    if (current.getEndTime() <= System.currentTimeMillis()) {
                        player.sendMessage(ChatColor.RED + "El tiempo ha expirado. Usa /timer countdown para reiniciarlo.");
                        return true;
                    }
                    current.setActive(true);
                    manager.start(current);
                    player.sendMessage(ChatColor.GREEN + "Timer '" + args[1] + "' reanudado.");
                } else {
                    player.sendMessage(ChatColor.RED + "Ese timer no está cargado o no existe.");
                }
            }
            case "countdown" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer countdown <tiempo>");
                    return true;
                }
                TimerModel current = manager.getCurrentTimer();
                if (current == null) {
                    player.sendMessage(ChatColor.RED + "No hay un timer activo. Crea uno primero.");
                    return true;
                }
                long duration = manager.parseTime(args[1]);
                if (duration <= 0) {
                    player.sendMessage(ChatColor.RED + "Tiempo invlido.");
                    return true;
                }
                current.setEndTime(System.currentTimeMillis() + duration);
                current.setActive(true);
                manager.start(current);
                plugin.getSqliteManager().saveTimer(current);
                player.sendMessage(ChatColor.GREEN + "Countdown iniciado.");
            }
            case "setmaterial" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Uso: /timer setmaterial <MATERIAL>");
                    return true;
                }
                Material material = Material.matchMaterial(args[1].toUpperCase());
                if (material == null || !material.isBlock()) {
                    player.sendMessage(ChatColor.RED + "Material inválido o no es un bloque.");
                    return true;
                }
                com.timerplugin.render.DigitRenderer.setDigitMaterial(material);
                plugin.getConfig().set("render-material", material.name());
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Material del timer cambiado a: " + material.name());
            }
            default -> player.sendMessage(ChatColor.RED + "Subcomando desconocido.");

        }
        return true;

    }
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD.BOLD + "━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW.BOLD + "⏱ Timer Commands");
        player.sendMessage(ChatColor.GOLD.BOLD + "━━━━━━━━━━━━━━━━━━━━━━");

        player.sendMessage(ChatColor.GREEN + "/timer wand " + ChatColor.GRAY + "→ Obtener herramienta");
        player.sendMessage(ChatColor.GREEN + "/timer create <name> " + ChatColor.GRAY + "→ Crear timer");
        player.sendMessage(ChatColor.GREEN + "/timer delete <name> " + ChatColor.GRAY + "→ Eliminar timer");
        player.sendMessage(ChatColor.GREEN + "/timer start <name> " + ChatColor.GRAY + "→ Iniciar timer");
        player.sendMessage(ChatColor.GREEN + "/timer stop <name> " + ChatColor.GRAY + "→ Detener timer");
        player.sendMessage(ChatColor.GREEN + "/timer editor " + ChatColor.GRAY + "→ Abrir editor");
        player.sendMessage(ChatColor.GREEN + "/timer reload " + ChatColor.GRAY + "→ Recarga la Config");

        player.sendMessage(ChatColor.GOLD.BOLD + "━━━━━━━━━━━━━━━━━━━━━━");
    }
}
