package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class FireEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public FireEvent(HungerGamesPlugin plugin) {
        super("fire_storm", "Tormenta de Fuego", "Llueve fuego del cielo en una zona", EventType.ENVIRONMENTAL, 2, 240);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.fire-title", "event.fire-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.fire-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        int fireCount = 15 + random.nextInt(10);
        
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= fireCount) {
                    cancel();
                    return;
                }
                
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = random.nextDouble() * arena.getRadius() * 0.7;
                double x = center.getX() + Math.cos(angle) * distance;
                double z = center.getZ() + Math.sin(angle) * distance;
                int y = world.getHighestBlockYAt((int) x, (int) z) + 1;
                
                Location fireLoc = new Location(world, x, y, z);
                
                // Crear fuego en área 3x3
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block block = fireLoc.clone().add(dx, 0, dz).getBlock();
                        if (block.getType() == Material.AIR || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.TALL_GRASS) {
                            block.setType(Material.FIRE);
                        }
                    }
                }
                
                // Efectos visuales
                world.spawnParticle(Particle.FLAME, fireLoc, 20, 1, 1, 1, 0.1);
                world.spawnParticle(Particle.LAVA, fireLoc, 10, 1, 1, 1, 0.05);
                world.playSound(fireLoc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}