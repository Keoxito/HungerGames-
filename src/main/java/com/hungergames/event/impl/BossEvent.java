package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class BossEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();
    private BossBar bossBar;

    public BossEvent(HungerGamesPlugin plugin) {
        super("boss", "Jefe de la Arena", "Un poderoso jefe aparece en el centro", EventType.BOSS, 4, 600);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (arena.getCenter() == null) return;
        
        setExecuted();
        plugin.getMessageManager().broadcastTitle("event.boss-title", "event.boss-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("event.boss-announce");
        
        World world = arena.getCenter().getWorld();
        Location center = arena.getCenter().clone();
        center.setY(world.getHighestBlockYAt(center) + 2);
        
        BossType type = BossType.values()[random.nextInt(BossType.values().length)];
        LivingEntity boss = type.spawn(world, center, alivePlayers.size());
        
        // Boss Bar
        bossBar = Bukkit.createBossBar(
            type.getDisplayName() + " §7- §c" + (int) boss.getHealth() + "/" + (int) boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
            BarColor.RED, BarStyle.SEGMENTED_10
        );
        bossBar.setVisible(true);
        for (Player p : alivePlayers) {
            bossBar.addPlayer(p);
        }
        
        // Actualizar boss bar
        new BukkitRunnable() {
            boolean deathHandled = false;
            
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    bossBar.removeAll();
                    bossBar = null;
                    
                    if (!deathHandled) {
                        deathHandled = true;
                        type.onDeath(world, boss.getLocation(), alivePlayers);
                    }
                    cancel();
                    return;
                }
                bossBar.setProgress(boss.getHealth() / boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                bossBar.setTitle(type.getDisplayName() + " §7- §c" + (int) boss.getHealth() + "/" + (int) boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }
        }.runTaskTimer(plugin, 0L, 5L);
        
        // Recompensa al morir
        boss.setMetadata("hg_boss", new org.bukkit.metadata.FixedMetadataValue(plugin, type));
        boss.setMetadata("hg_boss_reward", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
    }

    private enum BossType {
        WARDEN("Guardián Antiguo", EntityType.WARDEN, 500, 30, 0.25f) {
            @Override
            public void customize(LivingEntity entity, int playerCount) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
            }
            
            @Override
            public void onDeath(World world, Location loc, List<Player> players) {
                dropLoot(world, loc, players, new ItemStack[]{
                    new ItemStack(Material.NETHERITE_INGOT, 8),
                    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4),
                    new ItemStack(Material.TOTEM_OF_UNDYING, 2),
                    new ItemStack(Material.ELYTRA, 1)
                });
            }
        },
        WITHER("Wither del Capitolio", EntityType.WITHER, 400, 25, 0.3f) {
            @Override
            public void customize(LivingEntity entity, int playerCount) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(400 + playerCount * 50);
                entity.setHealth(400 + playerCount * 50);
            }
            
            @Override
            public void onDeath(World world, Location loc, List<Player> players) {
                dropLoot(world, loc, players, new ItemStack[]{
                    new ItemStack(Material.NETHER_STAR, 3),
                    new ItemStack(Material.BEACON, 1),
                    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 3),
                    new ItemStack(Material.NETHERITE_SCRAP, 8)
                });
            }
        },
        ENDER_DRAGON("Dragón del Fin", EntityType.ENDER_DRAGON, 300, 20, 0.4f) {
            @Override
            public void customize(LivingEntity entity, int playerCount) {
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300 + playerCount * 30);
                entity.setHealth(300 + playerCount * 30);
                entity.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(0.6);
            }
            
            @Override
            public void onDeath(World world, Location loc, List<Player> players) {
                dropLoot(world, loc, players, new ItemStack[]{
                    new ItemStack(Material.DRAGON_EGG, 1),
                    new ItemStack(Material.ELYTRA, 1),
                    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5),
                    new ItemStack(Material.DIAMOND, 32)
                });
            }
        },
        RAVAGER_KING("Rey Devastador", EntityType.RAVAGER, 600, 35, 0.35f) {
            @Override
            public void customize(LivingEntity entity, int playerCount) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(600 + playerCount * 100);
                entity.setHealth(600 + playerCount * 100);
                entity.setCustomName("§c§lRey Devastador");
            }
            
            @Override
            public void onDeath(World world, Location loc, List<Player> players) {
                dropLoot(world, loc, players, new ItemStack[]{
                    new ItemStack(Material.NETHERITE_INGOT, 12),
                    new ItemStack(Material.SADDLE, 1),
                    new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 6),
                    new ItemStack(Material.TOTEM_OF_UNDYING, 3)
                });
            }
        };

        private final String displayName;
        private final EntityType entityType;
        private final double baseHealth;
        private final double baseDamage;
        private final float speed;

        BossType(String displayName, EntityType entityType, double baseHealth, double baseDamage, float speed) {
            this.displayName = displayName;
            this.entityType = entityType;
            this.baseHealth = baseHealth;
            this.baseDamage = baseDamage;
            this.speed = speed;
        }

        public String getDisplayName() { return displayName; }
        public EntityType getEntityType() { return entityType; }
        public double getBaseHealth() { return baseHealth; }
        public double getBaseDamage() { return baseDamage; }
        public float getSpeed() { return speed; }

        public LivingEntity spawn(World world, Location loc, int playerCount) {
            LivingEntity entity = (LivingEntity) world.spawnEntity(loc, entityType);
            entity.setCustomName(displayName);
            entity.setCustomNameVisible(true);
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth + playerCount * 50);
            entity.setHealth(baseHealth + playerCount * 50);
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(baseDamage);
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100.0);
            entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);
            
            customize(entity, playerCount);
            
            // Target más cercano
            if (!world.getPlayers().isEmpty()) {
                Player nearest = world.getPlayers().stream()
                    .min((a, b) -> Double.compare(a.getLocation().distanceSquared(loc), b.getLocation().distanceSquared(loc)))
                    .orElse(null);
                if (nearest != null && entity instanceof PathfinderMob mob) {
                    mob.setTarget(nearest);
                }
            }
            
            return entity;
        }

        public abstract void customize(LivingEntity entity, int playerCount);
        public abstract void onDeath(World world, Location loc, List<Player> players);

        protected void dropLoot(World world, Location loc, List<Player> players, ItemStack[] items) {
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 3, 3, 3, 0.3);
            world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 2f, 1f);
            
            for (ItemStack item : items) {
                world.dropItemNaturally(loc, item);
            }
            
            // XP orbs
            world.spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(500));
        }
    }
    
    public BossBar getBossBar() { return bossBar; }
}