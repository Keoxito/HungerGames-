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

public class FeastEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public FeastEvent(HungerGamesPlugin plugin) {
        super("feast", "Banquete", "Aparece un banquete en el centro con loot épico", EventType.SUPPLY_DROP, 3, 600);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.feast-title", "event.feast-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.feast-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter().clone();
        center.setY(world.getHighestBlockYAt(center) + 1);
        
        // Crear estructura del banquete
        createFeastStructure(world, center);
        
        // Llenar cofres con loot épico
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fillFeastChests(world, center);
            
            // Efectos visuales
            for (Player p : alivePlayers) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                p.spawnParticle(Particle.END_ROD, center, 50, 5, 5, 5, 0.2);
            }
        }, 20L);
    }

    private void createFeastStructure(World world, Location center) {
        // Plataforma base
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                Block block = center.clone().add(x, -1, z).getBlock();
                block.setType(Material.GOLD_BLOCK);
            }
        }
        
        // Columnas
        for (int x = -4; x <= 4; x += 8) {
            for (int z = -4; z <= 4; z += 8) {
                for (int y = 0; y < 4; y++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    block.setType(Material.QUARTZ_PILLAR);
                }
            }
        }
        
        // Cofres en las esquinas
        Location[] chestLocs = {
            center.clone().add(-3, 0, -3),
            center.clone().add(3, 0, -3),
            center.clone().add(-3, 0, 3),
            center.clone().add(3, 0, 3)
        };
        
        for (Location loc : chestLocs) {
            Block block = loc.getBlock();
            block.setType(Material.CHEST);
            if (block.getState() instanceof Chest chest) {
                chest.setCustomName("§6§lBanquete");
            }
        }
        
        // Cofre central grande
        Block centerBlock = center.clone().add(0, 0, 0).getBlock();
        centerBlock.setType(Material.ENDER_CHEST);
        if (centerBlock.getState() instanceof Chest chest) {
            chest.setCustomName("§d§lBanquete Supremo");
        }
        
        // Beacon en el centro
        Block beaconBlock = center.clone().add(0, 1, 0).getBlock();
        beaconBlock.setType(Material.BEACON);
    }

    private void fillFeastChests(World world, Location center) {
        Location[] chestLocs = {
            center.clone().add(-3, 0, -3),
            center.clone().add(3, 0, -3),
            center.clone().add(-3, 0, 3),
            center.clone().add(3, 0, 3),
            center.clone().add(0, 0, 0)
        };
        
        for (int i = 0; i < chestLocs.length; i++) {
            Block block = chestLocs[i].getBlock();
            if (block.getState() instanceof Chest chest) {
                chest.getInventory().clear();
                if (i == 4) {
                    // Cofre central - loot legendario
                    fillLegendaryChest(chest);
                } else {
                    // Cofres normales - loot épico
                    fillEpicChest(chest);
                }
                chest.update();
            }
        }
    }

    private void fillEpicChest(Chest chest) {
        ItemStack[] epicItems = {
            new ItemStack(Material.DIAMOND_SWORD, 1),
            new ItemStack(Material.DIAMOND_PICKAXE, 1),
            new ItemStack(Material.DIAMOND_ARMOR_TRIM_SMITHING_TEMPLATE, 1),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2),
            new ItemStack(Material.GOLDEN_APPLE, 8),
            new ItemStack(Material.DIAMOND, 16),
            new ItemStack(Material.EMERALD, 10),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 16),
            new ItemStack(Material.END_CRYSTAL, 2),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1)
        };
        
        for (ItemStack item : epicItems) {
            if (random.nextBoolean()) {
                chest.getInventory().addItem(item);
            }
        }
    }

    private void fillLegendaryChest(Chest chest) {
        ItemStack[] legendaryItems = {
            new ItemStack(Material.NETHERITE_SWORD, 1),
            new ItemStack(Material.NETHERITE_CHESTPLATE, 1),
            new ItemStack(Material.NETHERITE_LEGGINGS, 1),
            new ItemStack(Material.NETHERITE_BOOTS, 1),
            new ItemStack(Material.NETHERITE_HELMET, 1),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8),
            new ItemStack(Material.TOTEM_OF_UNDYING, 3),
            new ItemStack(Material.END_CRYSTAL, 8),
            new ItemStack(Material.NETHERITE_INGOT, 16),
            new ItemStack(Material.ELYTRA, 1)
        };
        
        for (ItemStack item : legendaryItems) {
            if (random.nextBoolean()) {
                chest.getInventory().addItem(item);
            }
        }
    }
}