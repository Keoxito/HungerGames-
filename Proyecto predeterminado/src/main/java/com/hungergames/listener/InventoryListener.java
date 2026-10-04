package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.event.EventManager;
import com.hungergames.game.GameManager;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EventListener implements Listener {

    private final HungerGamesPlugin plugin;

    public EventListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        // GUI de patrocinio
        if (title.equals("§6§lPatrocinio del Capitolio")) {
            event.setCancelled(true);
            
            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
                ItemStack clicked = event.getCurrentItem();
                ItemStack cursor = event.getCursor();
                
                // Click en item de patrocinio (slots 0-44)
                if (event.getRawSlot() < 45 && clicked != null && clicked.getType() != Material.AIR) {
                    // Guardar item seleccionado en cursor
                    event.setCursor(clicked.clone());
                    player.sendActionBar("§eItem seleccionado: §f" + formatName(clicked.getType().name()) + " §7- Click en un jugador para patrocinar");
                }
                // Click en jugador (slots 45-53)
                else if (event.getRawSlot() >= 45 && event.getRawSlot() < 54 && cursor != null && cursor.getType() != Material.AIR) {
                    ItemStack targetItem = event.getCurrentItem();
                    if (targetItem != null && targetItem.getType() == Material.PLAYER_HEAD) {
                        ItemMeta meta = targetItem.getItemMeta();
                        if (meta != null && meta.getOwningPlayer() != null) {
                            Player target = meta.getOwningPlayer().getPlayer();
                            if (target != null && target.isOnline()) {
                                // Patrocinar
                                plugin.getSponsorManager().sponsorPlayer(player, target, cursor);
                                event.setCursor(new ItemStack(Material.AIR));
                            }
                        }
                    }
                }
            }
        }
        
        // GUI de eventos (Owner)
        if (title.equals("§c§lPanel de Control de Eventos")) {
            event.setCancelled(true);
            
            if (event.getClick() == ClickType.LEFT && event.getRawSlot() < 10) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.hasItemMeta()) {
                    String displayName = clicked.getItemMeta().getDisplayName();
                    // Buscar evento por display name
                    for (String eventName : EventManager.EVENT_NAMES) {
                        var evt = plugin.getEventManager().getEvent(eventName);
                        if (evt != null && evt.getDisplayName().equals(ChatColor.stripColor(displayName))) {
                            plugin.getEventManager().executeEvent(eventName, 
                                plugin.getGameManager().getCurrentArena(),
                                plugin.getGameManager().getAlivePlayersList());
                            plugin.getMessageManager().send(player, "event.triggered", "event", eventName);
                            player.closeInventory();
                            break;
                        }
                    }
                }
            } else if (event.getClick() == ClickType.RIGHT && event.getRawSlot() < 10) {
                // Click derecho - mostrar info
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.hasItemMeta()) {
                    String displayName = clicked.getItemMeta().getDisplayName();
                    for (String eventName : EventManager.EVENT_NAMES) {
                        var evt = plugin.getEventManager().getEvent(eventName);
                        if (evt != null && evt.getDisplayName().equals(ChatColor.stripColor(displayName))) {
                            showEventInfo(player, evt);
                            break;
                        }
                    }
                }
            } else if (event.getRawSlot() == 50) {
                // Dar item de owner
                ItemStack ownerItem = plugin.getSponsorManager().getOwnerEventItem();
                player.getInventory().addItem(ownerItem);
                plugin.getMessageManager().send(player, "command.item-given", "item", "Control de Eventos", "player", player.getName());
            }
        }
    }

    private void showEventInfo(Player player, com.hungergames.event.GameEvent event) {
        player.sendMessage("§8§m----------------§r §6Info Evento: " + event.getDisplayName() + " §r§8§m----------------");
        player.sendMessage("§7ID: §e" + event.getName());
        player.sendMessage("§7Tipo: §e" + event.getType().getDisplayName());
        player.sendMessage("§7Descripción: §e" + event.getDescription());
        player.sendMessage("§7Jugadores mínimos: §e" + event.getMinPlayers());
        player.sendMessage("§7Cooldown: §e" + event.getCooldownSeconds() + "s");
        if (event.getRemainingCooldown() > 0) {
            player.sendMessage("§7Cooldown restante: §c" + event.getRemainingCooldown() + "s");
        } else {
            player.sendMessage("§7Cooldown restante: §aDisponible");
        }
    }

    private String formatName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}