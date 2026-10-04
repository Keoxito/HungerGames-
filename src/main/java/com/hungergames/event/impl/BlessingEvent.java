package com.hungergames.event.impl;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventType;
import com.hungergames.event.GameEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class BlessingEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public BlessingEvent(HungerGamesPlugin plugin) {
        super("blessing", "Bendición del Capitolio", "Un tributo recibe una bendición poderosa", EventType.BLESSING, 3, 300);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (alivePlayers.isEmpty()) return;
        
        setExecuted();
        
        // Elegir un jugador aleatorio (prioridad a los con menos kills)
        Player blessed = alivePlayers.stream()
            .min((a, b) -> Integer.compare(
                plugin.getGameManager().getPlayers().get(a.getUniqueId()).getKills(),
                plugin.getGameManager().getPlayers().get(b.getUniqueId()).getKills()))
            .orElse(alivePlayers.get(0));
        
        BlessingType type = BlessingType.values()[random.nextInt(BlessingType.values().length)];
        
        plugin.getMessageManager().broadcastTitle("event.blessing-title", "event.blessing-subtitle", 10, 70, 20, "player", blessed.getName(), "blessing", type.getDisplayName());
        plugin.getMessageManager().broadcast("event.blessing-announce", "player", blessed.getName(), "blessing", type.getDisplayName());
        
        type.apply(blessed);
        
        // Efectos visuales
        blessed.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, blessed.getLocation(), 50, 1, 1, 1, 0.2);
        blessed.getWorld().spawnParticle(Particle.END_ROD, blessed.getLocation(), 30, 1, 2, 1, 0.1);
        blessed.playSound(blessed.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    private enum BlessingType {
        INVINCIBILITY("Invencibilidad", 30) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 4, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0, false, false, true));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
                player.setHealth(40);
            }
        },
        SPEED_DEMON("Demonio de la Velocidad", 45) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 900, 2, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 900, 2, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 900, 0, false, false, true));
            }
        },
        BERSERKER("Furia Berserker", 30) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 2, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1, false, false, true));
            }
        },
        PHANTOM("Fantasma", 60) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1200, 0, false, false, true));
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
            }
        },
        TANK("Tanque", 60) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 1, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 0, false, false, true));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(60);
                player.setHealth(60);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1200, 1, false, false, true));
            }
        },
        ARCHER("Arquero Supremo", 60) {
            @Override
            public void apply(Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POWER, 1200, 1, false, false, true));
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(10);
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.ARROW, 64));
            }
        };

        private final String displayName;
        private final int durationSeconds;

        BlessingType(String displayName, int durationSeconds) {
            this.displayName = displayName;
            this.durationSeconds = durationSeconds;
        }

        public String getDisplayName() { return displayName; }
        public int getDurationSeconds() { return durationSeconds; }
        public abstract void apply(Player player);
    }
}