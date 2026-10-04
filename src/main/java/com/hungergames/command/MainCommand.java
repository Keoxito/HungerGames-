package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.EventManager;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final HungerGamesPlugin plugin;

    public MainCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start" -> {
                if (args.length < 2) {
                    plugin.getMessageManager().send(sender, "command.usage", "usage", "/hg start <arena>");
                    return true;
                }
                handleStart(sender, args[1]);
            }
            case "stop" -> handleStop(sender);
            case "status" -> handleStatus(sender);
            case "join" -> handleJoin(sender);
            case "leave" -> handleLeave(sender);
            case "list" -> handleList(sender);
            case "arenas" -> handleArenas(sender);
            case "reload" -> handleReload(sender);
            case "giveitem" -> handleGiveItem(sender, args);
            case "event" -> handleEvent(sender, args);
            case "help" -> sendHelp(sender);
            default -> {
                plugin.getMessageManager().send(sender, "command.unknown", "command", subCommand);
                sendHelp(sender);
            }
        }
        return true;
    }

    private void handleStart(CommandSender sender, String arenaName) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return;
        }

        GameManager gameManager = plugin.getGameManager();
        if (gameManager.getGameState() != GameState.WAITING) {
            plugin.getMessageManager().send(sender, "command.game-already-running");
            return;
        }

        if (gameManager.startGame(arenaName)) {
            plugin.getMessageManager().send(sender, "command.game-started", "arena", arenaName);
        } else {
            plugin.getMessageManager().send(sender, "command.game-start-failed", "arena", arenaName);
        }
    }

    private void handleStop(CommandSender sender) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return;
        }

        plugin.getGameManager().forceStopGame();
        plugin.getMessageManager().send(sender, "command.game-stopped");
    }

    private void handleStatus(CommandSender sender) {
        GameManager gm = plugin.getGameManager();
        GameState state = gm.getGameState();
        Arena arena = gm.getCurrentArena();

        sender.sendMessage("§8§m----------------§r §6Estado del Juego §r§8§m----------------");
        sender.sendMessage("§7Estado: §e" + state.getDisplayName());
        sender.sendMessage("§7Arena: §e" + (arena != null ? arena.getName() : "Ninguna"));
        sender.sendMessage("§7Jugadores totales: §e" + gm.getTotalPlayers());
        sender.sendMessage("§7Jugadores vivos: §e" + gm.getAliveCount());
        sender.sendMessage("§7Tiempo de juego: §e" + (gm.getCurrentArena() != null ? "En curso" : "N/A"));
        
        if (!gm.getAlivePlayersList().isEmpty()) {
            sender.sendMessage("§7Vivos: §e" + gm.getAlivePlayersList().stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
        sender.sendMessage("§8§m--------------------------------------------------");
    }

    private void handleJoin(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return;
        }

        GameManager gm = plugin.getGameManager();
        if (gm.getGameState() != GameState.WAITING && gm.getGameState() != GameState.STARTING) {
            plugin.getMessageManager().send(player, "command.cannot-join-now");
            return;
        }

        if (gm.isPlayerInGame(player)) {
            plugin.getMessageManager().send(player, "command.already-in-game");
            return;
        }

        gm.addPlayer(player);
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return;
        }

        GameManager gm = plugin.getGameManager();
        if (!gm.isPlayerInGame(player)) {
            plugin.getMessageManager().send(player, "command.not-in-game");
            return;
        }

        gm.removePlayer(player);
        plugin.getMessageManager().send(player, "command.left-game");
    }

    private void handleList(CommandSender sender) {
        GameManager gm = plugin.getGameManager();
        List<Player> players = gm.getOnlinePlayers();
        
        sender.sendMessage("§8§m----------------§r §6Jugadores en el Juego §r§8§m----------------");
        sender.sendMessage("§7Total: §e" + players.size() + " §7| Vivos: §a" + gm.getAliveCount() + " §7| Espectadores: §7" + (players.size() - gm.getAliveCount()));
        
        for (Player p : players) {
            String status = gm.isPlayerAlive(p) ? "§a✓ Vivo" : "§c✗ Muerto";
            String kills = " §7(Kills: §e" + gm.getPlayers().get(p.getUniqueId()).getKills() + "§7)";
            String sponsor = " §7(Sponsors: §6" + gm.getPlayers().get(p.getUniqueId()).getSponsorItemsReceived() + "§7)";
            sender.sendMessage("  §e" + p.getName() + " §7- " + status + kills + sponsor);
        }
    }

    private void handleArenas(CommandSender sender) {
        Map<String, Arena> arenas = plugin.getArenaManager().getArenas();
        sender.sendMessage("§8§m----------------§r §6Arenas Disponibles §r§8§m----------------");
        for (Arena arena : arenas.values()) {
            String status = arena.isEnabled() ? "§a✓" : "§c✗";
            sender.sendMessage("  " + status + " §e" + arena.getName() + " §7- Spawns: " + arena.getSpawnPoints().size() + " | Pedestales: " + arena.getPedestalLocations().size() + " | Mundo: " + arena.getWorldName());
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.getMessageManager().send(sender, "command.reloaded");
    }

    private void handleGiveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hg giveitem <sponsor|owner> [jugador]");
            return;
        }

        String itemType = args[1].toLowerCase();
        Player target = args.length > 2 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);

        if (target == null) {
            plugin.getMessageManager().send(sender, "command.player-not-found");
            return;
        }

        switch (itemType) {
            case "sponsor" -> {
                ItemStack item = plugin.getSponsorManager().getSponsorItem();
                target.getInventory().addItem(item);
                plugin.getMessageManager().send(sender, "command.item-given", "item", "Patrocinio", "player", target.getName());
            }
            case "owner" -> {
                if (!sender.hasPermission("hungergames.owner")) {
                    plugin.getMessageManager().send(sender, "command.no-permission");
                    return;
                }
                ItemStack item = plugin.getSponsorManager().getOwnerEventItem();
                target.getInventory().addItem(item);
                plugin.getMessageManager().send(sender, "command.item-given", "item", "Control de Eventos", "player", target.getName());
            }
            default -> plugin.getMessageManager().send(sender, "command.unknown-item", "item", itemType);
        }
    }

    private void handleEvent(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hungergames.owner")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hg event <evento> [arena]");
            return;
        }

        String eventName = args[1];
        String arenaName = args.length > 2 ? args[2] : null;
        
        Arena arena;
        if (arenaName != null) {
            arena = plugin.getArenaManager().getArena(arenaName).orElse(null);
        } else {
            arena = plugin.getGameManager().getCurrentArena();
        }

        if (arena == null) {
            plugin.getMessageManager().send(sender, "command.no-arena");
            return;
        }

        List<Player> players = plugin.getGameManager().getAlivePlayersList();
        if (players.isEmpty()) {
            plugin.getMessageManager().send(sender, "command.no-players-alive");
            return;
        }

        plugin.getEventManager().executeEvent(eventName, arena, players);
        plugin.getMessageManager().send(sender, "command.event-triggered", "event", eventName);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6Hunger Games §r§8§m----------------");
        sender.sendMessage("§e/hg start <arena> §7- Iniciar partida");
        sender.sendMessage("§e/hg stop §7- Detener partida");
        sender.sendMessage("§e/hg status §7- Ver estado");
        sender.sendMessage("§e/hg join §7- Unirse a la partida");
        sender.sendMessage("§e/hg leave §7- Salir de la partida");
        sender.sendMessage("§e/hg list §7- Listar jugadores");
        sender.sendMessage("§e/hg arenas §7- Listar arenas");
        sender.sendMessage("§e/hg reload §7- Recargar config");
        sender.sendMessage("§e/hg giveitem <sponsor|owner> [jugador] §7- Dar items especiales");
        sender.sendMessage("§e/hg event <evento> [arena] §7- Ejecutar evento (Owner)");
        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§7Eventos disponibles:");
        for (int i = 0; i < EventManager.EVENT_NAMES.length; i++) {
            sender.sendMessage("  §e" + EventManager.EVENT_NAMES[i] + " §7- " + EventManager.EVENT_DISPLAY_NAMES[i]);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "stop", "status", "join", "leave", "list", "arenas", "reload", "giveitem", "event", "help"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("start") || sub.equals("event")) {
                completions.addAll(plugin.getArenaManager().getArenas().keySet());
            } else if (sub.equals("giveitem")) {
                completions.addAll(Arrays.asList("sponsor", "owner"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("giveitem")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("event")) {
            completions.addAll(Arrays.asList(EventManager.EVENT_NAMES));
        }
        
        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}