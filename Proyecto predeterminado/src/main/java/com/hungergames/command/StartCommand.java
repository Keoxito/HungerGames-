package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

public class StartCommand implements CommandExecutor, TabCompleter {

    private final HungerGamesPlugin plugin;

    public StartCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return true;
        }

        GameManager gm = plugin.getGameManager();
        
        if (gm.getGameState() != GameState.WAITING) {
            plugin.getMessageManager().send(sender, "command.game-already-running");
            return true;
        }

        if (args.length == 0) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgstart <arena>");
            return true;
        }

        String arenaName = args[0];
        if (gm.startGame(arenaName)) {
            plugin.getMessageManager().send(sender, "command.game-started", "arena", arenaName);
        } else {
            plugin.getMessageManager().send(sender, "command.game-start-failed", "arena", arenaName);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return plugin.getArenaManager().getArenas().keySet().stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}