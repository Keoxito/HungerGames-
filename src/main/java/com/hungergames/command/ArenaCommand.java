package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaCommand implements CommandExecutor, TabCompleter {

    private final HungerGamesPlugin plugin;

    public ArenaCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hungergames.admin")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "crear", "create" -> handleCreate(player, args);
            case "eliminar", "delete", "remove" -> handleDelete(sender, args);
            case "listar", "list" -> handleList(sender);
            case "establecer", "set" -> handleSet(player, args);
            case "info" -> handleInfo(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena crear <nombre>");
            return;
        }

        String name = args[1];
        if (plugin.getArenaManager().hasArena(name)) {
            plugin.getMessageManager().send(player, "arena.already-exists", "name", name);
            return;
        }

        Arena arena = plugin.getArenaManager().createArena(name);
        arena.setWorldName(player.getWorld().getName());
        arena.setLobbySpawn(player.getLocation());
        arena.setCenter(player.getLocation());
        
        plugin.getArenaManager().saveArenas();
        plugin.getMessageManager().send(player, "arena.created", "name", name);
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena eliminar <nombre>");
            return;
        }

        String name = args[1];
        if (plugin.getArenaManager().deleteArena(name)) {
            plugin.getMessageManager().send(sender, "arena.deleted", "name", name);
        } else {
            plugin.getMessageManager().send(sender, "arena.not-found", "name", name);
        }
    }

    private void handleList(CommandSender sender) {
        var arenas = plugin.getArenaManager().getArenas();
        sender.sendMessage("§8§m----------------§r §6Arenas §r§8§m----------------");
        for (Arena arena : arenas.values()) {
            String status = arena.isEnabled() ? "§a✓" : "§c✗";
            sender.sendMessage("  " + status + " §e" + arena.getName() + " §7(Mundo: " + arena.getWorldName() + ")");
            sender.sendMessage("    §7Centro: " + formatLoc(arena.getCenter()) + " | Radio: " + arena.getRadius());
            sender.sendMessage("    §7Lobby: " + formatLoc(arena.getLobbySpawn()) + " | Spawns: " + arena.getSpawnPoints().size() + " | Pedestales: " + arena.getPedestalLocations().size());
        }
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena set <arena> <propiedad>");
            return;
        }

        String arenaName = args[1];
        String property = args[2].toLowerCase();
        
        Arena arena = plugin.getArenaManager().getArena(arenaName).orElse(null);
        if (arena == null) {
            plugin.getMessageManager().send(player, "arena.not-found", "name", arenaName);
            return;
        }

        Location loc = player.getLocation();

        switch (property) {
            case "lobby" -> {
                arena.setLobbySpawn(loc);
                plugin.getMessageManager().send(player, "arena.set-lobby", "name", arenaName);
            }
            case "centro", "center" -> {
                arena.setCenter(loc);
                plugin.getMessageManager().send(player, "arena.set-center", "name", arenaName);
            }
            case "radio", "radius" -> {
                if (args.length < 4) {
                    plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena set <arena> radio <valor>");
                    return;
                }
                try {
                    int radius = Integer.parseInt(args[3]);
                    arena.setRadius(radius);
                    plugin.getMessageManager().send(player, "arena.set-radius", "name", arenaName, "value", String.valueOf(radius));
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().send(player, "arena.invalid-number");
                }
            }
            case "deathmatch" -> {
                arena.setDeathmatchCenter(loc);
                plugin.getMessageManager().send(player, "arena.set-deathmatch", "name", arenaName);
            }
            case "spawn" -> {
                arena.addSpawnPoint(loc);
                plugin.getMessageManager().send(player, "arena.add-spawn", "name", arenaName, "count", String.valueOf(arena.getSpawnPoints().size()));
            }
            case "pedestal" -> {
                arena.addPedestalLocation(loc);
                plugin.getMessageManager().send(player, "arena.add-pedestal", "name", arenaName, "count", String.valueOf(arena.getPedestalLocations().size()));
            }
            case "cofre", "chest" -> {
                arena.addChestLocation(loc);
                plugin.getMessageManager().send(player, "arena.add-chest", "name", arenaName, "count", String.valueOf(arena.getChestLocations().size()));
            }
            case "altura", "height" -> {
                if (args.length < 4) {
                    plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena set <arena> altura <valor>");
                    return;
                }
                try {
                    int height = Integer.parseInt(args[3]);
                    arena.setHeight(height);
                    plugin.getMessageManager().send(player, "arena.set-height", "name", arenaName, "value", String.valueOf(height));
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().send(player, "arena.invalid-number");
                }
            }
            case "dmradio", "deathmatch-radius" -> {
                if (args.length < 4) {
                    plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena set <arena> dmradio <valor>");
                    return;
                }
                try {
                    int radius = Integer.parseInt(args[3]);
                    arena.setDeathmatchRadius(radius);
                    plugin.getMessageManager().send(player, "arena.set-dm-radius", "name", arenaName, "value", String.valueOf(radius));
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().send(player, "arena.invalid-number");
                }
            }
            case "habilitar", "enable" -> {
                arena.setEnabled(true);
                plugin.getMessageManager().send(player, "arena.enabled", "name", arenaName);
            }
            case "deshabilitar", "disable" -> {
                arena.setEnabled(false);
                plugin.getMessageManager().send(player, "arena.disabled", "name", arenaName);
            }
            default -> {
                plugin.getMessageManager().send(player, "arena.unknown-property", "property", property);
                sendHelp(sender);
            }
        }

        plugin.getArenaManager().saveArenas();
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "command.usage", "usage", "/hgarena info <nombre>");
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[1]).orElse(null);
        if (arena == null) {
            plugin.getMessageManager().send(sender, "arena.not-found", "name", args[1]);
            return;
        }

        sender.sendMessage("§8§m----------------§r §6Info Arena: " + arena.getName() + " §r§8§m----------------");
        sender.sendMessage("§7Mundo: §e" + arena.getWorldName());
        sender.sendMessage("§7Habilitada: §e" + (arena.isEnabled() ? "Sí" : "No"));
        sender.sendMessage("§7Centro: §e" + formatLoc(arena.getCenter()));
        sender.sendMessage("§7Radio: §e" + arena.getRadius());
        sender.sendMessage("§7Altura: §e" + arena.getHeight());
        sender.sendMessage("§7Lobby: §e" + formatLoc(arena.getLobbySpawn()));
        sender.sendMessage("§7Deathmatch Centro: §e" + formatLoc(arena.getDeathmatchCenter()));
        sender.sendMessage("§7Deathmatch Radio: §e" + arena.getDeathmatchRadius());
        sender.sendMessage("§7Spawn Points: §e" + arena.getSpawnPoints().size());
        sender.sendMessage("§7Pedestales: §e" + arena.getPedestalLocations().size());
        sender.sendMessage("§7Cofres: §e" + arena.getChestLocations().size());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6Gestión de Arenas §r§8§m----------------");
        sender.sendMessage("§e/hgarena crear <nombre> §7- Crear nueva arena");
        sender.sendMessage("§e/hgarena eliminar <nombre> §7- Eliminar arena");
        sender.sendMessage("§e/hgarena listar §7- Listar arenas");
        sender.sendMessage("§e/hgarena info <nombre> §7- Ver info de arena");
        sender.sendMessage("§e/hgarena set <arena> lobby §7- Establecer lobby");
        sender.sendMessage("§e/hgarena set <arena> centro §7- Establecer centro");
        sender.sendMessage("§e/hgarena set <arena> radio <valor> §7- Establecer radio");
        sender.sendMessage("§e/hgarena set <arena> deathmatch §7- Establecer centro deathmatch");
        sender.sendMessage("§e/hgarena set <arena> spawn §7- Añadir spawn point");
        sender.sendMessage("§e/hgarena set <arena> pedestal §7- Añadir pedestal");
        sender.sendMessage("§e/hgarena set <arena> cofre §7- Añadir ubicación cofre");
        sender.sendMessage("§e/hgarena set <arena> altura <valor> §7- Establecer altura");
        sender.sendMessage("§e/hgarena set <arena> dmradio <valor> §7- Radio deathmatch");
        sender.sendMessage("§e/hgarena set <arena> habilitar|deshabilitar §7- Activar/desactivar");
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "No establecido";
        return String.format("%.0f, %.0f, %.0f (%s)", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("crear", "eliminar", "listar", "establecer", "info"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("eliminar") || sub.equals("info") || sub.equals("establecer")) {
                completions.addAll(plugin.getArenaManager().getArenas().keySet());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("establecer")) {
            completions.addAll(Arrays.asList("lobby", "centro", "radio", "deathmatch", "spawn", "pedestal", "cofre", "altura", "dmradio", "habilitar", "deshabilitar"));
        }
        
        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}