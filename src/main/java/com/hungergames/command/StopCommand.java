package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopCommand implements CommandExecutor {

    private final HungerGamesPlugin plugin;

    public StopCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return true;
        }

        GameManager gm = plugin.getGameManager();
        gm.forceStopGame();
        plugin.getMessageManager().send(sender, "command.game-stopped");
        return true;
    }
}