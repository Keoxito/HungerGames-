package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class TrapEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public TrapEvent(HungerGamesPlugin plugin) {
        super("traps", "Trampas Mortales", "Trampas se activan aleatoriamente en la arena", EventType.TRAP, 2, 180);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null || alivePlayers.isEmpty()) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.trap-title", "event.trap-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.trap-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        
        TrapType type = TrapType.values()[random.nextInt(TrapType.values().length)];
        type.execute(plugin, world, center, arena.getRadius(), alivePlayers);
    }

    private enum TrapType {
        EXPLOSIVE_FLOOR("Suelo Explosivo") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks > 60) { // 3 segundos
                            cancel();
                            return;
                        }
                        
                        for (Player p : players) {
                            if (random.nextDouble() < 0.1) { // 10% chance por tick
                                Location loc = p.getLocation().clone().subtract(0, 1, 0);
                                Block block = loc.getBlock();
                                if (block.getType().isSolid()) {
                                    block.setType(Material.TNT);
                                    world.spawnEntity(loc.add(0.5, 0.5, 0.5), EntityType.PRIMED_TNT);
                                }
                            }
                        }
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        },
        POISON_GAS("Gas Venenoso") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks > 200) { // 10 segundos
                            cancel();
                            return;
                        }
                        
                        for (Player p : players) {
                            double dist = p.getLocation().distance(center);
                            if (dist < radius * 0.5) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1, false, false, true));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0, false, false, true));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0, false, false, true));
                            }
                            
                            // Partículas de gas
                            if (ticks % 5 == 0) {
                                p.spawnParticle(Particle.ENTITY_EFFECT, p.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1, 
                                    org.bukkit.potion.PotionEffectType.POISON.createEffect(1, 0).getColor().asRGB());
                            }
                        }
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        },
        SAND_TRAP("Trampa de Arena") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                for (Player p : players) {
                    if (random.nextDouble() < 0.4) { // 40% de jugadores afectados
                        Location loc = p.getLocation().clone().subtract(0, 1, 0);
                        // Convertir bloques alrededor en arena que cae
                        for (int x = -2; x <= 2; x++) {
                            for (int z = -2; z <= 2; z++) {
                                Block block = loc.clone().add(x, 0, z).getBlock();
                                if (block.getType() != Material.AIR && !block.getType().equals(Material.BEDROCK)) {
                                    world.spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), Material.SAND.createBlockData());
                                }
                            }
                        }
                    }
                }
            }
        },
        VOID_HOLES("Agujeros al Vacío") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                int holes = 5 + random.nextInt(5);
                for (int i = 0; i < holes; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * radius * 0.6;
                    double x = center.getX() + Math.cos(angle) * distance;
                    double z = center.getZ() + Math.sin(angle) * distance;
                    
                    // Hacer un agujero 3x3 hasta el vacío
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            int bx = (int) (x + dx);
                            int bz = (int) (z + dz);
                            for (int y = world.getHighestBlockYAt(bx, bz); y > world.getMinHeight(); y--) {
                                Block block = world.getBlockAt(bx, y, bz);
                                if (block.getType() != Material.BEDROCK && block.getType() != Material.AIR) {
                                    block.setType(Material.AIR);
                                }
                            }
                        }
                    }
                    
                    // Partículas de portal
                    world.spawnParticle(Particle.PORTAL, new Location(world, x, 64, z), 50, 2, 32, 2, 0.1);
                }
            }
        },
        ANVIL_RAIN("Lluvia de Yunque") {
            @Override
            public void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players) {
                new BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        if (count > 20) {
                            cancel();
                            return;
                        }
                        
                        for (Player p : players) {
                            if (random.nextDouble() < 0.3) {
                                Location anvilLoc = p.getLocation().add(
                                    (random.nextDouble() - 0.5) * 6, 20, (random.nextDouble() - 0.5) * 6);
                                world.spawnFallingBlock(anvilLoc, Material.ANVIL.createBlockData());
                            }
                        }
                        count++;
                    }
                }.runTaskTimer(plugin, 0L, 10L);
            }
        };

        private final String displayName;

        TrapType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
        public abstract void execute(HungerGamesPlugin plugin, World world, Location center, int radius, List<Player> players);
    }
}