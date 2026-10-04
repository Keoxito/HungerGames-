package com.hungergames.arena;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class ArenaManager {

    private final HungerGamesPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfigManager().getArenasConfig();
        if (config.getConfigurationSection("arenas") == null) return;
        
        for (String name : config.getConfigurationSection("arenas").getKeys(false)) {
            try {
                Arena arena = Arena.load(name, config);
                arenas.put(name.toLowerCase(), arena);
                plugin.getLogger().info("Arena cargada: " + name);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error cargando arena: " + name, e);
            }
        }
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfigManager().getArenasConfig();
        for (Arena arena : arenas.values()) {
            arena.save(config);
        }
        plugin.getConfigManager().saveArenas();
    }

    public Arena createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) return null;
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    public boolean deleteArena(String name) {
        Arena arena = arenas.remove(name.toLowerCase());
        if (arena != null) {
            saveArenas();
            return true;
        }
        return false;
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name.toLowerCase()));
    }

    public Arena getArenaByLocation(org.bukkit.Location loc) {
        for (Arena arena : arenas.values()) {
            if (arena.isInArena(loc)) return arena;
        }
        return null;
    }

    public Map<String, Arena> getArenas() {
        return new HashMap<>(arenas);
    }

    public boolean hasArena(String name) {
        return arenas.containsKey(name.toLowerCase());
    }
}