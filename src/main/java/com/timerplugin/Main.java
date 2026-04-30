package com.timerplugin;

import com.timerplugin.commands.EditorListener;
import com.timerplugin.commands.TimerCommand;
import com.timerplugin.commands.TimerListener;
import com.timerplugin.manager.EditorManager;
import com.timerplugin.manager.SQLiteManager;
import com.timerplugin.manager.TimerManager;
import com.timerplugin.render.DigitRenderer;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.ArrayList;

public class Main extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private TimerManager timerManager;
    private EditorManager editorManager;
    private List<Material> availableMaterials = new ArrayList<>();
    private String color(String msg) {
        return msg
                .replace("&0", "\u001B[30m") // negro
                .replace("&4", "\u001B[31m") // rojo oscuro
                .replace("&c", "\u001B[91m") // rojo claro
                .replace("&f", "\u001B[97m") // blanco
                .replace("&7", "\u001B[37m") // gris
                .replace("&8", "\u001B[90m") // gris oscuro
                .replace("&m", "") // tachado NO funciona en consola
                + "\u001B[0m"; // reset SIEMPRE
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        availableMaterials.clear();

        for (String matName : getConfig().getStringList("render-material")) {
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                availableMaterials.add(mat);
            } else {
                getLogger().warning("Material inválido en config: " + matName);
            }
        }

        if (!availableMaterials.isEmpty()) {
            DigitRenderer.setDigitMaterial(availableMaterials.get(0));
        } else {
            getLogger().warning("No hay materiales válidos en config.yml");
        }

        this.sqliteManager = new SQLiteManager(this);
        this.timerManager = new TimerManager(this);
        this.editorManager = new EditorManager(this);

        getCommand("timer").setExecutor(new TimerCommand(this));
        getServer().getPluginManager().registerEvents(new TimerListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.timerplugin.commands.ChatInputListener(this), this);

        // Load existing timer if any
        sqliteManager.loadFirstTimer().ifPresent(timer -> {
            if (timer.getEndTime() > System.currentTimeMillis()) {
                timerManager.start(timer);
            } else {
                timerManager.start(timer); 
            }
        });

        getLogger().info(color("&4&m----------------------"));
        getLogger().info(color("&c     TimerInGame"));
        getLogger().info(color("&4&m----------------------"));
        getLogger().info(color("&cVersion: &f" + getDescription().getVersion()));
        getLogger().info(color("&cAuthor: &fDevAdvvy"));
        getLogger().info(color("&cGithub: &fhttps://github.com/DevAdvvy"));
        getLogger().info(color("&4&m----------------------"));
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null) {
            sqliteManager.close();
        }
        getLogger().info("TimerPlugin deshabilitado.");
    }

    public void reloadPluginConfig() {
        reloadConfig();

        availableMaterials.clear();

        for (String matName : getConfig().getStringList("render-material")) {
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                availableMaterials.add(mat);
            }
        }

        if (!availableMaterials.isEmpty()) {
            DigitRenderer.setDigitMaterial(availableMaterials.get(0));
        }
    }

    public SQLiteManager getSqliteManager() {
        return sqliteManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public EditorManager getEditorManager() {
        return editorManager;
    }

    public List<Material> getAvailableMaterials() {
        return availableMaterials;
    }
}
