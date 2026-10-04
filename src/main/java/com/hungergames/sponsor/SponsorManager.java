package com.hungergames.sponsor;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SponsorManager {

    private final HungerGamesPlugin plugin;
    private final Map<UUID, SponsorData> sponsorData = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sponsorCooldowns = new ConcurrentHashMap<>();
    
    private int maxSponsorsPerPlayer = 3;
    private int sponsorCooldownSeconds = 30;
    private double dropHeight = 50;
    private double dropSpeed = 1.5;

    public SponsorManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        maxSponsorsPerPlayer = plugin.getConfigManager().getConfig().getInt("sponsor.max-per-player", 3);
        sponsorCooldownSeconds = plugin.getConfigManager().getConfig().getInt("sponsor.cooldown", 30);
        dropHeight = plugin.getConfigManager().getConfig().getDouble("sponsor.drop-height", 50);
        dropSpeed = plugin.getConfigManager().getConfig().getDouble("sponsor.drop-speed", 1.5);
    }

    public boolean canSponsor(Player sponsor, Player target) {
        if (!plugin.getGameManager().isPlayerAlive(target)) {
            plugin.getMessageManager().send(sponsor, "sponsor.target-not-alive");
            return false;
        }
        
        if (sponsor.equals(target)) {
            plugin.getMessageManager().send(sponsor, "sponsor.cannot-self");
            return false;
        }
        
        SponsorData data = sponsorData.computeIfAbsent(sponsor.getUniqueId(), k -> new SponsorData());
        if (data.getSponsorCount() >= maxSponsorsPerPlayer) {
            plugin.getMessageManager().send(sponsor, "sponsor.max-reached", "max", String.valueOf(maxSponsorsPerPlayer));
            return false;
        }
        
        Long lastSponsor = sponsorCooldowns.get(sponsor.getUniqueId());
        if (lastSponsor != null) {
            long remaining = (lastSponsor + sponsorCooldownSeconds * 1000L - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                plugin.getMessageManager().send(sponsor, "sponsor.cooldown", "time", String.valueOf(remaining));
                return false;
            }
        }
        
        return true;
    }

    public void sponsorPlayer(Player sponsor, Player target, ItemStack item) {
        if (!canSponsor(sponsor, target)) return;
        
        SponsorData data = sponsorData.computeIfAbsent(sponsor.getUniqueId(), k -> new SponsorData());
        data.incrementCount();
        sponsorCooldowns.put(sponsor.getUniqueId(), System.currentTimeMillis());
        
        // Actualizar stats del target
        GameManager.PlayerData targetData = plugin.getGameManager().getPlayers().get(target.getUniqueId());
        if (targetData != null) {
            targetData.addSponsorItem();
        }
        
        // Enviar mensajes
        plugin.getMessageManager().send(sponsor, "sponsor.sent", "target", target.getName(), "item", item.getType().name());
        plugin.getMessageManager().send(target, "sponsor.received", "sponsor", sponsor.getName(), "item", item.getType().name());
        plugin.getMessageManager().broadcast("sponsor.broadcast", "sponsor", sponsor.getName(), "target", target.getName(), "item", item.getType().name());
        
        // Dropear el item desde el cielo
        dropItemFromSky(target, item);
        
        // Efectos visuales y sonoros
        playSponsorEffects(target);
    }

    private void dropItemFromSky(Player target, ItemStack item) {
        Location targetLoc = target.getLocation().clone();
        World world = target.getWorld();
        Location dropLoc = targetLoc.clone().add(0, dropHeight, 0);
        
        // Crear item entity
        Item itemEntity = world.dropItem(dropLoc, item.clone());
        itemEntity.setPickupDelay(20); // 1 segundo antes de poder recoger
        itemEntity.setVelocity(new org.bukkit.util.Vector(0, -dropSpeed, 0));
        itemEntity.setInvulnerable(true);
        itemEntity.setGlowing(true);
        
        // Marcar como item de patrocinio
        itemEntity.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "sponsored_item"),
            PersistentDataType.BYTE, (byte) 1
        );
        itemEntity.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "sponsored_target"),
            PersistentDataType.STRING, target.getUniqueId().toString()
        );
        itemEntity.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "sponsored_sponsor"),
            PersistentDataType.STRING, plugin.getGameManager().getPlayers().keySet().stream()
                .filter(u -> plugin.getServer().getPlayer(u) == target)
                .findFirst()
                .map(UUID::toString)
                .orElse("")
        );
        
        // Partículas de rastro
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemEntity.isDead() || itemEntity.isOnGround()) {
                    cancel();
                    return;
                }
                world.spawnParticle(Particle.TRAIL, itemEntity.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
                world.spawnParticle(Particle.END_ROD, itemEntity.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Asegurar que caiga cerca del target
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemEntity.isDead() || itemEntity.isOnGround()) {
                    cancel();
                    return;
                }
                
                Location itemLoc = itemEntity.getLocation();
                double dx = targetLoc.getX() - itemLoc.getX();
                double dz = targetLoc.getZ() - itemLoc.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                
                if (dist > 2) {
                    // Corrección de trayectoria
                    double factor = 0.1;
                    itemEntity.setVelocity(itemEntity.getVelocity().add(
                        dx * factor, 0, dz * factor));
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private void playSponsorEffects(Player target) {
        Location loc = target.getLocation();
        World world = target.getWorld();
        
        // Sonido global para el target
        target.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        target.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        
        // Partículas
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 30, 1, 1, 1, 0.2);
        world.spawnParticle(Particle.END_ROD, loc.add(0, 1, 0), 20, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
        
        // Beacon beam temporal
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 40) { // 2 segundos
                    cancel();
                    return;
                }
                world.spawnParticle(Particle.END_ROD, loc.clone().add(0, ticks * 0.5, 0), 1, 0, 0, 0, 0);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void onPlayerDeath(Player player) {
        // Limpiar datos de patrocinio del jugador muerto
        sponsorData.remove(player.getUniqueId());
        sponsorCooldowns.remove(player.getUniqueId());
    }

    public ItemStack getSponsorItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lPatrocinio del Capitolio");
        meta.setLore(Arrays.asList(
            "§7Click derecho para abrir el menú",
            "§7de patrocinio y enviar items",
            "§7a los tributos vivos.",
            "",
            "§eUsos restantes: §a" + maxSponsorsPerPlayer,
            "§eCooldown: §a" + sponsorCooldownSeconds + "s"
        ));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "hg_sponsor_item"),
            PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getOwnerEventItem() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lControl de Eventos (Owner)");
        meta.setLore(Arrays.asList(
            "§7Click derecho para abrir el panel",
            "§7de control de eventos de la arena.",
            "",
            "§cSolo para Owners/Admins"
        ));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "hg_owner_event_item"),
            PersistentDataType.BYTE, (byte) 1
        );
        item.setItemMeta(meta);
        return item;
    }

    public boolean isSponsorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new NamespacedKey(plugin, "hg_sponsor_item"), PersistentDataType.BYTE);
    }

    public boolean isOwnerEventItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new NamespacedKey(plugin, "hg_owner_event_item"), PersistentDataType.BYTE);
    }

    public boolean isSponsoredItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new NamespacedKey(plugin, "sponsored_item"), PersistentDataType.BYTE);
    }

    public SponsorData getSponsorData(Player player) {
        return sponsorData.getOrDefault(player.getUniqueId(), new SponsorData());
    }

    public void clearSponsorData(Player player) {
        sponsorData.remove(player.getUniqueId());
        sponsorCooldowns.remove(player.getUniqueId());
    }

    public static class SponsorData {
        private int sponsorCount = 0;
        private final Map<String, Integer> itemsSent = new HashMap<>();

        public int getSponsorCount() { return sponsorCount; }
        public void incrementCount() { sponsorCount++; }
        public void addItemSent(String itemType) {
            itemsSent.merge(itemType, 1, Integer::sum);
        }
        public Map<String, Integer> getItemsSent() { return new HashMap<>(itemsSent); }
    }
}