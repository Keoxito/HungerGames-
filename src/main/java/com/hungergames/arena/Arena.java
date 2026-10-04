package com.hungergames.arena;

import com.hungergames.util.ConfigManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena {

    private final String name;
    private Location lobbySpawn;
    private Location center;
    private int radius;
    private int height;
    private List<Location> pedestalLocations;
    private List<Location> spawnPoints;
    private List<Location> chestLocations;
    private Location deathmatchCenter;
    private int deathmatchRadius;
    private boolean enabled;
    private String worldName;

    public Arena(String name) {
        this.name = name;
        this.pedestalLocations = new ArrayList<>();
        this.spawnPoints = new ArrayList<>();
        this.chestLocations = new ArrayList<>();
        this.enabled = true;
        this.radius = 200;
        this.height = 100;
        this.deathmatchRadius = 50;
    }

    public void save(FileConfiguration config) {
        String path = "arenas." + name + ".";
        
        if (lobbySpawn != null) {
            config.set(path + "lobby", serializeLocation(lobbySpawn));
        }
        if (center != null) {
            config.set(path + "center", serializeLocation(center));
        }
        config.set(path + "radius", radius);
        config.set(path + "height", height);
        config.set(path + "enabled", enabled);
        config.set(path + "world", worldName);
        
        if (deathmatchCenter != null) {
            config.set(path + "deathmatch.center", serializeLocation(deathmatchCenter));
        }
        config.set(path + "deathmatch.radius", deathmatchRadius);

        // Pedestales
        List<String> pedestalStrings = new ArrayList<>();
        for (Location loc : pedestalLocations) {
            pedestalStrings.add(serializeLocation(loc));
        }
        config.set(path + "pedestals", pedestalStrings);

        // Spawn points
        List<String> spawnStrings = new ArrayList<>();
        for (Location loc : spawnPoints) {
            spawnStrings.add(serializeLocation(loc));
        }
        config.set(path + "spawns", spawnStrings);

        // Cofres
        List<String> chestStrings = new ArrayList<>();
        for (Location loc : chestLocations) {
            chestStrings.add(serializeLocation(loc));
        }
        config.set(path + "chests", chestStrings);
    }

    public static Arena load(String name, FileConfiguration config) {
        String path = "arenas." + name + ".";
        Arena arena = new Arena(name);
        
        arena.lobbySpawn = deserializeLocation(config.getString(path + "lobby"));
        arena.center = deserializeLocation(config.getString(path + "center"));
        arena.radius = config.getInt(path + "radius", 200);
        arena.height = config.getInt(path + "height", 100);
        arena.enabled = config.getBoolean(path + "enabled", true);
        arena.worldName = config.getString(path + "world", "world");
        
        arena.deathmatchCenter = deserializeLocation(config.getString(path + "deathmatch.center"));
        arena.deathmatchRadius = config.getInt(path + "deathmatch.radius", 50);

        // Pedestales
        List<String> pedestalStrings = config.getStringList(path + "pedestals");
        for (String s : pedestalStrings) {
            Location loc = deserializeLocation(s);
            if (loc != null) arena.pedestalLocations.add(loc);
        }

        // Spawn points
        List<String> spawnStrings = config.getStringList(path + "spawns");
        for (String s : spawnStrings) {
            Location loc = deserializeLocation(s);
            if (loc != null) arena.spawnPoints.add(loc);
        }

        // Cofres
        List<String> chestStrings = config.getStringList(path + "chests");
        for (String s : chestStrings) {
            Location loc = deserializeLocation(s);
            if (loc != null) arena.chestLocations.add(loc);
        }

        return arena;
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private static Location deserializeLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 6) return null;
        
        World world = org.bukkit.Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        
        return new Location(world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5]));
    }

    // Getters y Setters
    public String getName() { return name; }
    public Location getLobbySpawn() { return lobbySpawn; }
    public void setLobbySpawn(Location lobbySpawn) { this.lobbySpawn = lobbySpawn; }
    public Location getCenter() { return center; }
    public void setCenter(Location center) { this.center = center; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public List<Location> getPedestalLocations() { return new ArrayList<>(pedestalLocations); }
    public void addPedestalLocation(Location loc) { this.pedestalLocations.add(loc); }
    public void removePedestalLocation(int index) { if (index >= 0 && index < pedestalLocations.size()) pedestalLocations.remove(index); }
    public List<Location> getSpawnPoints() { return new ArrayList<>(spawnPoints); }
    public void addSpawnPoint(Location loc) { this.spawnPoints.add(loc); }
    public void removeSpawnPoint(int index) { if (index >= 0 && index < spawnPoints.size()) spawnPoints.remove(index); }
    public List<Location> getChestLocations() { return new ArrayList<>(chestLocations); }
    public void addChestLocation(Location loc) { this.chestLocations.add(loc); }
    public void removeChestLocation(int index) { if (index >= 0 && index < chestLocations.size()) chestLocations.remove(index); }
    public Location getDeathmatchCenter() { return deathmatchCenter; }
    public void setDeathmatchCenter(Location deathmatchCenter) { this.deathmatchCenter = deathmatchCenter; }
    public int getDeathmatchRadius() { return deathmatchRadius; }
    public void setDeathmatchRadius(int deathmatchRadius) { this.deathmatchRadius = deathmatchRadius; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    
    public Location getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) return center;
        return spawnPoints.get((int) (Math.random() * spawnPoints.size()));
    }
    
    public boolean isInArena(Location loc) {
        if (center == null || !loc.getWorld().equals(center.getWorld())) return false;
        double dx = loc.getX() - center.getX();
        double dz = loc.getZ() - center.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= radius;
    }
    
    public boolean isInDeathmatch(Location loc) {
        if (deathmatchCenter == null || !loc.getWorld().equals(deathmatchCenter.getWorld())) return false;
        double dx = loc.getX() - deathmatchCenter.getX();
        double dz = loc.getZ() - deathmatchCenter.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= deathmatchRadius;
    }
}