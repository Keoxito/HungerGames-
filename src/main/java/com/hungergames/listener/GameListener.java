package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GameListener implements Listener {

    private final HungerGamesPlugin plugin;

    public GameListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (!gm.isPlayerInGame(player)) return;
        
        // Verificar si salió de la arena durante el juego
        if (gm.getGameState().isPlaying() && gm.getCurrentArena() != null) {
            Location to = event.getTo();
            if (to != null && !gm.getCurrentArena().isInArena(to)) {
                // Fuera del borde
                if (gm.isPlayerAlive(player)) {
                    player.damage(4.0); // 2 corazones por segundo fuera
                    player.sendActionBar("§c¡Estás fuera del borde! Regresa a la arena.");
                }
            }
        }
        
        // Durante grace period, no moverse de los pedestales
        if (gm.getGameState() == GameState.GRACE_PERIOD && gm.isPlayerAlive(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from != null && to != null && from.distance(to) > 0.5) {
                event.setCancelled(true);
                player.sendActionBar("§c¡No te muevas durante el periodo de gracia!");
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player) && gm.getGameState() == GameState.GRACE_PERIOD && gm.isPlayerAlive(player)) {
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
            }
        }
    }
}