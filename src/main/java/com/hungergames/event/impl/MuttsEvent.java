package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class MuttsEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public MuttsEvent(HungerGamesPlugin plugin) {
        super("mutts", "Mutaciones", "Libera mutaciones genéticas en la arena", EventType.MUTTATION, 2, 300);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.mutts-title", "event.mutts-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.mutts-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter();
        int muttCount = Math.min(alivePlayers.size() * 2, 20);
        
        for (int i = 0; i < muttCount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawnMutt(world, center, alivePlayers);
            }, i * 10L);
        }
        
        // Sonido global
        for (Player p : alivePlayers) {
            p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 0.8f);
        }
    }

    private void spawnMutt(World world, Location center, List<Player> alivePlayers) {
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = arena.getRadius() * 0.8 + random.nextDouble() * arena.getRadius() * 0.2;
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        int y = world.getHighestBlockYAt((int) x, (int) z) + 1;
        
        Location spawnLoc = new Location(world, x, y, z);
        
        // Tipo de muto aleatorio
        MutType type = MutType.values()[random.nextInt(MutType.values().length)];
        Entity mutt = type.spawn(world, spawnLoc);
        
        if (mutt instanceof LivingEntity living) {
            // Configurar muto
            living.setCustomName(type.getDisplayName());
            living.setCustomNameVisible(true);
            living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(type.getHealth());
            living.setHealth(type.getHealth());
            living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(type.getDamage());
            living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(type.getSpeed());
            living.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(64.0);
            
            // Efectos
            living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, type.getSpeedAmplifier()));
            if (type.isStrong()) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
            }
            
            // Target aleatorio
            if (!alivePlayers.isEmpty()) {
                Player target = alivePlayers.get(random.nextInt(alivePlayers.size()));
                if (mutt instanceof PathfinderMob mob) {
                    mob.setTarget(target);
                }
            }
            
            // Marcar como muto del evento
            living.setMetadata("hg_mutt", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            living.setMetadata("hg_mutt_type", new org.bukkit.metadata.FixedMetadataValue(plugin, type.name()));
        }
    }

    private enum MutType {
        WOLF("Muto Lobo", EntityType.WOLF, 30, 6, 0.35f, 1, false),
        SPIDER("Muto Araña", EntityType.CAVE_SPIDER, 25, 5, 0.4f, 1, false),
        ZOMBIE("Muto Zombi", EntityType.ZOMBIE, 40, 8, 0.3f, 0, true),
        RAVAGER("Muto Devastador", EntityType.RAVAGER, 100, 15, 0.35f, 2, true),
        VINDICATOR("Muto Vindicador", EntityType.VINDICATOR, 50, 12, 0.35f, 1, true),
        HUSK("Muto Husk", EntityType.HUSK, 35, 7, 0.3f, 1, false),
        STRAY("Muto Stray", EntityType.STRAY, 30, 6, 0.35f, 1, false),
        BOGEYMAN("Muto Bogeyman", EntityType.BOGED, 45, 8, 0.4f, 1, true);

        private final String displayName;
        private final EntityType entityType;
        private final double health;
        private final double damage;
        private final float speed;
        private final int speedAmplifier;
        private final boolean strong;

        MutType(String displayName, EntityType entityType, double health, double damage, float speed, int speedAmplifier, boolean strong) {
            this.displayName = displayName;
            this.entityType = entityType;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.speedAmplifier = speedAmplifier;
            this.strong = strong;
        }

        public String getDisplayName() { return displayName; }
        public EntityType getEntityType() { return entityType; }
        public double getHealth() { return health; }
        public double getDamage() { return damage; }
        public float getSpeed() { return speed; }
        public int getSpeedAmplifier() { return speedAmplifier; }
        public boolean isStrong() { return strong; }

        public Entity spawn(World world, Location loc) {
            return world.spawnEntity(loc, entityType);
        }
    }
}