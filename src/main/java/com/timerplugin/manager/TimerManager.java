package com.timerplugin.manager;

import com.timerplugin.Main;
import com.timerplugin.model.TimerModel;
import com.timerplugin.render.DigitRenderer;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class TimerManager {
    private final Main plugin;
    private TimerModel currentTimer;
    private String lastRenderedString = "";
    private BukkitTask task;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<UUID, Location> centers = new HashMap<>();

    public TimerManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setCenter(UUID uuid, Location loc) {
        centers.put(uuid, loc);
    }

    public Location getCenter(UUID uuid) {
        return centers.get(uuid);
    }

    private static String applyGradient(String text, String startHex, String endHex) {

        StringBuilder result = new StringBuilder();

        java.awt.Color start = java.awt.Color.decode("#" + startHex);
        java.awt.Color end = java.awt.Color.decode("#" + endHex);

        int length = text.length();

        for (int i = 0; i < length; i++) {

            float ratio = (float) i / (length - 1);

            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            String hex = String.format("#%02x%02x%02x", r, g, b);

            result.append(ChatColor.of(hex)).append(text.charAt(i));
        }

        return result.toString();
    }


    public void start(TimerModel timer) {
        if (currentTimer != null) {
            stopCurrent();
        }
        this.currentTimer = timer;
        this.lastRenderedString = "";
        startTask();
    }

    public void stopCurrent() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (currentTimer != null) {
            DigitRenderer.clearString(currentTimer.getCenter());
            currentTimer = null;
            lastRenderedString = "";
        }
    }

    public void stopTaskOnly() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastRenderedString = ""; // Reset optimization to allow forced render
    }

    public void startFireworks(Location base) {

        int duration = plugin.getConfig().getInt("fireworks-duration", 10);

        int charWidth = 7;
        int spacing = 1;

        int length = lastRenderedString.length();

        double totalWidth = length * (charWidth + spacing);

        new org.bukkit.scheduler.BukkitRunnable() {

            int time = 0;

            @Override
            public void run() {

                if (time >= duration) {
                    cancel();
                    return;
                }

                for (double x = 0; x < totalWidth; x += 3) {

                    // ARRIBA
                    Location top = base.clone().add(x, 2, 2);
                    spawnFirework(top);

                    // ABAJO
                    Location bottom = base.clone().add(x, 2, -2);
                    spawnFirework(bottom);
                }

                time++;
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnFirework(Location loc) {

        loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class, fw -> {

            org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();

            meta.addEffect(org.bukkit.FireworkEffect.builder()
                    .withColor(
                            org.bukkit.Color.RED,
                            org.bukkit.Color.ORANGE,
                            org.bukkit.Color.YELLOW
                    )
                    .withFade(org.bukkit.Color.WHITE)
                    .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .flicker(true)
                    .build()
            );

            meta.setPower(2);
            fw.setFireworkMeta(meta);
        });
    }

    private String convertHexToMiniMessage(String text) {
        return text.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
    }

    private void sendAnnounce() {

        List<String> messages = plugin.getConfig().getStringList("announce");

        for (String msg : messages) {
            msg = convertHexToMiniMessage(msg);
            Component component = miniMessage.deserialize(msg);
            Bukkit.getServer().sendMessage(component);
        }
    }

    private void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentTimer == null || !currentTimer.isActive()) {
                if (task != null) task.cancel();
                return;
            }

            long remaining = currentTimer.getEndTime() - System.currentTimeMillis();
            if (remaining <= 0) {
                render(0);
                currentTimer.setActive(false);
                sendAnnounce();

                // 🔥 NUEVO
                if (plugin.getConfig().getBoolean("fireworks-on-finish")) {
                    startFireworks(currentTimer.getCenter());
                }

                if (task != null) task.cancel();
                return;
            }

            render(remaining);
        }, 0L, 20L);
    }

    public void render(long millis) {
        String timeStr = formatTime(millis);
        DigitRenderer.renderString(currentTimer.getCenter(), lastRenderedString, timeStr);
        lastRenderedString = timeStr;
    }

    private Location getRealCenter(Location base) {

        if (lastRenderedString == null || lastRenderedString.isEmpty()) {
            return base;
        }

        int charWidth = 7;
        int spacing = 1;

        int length = lastRenderedString.length();

        double totalWidth = length * (charWidth + spacing);
        double offset = totalWidth / 2.0;

        // 👉 Si tu texto va en X (lo más común)
        return base.clone().add(offset, 0, 0);

        // 👉 Si va en Z usa esto en su lugar:
        // return base.clone().add(0, 0, offset);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
    }

    public void refreshWithNewMaterial() {
        if (currentTimer == null) return;

        stopTaskOnly();

        DigitRenderer.clearString(currentTimer.getCenter());

        long remaining = currentTimer.getEndTime() - System.currentTimeMillis();
        render(remaining);
    }

    public long parseTime(String input) {
        long totalMillis = 0;
        String number = "";
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                number += c;
            } else {
                if (number.isEmpty()) continue;
                long val = Long.parseLong(number);
                switch (c) {
                    case 'd': totalMillis += val * 24 * 3600 * 1000; break;
                    case 'h': totalMillis += val * 3600 * 1000; break;
                    case 'm': totalMillis += val * 60 * 1000; break;
                    case 's': totalMillis += val * 1000; break;
                }
                number = "";
            }
        }
        return totalMillis;
    }

    public TimerModel getCurrentTimer() {
        return currentTimer;
    }
}
