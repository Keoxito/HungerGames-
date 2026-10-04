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

public class PlayerEffectEvent extends GameEvent {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    public PlayerEffectEvent(HungerGamesPlugin plugin) {
        super("player_effects", "Efectos Globales", "Efectos aleatorios afectan a todos los tributos", EventType.PLAYER_EFFECT, 2, 120);
        this.plugin = plugin;
    }

    @Override
    public void execute(Arena arena, List<Player> alivePlayers) {
        if (alivePlayers.isEmpty()) return;
        
        setExecuted();
        
        EffectType type = EffectType.values()[random.nextInt(EffectType.values().length)];
        
        plugin.getMessageManager().broadcastTitle("event.effect-title", "event.effect-subtitle", 10, 70, 20, "effect", type.getDisplayName());
        plugin.getMessageManager().broadcast("event.effect-announce", "effect", type.getDisplayName());
        
        type.apply(alivePlayers);
    }

    private enum EffectType {
        HUNGER("Hambruna", 60) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.setFoodLevel(Math.max(0, p.getFoodLevel() - 6));
                    p.setSaturation(0);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1200, 1, false, false, true));
                }
            }
        },
        BLINDNESS("Ceguera Temporal", 15) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0, false, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 300, 0, false, false, true));
                }
            }
        },
        NAUSEA("Mareas", 20) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 400, 1, false, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 0, false, false, true));
                }
            }
        },
        GLOWING("Resplandor", 30) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0, false, false, true));
                }
            }
        },
        WEAKNESS("Debilidad", 30) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1, false, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 1, false, false, true));
                }
            }
        },
        REGENERATION("Regeneración", 30) {
            @Override
            public void apply(List<Player> players) {
                for (Player p : players) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1, false, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 600, 1, false, false, true));
                }
            }
        },
        SWAP_POSITIONS("Intercambio de Posiciones", 1) {
            @Override
            public void apply(List<Player> players) {
                if (players.size() < 2) return;
                for (int i = 0; i < players.size() / 2; i++) {
                    Player p1 = players.get(i);
                    Player p2 = players.get(players.size() - 1 - i);
                    Location loc1 = p1.getLocation();
                    Location loc2 = p2.getLocation();
                    p1.teleport(loc2);
                    p2.teleport(loc1);
                    p1.playSound(p1.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    p2.playSound(p2.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                }
            }
        };

        private final String displayName;
        private final int durationSeconds;

        EffectType(String displayName, int durationSeconds) {
            this.displayName = displayName;
            this.durationSeconds = durationSeconds;
        }

        public String getDisplayName() { return displayName; }
        public int getDurationSeconds() { return durationSeconds; }
        public abstract void apply(List<Player> players);
    }
}