package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class SupplyDropEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public SupplyDropEvent(HungerGamesPlugin plugin) {
        super("supply_drop", "Drop de Suministros", "Cajas de suministros caen del cielo", EventType.SUPPLY_DROP, 2, 120);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.supply-title", "event.supply-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.supply-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        int dropCount = 3 + random.nextInt(3); // 3-5 drops
        
        for (int i = 0; i < dropCount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawnSupplyDrop(world, center, arena.getRadius(), alivePlayers);
            }, i * 40L);
        }
    }

    private void spawnSupplyDrop(World world, Location center, int radius, List<Player> alivePlayers) {
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = random.nextDouble() * radius * 0.7;
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        int y = world.getHighestBlockYAt((int) x, (int) z) + 20;
        
        Location dropLoc = new Location(world, x, y, z);
        
        // Partículas de caída
        new BukkitRunnable() {
            double currentY = y;
            @Override
            public void run() {
                currentY -= 1.5;
                if (currentY <= dropLoc.getY() - 20) {
                    cancel();
                    createChestAtGround(world, dropLoc);
                    return;
                }
                
                Location particleLoc = new Location(world, x, currentY, z);
                world.spawnParticle(Particle.CLOUD, particleLoc, 10, 1, 1, 1, 0.1);
                world.spawnParticle(Particle.END_ROD, particleLoc, 5, 0.5, 0.5, 0.5, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sonido
        world.playSound(dropLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
    }

    private void createChestAtGround(World world, Location airLoc) {
        int groundY = world.getHighestBlockYAt(airLoc.getBlockX(), airLoc.getBlockZ());
        Location chestLoc = new Location(world, airLoc.getX(), groundY + 1, airLoc.getZ());
        
        Block block = chestLoc.getBlock();
        block.setType(Material.CHEST);
        
        if (block.getState() instanceof Chest chest) {
            chest.setCustomName("§b§lSuministros Aéreos");
            fillSupplyChest(chest);
            chest.update();
        }
        
        // Efecto de aterrizaje
        world.spawnParticle(Particle.EXPLOSION, chestLoc, 5, 1, 1, 1, 0);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, chestLoc, 30, 1, 1, 1, 0.2);
        world.playSound(chestLoc, Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    private void fillSupplyChest(Chest chest) {
        ItemStack[] supplyItems = {
            new ItemStack(Material.IRON_SWORD, 1),
            new ItemStack(Material.IRON_PICKAXE, 1),
            new ItemStack(Material.IRON_AXE, 1),
            new ItemStack(Material.GOLDEN_APPLE, 4),
            new ItemStack(Material.COOKED_BEEF, 16),
            new ItemStack(Material.ARROW, 32),
            new ItemStack(Material.BOW, 1),
            new ItemStack(Material.SHIELD, 1),
            new ItemStack(Material.IRON_INGOT, 8),
            new ItemStack(Material.GOLD_INGOT, 4),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 8),
            new ItemStack(Material.ENDER_PEARL, 4),
            new ItemStack(Material.FIREWORK_ROCKET, 8),
            new ItemStack(Material.POTION, 1), // Se puede mejorar con meta
            new ItemStack(Material.TNT, 4)
        };
        
        // Añadir 4-6 items aleatorios
        int count = 4 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            ItemStack item = supplyItems[random.nextInt(supplyItems.length)].clone();
            chest.getInventory().addItem(item);
        }
    }
}