package com.hungergames.event;

import com.hungergames.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class GameEvent {

    private final String name;
    private final String displayName;
    private final String description;
    private final EventType type;
    private final int minPlayers;
    private final int cooldownSeconds;
    private long lastExecuted = 0;

    public GameEvent(String name, String displayName, String description, EventType type, int minPlayers, int cooldownSeconds) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.minPlayers = minPlayers;
        this.cooldownSeconds = cooldownSeconds;
    }

    public abstract void execute(Arena arena, List<Player> alivePlayers);
    
    public boolean canExecute(Arena arena, List<Player> alivePlayers) {
        if (alivePlayers.size() < minPlayers) return false;
        if (System.currentTimeMillis() - lastExecuted < cooldownSeconds * 1000L) return false;
        return checkConditions(arena, alivePlayers);
    }

    protected boolean checkConditions(Arena arena, List<Player> alivePlayers) {
        return true;
    }

    public void setExecuted() {
        this.lastExecuted = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public EventType getType() { return type; }
    public int getMinPlayers() { return minPlayers; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public long getLastExecuted() { return lastExecuted; }
    public long getRemainingCooldown() { 
        return Math.max(0, (lastExecuted + cooldownSeconds * 1000L - System.currentTimeMillis()) / 1000);
    }
}