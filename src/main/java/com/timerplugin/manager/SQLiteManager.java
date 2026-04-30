package com.timerplugin.manager;

import com.timerplugin.Main;
import com.timerplugin.model.TimerModel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.Optional;

public class SQLiteManager {
    private final Main plugin;
    private Connection connection;

    public SQLiteManager(Main plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/timers.db");
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS timers (" +
                    "name TEXT PRIMARY KEY, " +
                    "world TEXT, " +
                    "x INTEGER, y INTEGER, z INTEGER, " +
                    "end_time BIGINT)");
        }
    }

    public boolean timerExists(String name) {
        String sql = "SELECT 1 FROM timers WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveTimer(TimerModel timer) {
        String sql = "INSERT OR REPLACE INTO timers (name, world, x, y, z, end_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, timer.getName());
            pstmt.setString(2, timer.getCenter().getWorld().getName());
            pstmt.setInt(3, timer.getCenter().getBlockX());
            pstmt.setInt(4, timer.getCenter().getBlockY());
            pstmt.setInt(5, timer.getCenter().getBlockZ());
            pstmt.setLong(6, timer.getEndTime());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTimer(String name) {
        String sql = "DELETE FROM timers WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<TimerModel> loadFirstTimer() {
        String sql = "SELECT * FROM timers LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String name = rs.getString("name");
                String worldName = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                long endTime = rs.getLong("end_time");

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location center = new Location(world, x, y, z);
                    return Optional.of(new TimerModel(name, center, endTime));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
