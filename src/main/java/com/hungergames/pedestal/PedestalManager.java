package com.hungergames.pedestal;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PedestalManager {

    private final HungerGamesPlugin plugin;
    private final Map<Location, Pedestal> activePedestals = new ConcurrentHashMap<>();
    private BukkitTask riseTask;
    
    private double riseSpeed = 0.2; // bloques por tick
    private double maxHeight = 20;
    private int riseDuration = 100; // ticks (5 segundos)

    public PedestalManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        riseSpeed = plugin.getConfigManager().getConfig().getDouble("pedestal.rise-speed", 0.2);
        maxHeight = plugin.getConfigManager().getConfig().getDouble("pedestal.max-height", 20);
        riseDuration = plugin.getConfigManager().getConfig().getInt("pedestal.rise-duration", 100);
    }

    public void startRising(Arena arena) {
        List<Location> pedestalLocations = arena.getPedestalLocations();
        if (pedestalLocations.isEmpty()) return;
        
        plugin.getMessageManager().broadcast("pedestal.rising-start");
        
        for (Location loc : pedestalLocations) {
            createPedestal(loc);
        }
        
        // Task de subida
        riseTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                double progress = (double) ticks / riseDuration;
                
                for (Pedestal pedestal : activePedestals.values()) {
                    pedestal.updateHeight(progress);
                }
                
                if (ticks >= riseDuration) {
                    finishRising();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void createPedestal(Location baseLoc) {
        World world = baseLoc.getWorld();
        Location center = baseLoc.clone().add(0.5, 0, 0.5);
        
        // Crear base del pedestal (bloques)
        Block baseBlock = center.clone().subtract(0.5, 0, 0.5).getBlock();
        baseBlock.setType(Material.QUARTZ_BLOCK);
        
        // Armor stand como plataforma
        ArmorStand stand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setCustomNameVisible(false);
        stand.addScoreboardTag("hg_pedestal");
        
        // Colocar al jugador encima si hay uno
        Player player = findPlayerAt(baseLoc);
        if (player != null) {
            player.teleport(center.clone().add(0, 2, 0));
            player.addPassenger(stand);
        }
        
        // Partículas iniciales
        world.spawnParticle(Particle.END_ROD, center.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f);
        
        Pedestal pedestal = new Pedestal(center, stand, player, maxHeight);
        activePedestals.put(center, pedestal);
    }

    private Player findPlayerAt(Location loc) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getLocation().distanceSquared(loc) < 4) {
                return player;
            }
        }
        return null;
    }

    private void finishRising() {
        for (Pedestal pedestal : activePedestals.values()) {
            pedestal.finish();
        }
        activePedestals.clear();
        
        if (riseTask != null) {
            riseTask.cancel();
            riseTask = null;
        }
    }

    public void startPedestalTask() {
        // Task periódica para verificar pedestales
        new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar pedestales activos
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void cleanup() {
        for (Pedestal pedestal : activePedestals.values()) {
            pedestal.remove();
        }
        activePedestals.clear();
        
        if (riseTask != null) {
            riseTask.cancel();
            riseTask = null;
        }
    }

    public void createPedestalAt(Location loc) {
        createPedestal(loc);
    }

    private class Pedestal {
        private final Location baseLocation;
        private final ArmorStand stand;
        private final Player player;
        private final double maxHeight;
        private double currentHeight = 0;
        private boolean finished = false;

        public Pedestal(Location base, ArmorStand stand, Player player, double maxHeight) {
            this.baseLocation = base;
            this.stand = stand;
            this.player = player;
            this.maxHeight = maxHeight;
        }

        public void updateHeight(double progress) {
            if (finished) return;
            
            currentHeight = maxHeight * progress;
            Location newLoc = baseLocation.clone().add(0, currentHeight, 0);
            stand.teleport(newLoc);
            
            if (player != null && player.isOnline()) {
                player.teleport(newLoc.clone().add(0, 1, 0));
            }
            
            // Partículas durante la subida
            if (progress % 0.1 < 0.02) {
                baseLocation.getWorld().spawnParticle(Particle.END_ROD, newLoc, 5, 0.5, 0.1, 0.5, 0.05);
            }
        }

        public void finish() {
            finished = true;
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
            
            if (player != null && player.isOnline()) {
                Location finalLoc = baseLocation.clone().add(0, maxHeight + 1, 0);
                player.teleport(finalLoc);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            
            // Efecto final
            baseLocation.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, baseLocation.clone().add(0, maxHeight + 1, 0), 30, 1, 1, 1, 0.2);
            baseLocation.getWorld().playSound(baseLocation, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        }

        public void remove() {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
    }
}