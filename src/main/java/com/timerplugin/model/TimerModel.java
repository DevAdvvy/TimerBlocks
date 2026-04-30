package com.timerplugin.model;

import org.bukkit.Location;

public class TimerModel {
    private final String name;
    private final Location center;
    private long endTime;
    private boolean active;

    public TimerModel(String name, Location center, long endTime) {
        this.name = name;
        this.center = center;
        this.endTime = endTime;
        this.active = endTime > System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public Location getCenter() {
        return center;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.active = endTime > System.currentTimeMillis();
    }

    public long getRemainingTime() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
