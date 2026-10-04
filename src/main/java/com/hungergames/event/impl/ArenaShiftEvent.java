package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class ArenaShiftEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public ArenaShiftEvent(HungerGamesPlugin plugin) {
        super("arena_shift", "Cambio de Arena", "La arena se transforma drásticamente", EventType.ARENA_CHANGE, 3, 480);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.shift-title", "event.shift-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.shift-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        
        ShiftType type = ShiftType.values()[random.nextInt(ShiftType.values().length)];
        type.execute(plugin, world, center, arena.getRadius(), alivePlayers);
    }

    private enum ShiftType {
        LAVA_FLOOD("Inundación de Lava") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                // La lava sube desde el fondo
                new org.bukkit.scheduler.BukkitRunnable() {
                    int level = 0;
                    @Override
                    public void run() {
                        if (level > 10) {
                            cancel();
                            return;
                        }
                        
                        for (int x = -radius; x <= radius; x += 2) {
                            for (int z = -radius; z <= radius; z += 2) {
                                Block block = center.clone().add(x, center.getBlockY() - 10 + level, z).getBlock();
                                if (block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                                    block.setType(Material.LAVA);
                                }
                            }
                        }
                        
                        world.playSound(center, Sound.BLOCK_LAVA_POP, 1f, 0.5f);
                        level++;
                    }
                }.runTaskTimer(plugin, 0L, 40L);
            }
        },
        FLOOD("Inundación") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                new org.bukkit.scheduler.BukkitRunnable() {
                    int level = 0;
                    @Override
                    public void run() {
                        if (level > 15) {
                            cancel();
                            return;
                        }
                        
                        for (int x = -radius; x <= radius; x += 2) {
                            for (int z = -radius; z <= radius; z += 2) {
                                Block block = center.clone().add(x, center.getBlockY() - 5 + level, z).getBlock();
                                if (block.getType() == Material.AIR) {
                                    block.setType(Material.WATER);
                                }
                            }
                        }
                        
                        world.playSound(center, Sound.BLOCK_WATER_AMBIENT, 1f, 0.5f);
                        level++;
                    }
                }.runTaskTimer(plugin, 0L, 40L);
            }
        },
        EARTHQUAKE("Terremoto") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks > 100) {
                            cancel();
                            return;
                        }
                        
                        for (Player p : players) {
                            // Efecto de temblor en pantalla
                            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
                            if (ticks % 10 == 0) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 0));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                            }
                        }
                        
                        // Bloques caen
                        if (ticks % 5 == 0) {
                            for (int i = 0; i < 5; i++) {
                                double angle = random.nextDouble() * Math.PI * 2;
                                double distance = random.nextDouble() * radius * 0.8;
                                double x = center.getX() + Math.cos(angle) * distance;
                                double z = center.getZ() + Math.sin(angle) * distance;
                                int y = world.getHighestBlockYAt((int) x, (int) z) + 5;
                                
                                Location fallLoc = new Location(world, x, y, z);
                                world.spawnFallingBlock(fallLoc, Material.SAND.createBlockData());
                            }
                        }
                        
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 2L);
            }
        },
        VOID_EXPANSION("Expansión del Vacío") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                // El borde se contrae rápido hacia el centro
                world.getWorldBorder().setCenter(center.getX(), center.getZ());
                world.getWorldBorder().setSize(radius * 2);
                world.getWorldBorder().setSize(10, 60); // Se contrae a 10 bloques en 60 segundos
                world.getWorldBorder().setWarningDistance(5);
                world.getWorldBorder().setDamageAmount(2);
                world.getWorldBorder().setDamageBuffer(0);
                
                for (Player p : players) {
                    p.sendTitle("§c§lVACÍO EXPANDIÉNDOSE", "§7El borde se cierra rápidamente", 10, 70, 20);
                }
            }
        };

        private final String displayName;

        ShiftType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
        public abstract void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players);
    }
}