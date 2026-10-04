package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class StormEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public StormEvent(HungerGamesPlugin plugin) {
        super("storm", "Tormenta Mortal", "Una tormenta con rayos cae sobre la arena", EventType.ENVIRONMENTAL, 2, 180);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.storm-title", "event.storm-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.storm-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        
        // Cambiar clima
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(6000); // 5 minutos
        
        // Rayos cada pocos segundos
        new org.bukkit.scheduler.BukkitRunnable() {
            int strikes = 0;
            @Override
            public void run() {
                if (strikes >= 30) {
                    world.setStorm(false);
                    world.setThundering(false);
                    cancel();
                    return;
                }
                
                for (Player p : alivePlayers) {
                    if (random.nextDouble() < 0.3) { // 30% chance por jugador
                        Location strikeLoc = p.getLocation().add(
                            (random.nextDouble() - 0.5) * 10, 0, (random.nextDouble() - 0.5) * 10);
                        world.strikeLightningEffect(strikeLoc);
                    }
                }
                
                // Rayos aleatorios en la arena
                if (random.nextDouble() < 0.5) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * arena.getRadius() * 0.8;
                    double x = center.getX() + Math.cos(angle) * distance;
                    double z = center.getZ() + Math.sin(angle) * distance;
                    int y = world.getHighestBlockYAt((int) x, (int) z);
                    world.strikeLightningEffect(new Location(world, x, y, z));
                }
                
                strikes++;
            }
        }.runTaskTimer(plugin, 20L, 20L); // Cada segundo
    }
}