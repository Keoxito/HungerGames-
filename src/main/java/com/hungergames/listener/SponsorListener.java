package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SponsorListener implements Listener {

    private final HungerGamesPlugin plugin;

    public SponsorListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        SponsorManager sponsorManager = plugin.getSponsorManager();
        GameManager gm = plugin.getGameManager();
        
        // Item de patrocinio
        if (sponsorManager.isSponsorItem(item) && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            if (!gm.getGameState().isPlaying()) {
                plugin.getMessageManager().send(player, "sponsor.game-not-running");
                return;
            }
            
            // Abrir GUI de patrocinio
            plugin.getCommand("hgsponsor").execute(player, "hgsponsor", new String[]{});
        }
        
        // Item de owner para eventos
        if (sponsorManager.isOwnerEventItem(item) && 
            (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            if (!player.hasPermission("hungergames.owner")) {
                plugin.getMessageManager().send(player, "command.no-permission");
                return;
            }
            
            // Abrir GUI de eventos
            plugin.getCommand("hgevent").execute(player, "hgevent", new String[]{});
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        Item itemEntity = event.getItem();
        ItemStack itemStack = itemEntity.getItemStack();
        
        // Verificar si es un item patrocinado
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "sponsored_item"), PersistentDataType.BYTE)) {
            
            String targetUUID = itemStack.getItemMeta().getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "sponsored_target"), PersistentDataType.STRING);
            
            if (targetUUID != null && !targetUUID.equals(player.getUniqueId().toString())) {
                // Solo el jugador objetivo puede recogerlo (durante 10 segundos)
                if (System.currentTimeMillis() - itemEntity.getTicksLived() * 50L < 10000) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            // El objetivo lo recogió
            plugin.getMessageManager().send(player, "sponsor.picked-up");
        }
    }
}