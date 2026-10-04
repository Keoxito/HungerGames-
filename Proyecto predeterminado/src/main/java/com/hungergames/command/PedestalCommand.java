package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PedestalCommand implements CommandExecutor {

    private final HungerGamesPlugin plugin;

    public PedestalCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return true;
        }

        GameManager gm = plugin.getGameManager();
        
        if (gm.getGameState() != GameState.WAITING && gm.getGameState() != GameState.STARTING) {
            plugin.getMessageManager().send(player, "pedestal.cannot-use-now");
            return true;
        }

        if (!gm.isPlayerInGame(player)) {
            plugin.getMessageManager().send(player, "pedestal.not-in-game");
            return true;
        }

        // Verificar si está en un pedestal
        Location loc = player.getLocation().clone().subtract(0, 1, 0);
        boolean onPedestal = false;
        
        if (gm.getCurrentArena() != null) {
            for (Location pLoc : gm.getCurrentArena().getPedestalLocations()) {
                if (loc.distanceSquared(pLoc) < 2) {
                    onPedestal = true;
                    break;
                }
            }
        }

        if (!onPedestal) {
            plugin.getMessageManager().send(player, "pedestal.not-on-pedestal");
            return true;
        }

        plugin.getMessageManager().send(player, "pedestal.activated");
        // El pedestal se activará automáticamente al iniciar la partida
        return true;
    }
}