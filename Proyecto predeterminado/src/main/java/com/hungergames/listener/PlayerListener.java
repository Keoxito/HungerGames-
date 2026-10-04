package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final HungerGamesPlugin plugin;

    public PlayerListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        // Si hay partida en curso y el jugador estaba en ella
        if (gm.getGameState().isPlaying() && gm.isPlayerInGame(player)) {
            if (gm.isPlayerAlive(player)) {
                player.setGameMode(GameMode.SURVIVAL);
            } else {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(gm.getCurrentArena().getCenter().clone().add(0, 50, 0));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player)) {
            if (gm.getGameState().isPlaying()) {
                // No remover, solo marcar como desconectado
                // El juego continuará y se manejará la muerte si es necesario
            } else {
                gm.removePlayer(player);
            }
        }
        
        plugin.getSponsorManager().clearSponsorData(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        GameManager gm = plugin.getGameManager();
        
        if (!gm.isPlayerInGame(victim) || !gm.getGameState().isPlaying()) {
            return;
        }

        // Cancelar drops normales
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage(null);

        Player killer = victim.getKiller();
        
        // Si fue asesinado por un jugador
        if (killer != null && gm.isPlayerInGame(killer)) {
            gm.getPlayers().get(killer.getUniqueId()).addKill();
        }
        
        gm.playerDied(victim, killer);
        
        // Notificar a sponsor manager
        plugin.getSponsorManager().onPlayerDeath(victim);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player) && gm.getGameState().isPlaying()) {
            if (gm.isPlayerAlive(player)) {
                event.setRespawnLocation(player.getLocation());
            } else {
                // Espectador
                event.setRespawnLocation(gm.getCurrentArena().getCenter().clone().add(0, 50, 0));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player) && gm.getGameState().isPlaying()) {
            if (!gm.isPlayerAlive(player)) {
                // Los muertos solo pueden hablar con muertos
                event.setCancelled(true);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (gm.isPlayerInGame(p) && !gm.isPlayerAlive(p)) {
                        p.sendMessage("§7[§cMuerto§7] §f" + player.getName() + ": " + event.getMessage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player) && gm.getGameState().isPlaying() && !gm.isPlayerAlive(player)) {
            String cmd = event.getMessage().toLowerCase();
            // Permitir solo ciertos comandos a espectadores
            if (!cmd.startsWith("/msg") && !cmd.startsWith("/tell") && !cmd.startsWith("/r") 
                && !cmd.startsWith("/hg") && !cmd.startsWith("/hglobby")
                && !cmd.startsWith("/me") && !player.hasPermission("hungergames.admin")) {
                event.setCancelled(true);
                plugin.getMessageManager().send(player, "spectator.no-commands");
            }
        }
    }
}