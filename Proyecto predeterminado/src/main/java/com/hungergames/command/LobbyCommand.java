package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private final HungerGamesPlugin plugin;

    public LobbyCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return true;
        }

        GameManager gm = plugin.getGameManager();
        
        if (gm.isPlayerInGame(player) && gm.getGameState().isPlaying()) {
            plugin.getMessageManager().send(player, "lobby.cannot-leave-during-game");
            return true;
        }

        Arena arena = gm.getCurrentArena();
        Location lobby = arena != null ? arena.getLobbySpawn() : null;
        
        if (lobby == null) {
            lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        player.teleport(lobby);
        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.removePotionEffects();
        
        plugin.getMessageManager().send(player, "lobby.teleported");
        return true;
    }
}