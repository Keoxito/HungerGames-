package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.event.impl.BossEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityListener implements Listener {

    private final HungerGamesPlugin plugin;

    public EntityListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Mutt death - dar recompensas
        if (entity.hasMetadata("hg_mutt")) {
            event.getDrops().clear();
            
            // Drop aleatorio
            if (Math.random() < 0.3) {
                String[] drops = {"ROTTEN_FLESH", "BONE", "SPIDER_EYE", "STRING", "LEATHER"};
                String drop = drops[(int) (Math.random() * drops.length)];
                entity.getWorld().dropItemNaturally(entity.getLocation(), new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(drop), 1 + (int)(Math.random() * 3)));
            }
            
            // XP
            event.setDroppedExp(5 + (int)(Math.random() * 10));
        }
    }
}