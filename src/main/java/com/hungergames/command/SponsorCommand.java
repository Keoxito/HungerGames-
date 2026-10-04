package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SponsorCommand implements CommandExecutor, TabCompleter {

    private final HungerGamesPlugin plugin;

    public SponsorCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return true;
        }

        if (!player.hasPermission("hungergames.sponsor")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return true;
        }

        GameManager gm = plugin.getGameManager();
        
        if (!gm.getGameState().isPlaying()) {
            plugin.getMessageManager().send(player, "sponsor.game-not-running");
            return true;
        }

        List<Player> alivePlayers = gm.getAlivePlayersList();
        if (alivePlayers.isEmpty()) {
            plugin.getMessageManager().send(player, "sponsor.no-alive-players");
            return true;
        }

        if (args.length == 0) {
            // Abrir GUI de patrocinio
            openSponsorGUI(player, alivePlayers);
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().send(player, "command.player-not-found", "player", targetName);
            return true;
        }

        if (!gm.isPlayerAlive(target)) {
            plugin.getMessageManager().send(player, "sponsor.target-not-alive");
            return true;
        }

        if (args.length >= 2) {
            // Patrocinar con item específico
            String itemName = args[1].toUpperCase();
            try {
                org.bukkit.Material material = org.bukkit.Material.valueOf(itemName);
                ItemStack item = new ItemStack(material);
                plugin.getSponsorManager().sponsorPlayer(player, target, item);
            } catch (IllegalArgumentException e) {
                plugin.getMessageManager().send(player, "sponsor.invalid-item", "item", itemName);
            }
        } else {
            // Dar item de patrocinio
            ItemStack sponsorItem = plugin.getSponsorManager().getSponsorItem();
            player.getInventory().addItem(sponsorItem);
            plugin.getMessageManager().send(player, "sponsor.item-given");
        }

        return true;
    }

    private void openSponsorGUI(Player player, List<Player> alivePlayers) {
        org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 54, "§6§lPatrocinio del Capitolio");
        
        // Items de patrocinio predefinidos
        ItemStack[] sponsorItems = {
            new ItemStack(org.bukkit.Material.GOLDEN_APPLE, 1),
            new ItemStack(org.bukkit.Material.ENCHANTED_GOLDEN_APPLE, 1),
            new ItemStack(org.bukkit.Material.DIAMOND_SWORD, 1),
            new ItemStack(org.bukkit.Material.DIAMOND_PICKAXE, 1),
            new ItemStack(org.bukkit.Material.BOW, 1),
            new ItemStack(org.bukkit.Material.ARROW, 64),
            new ItemStack(org.bukkit.Material.COOKED_BEEF, 32),
            new ItemStack(org.bukkit.Material.GOLDEN_CARROT, 32),
            new ItemStack(org.bukkit.Material.ENDER_PEARL, 16),
            new ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE, 16),
            new ItemStack(org.bukkit.Material.TOTEM_OF_UNDYING, 1),
            new ItemStack(org.bukkit.Material.END_CRYSTAL, 4),
            new ItemStack(org.bukkit.Material.NETHERITE_INGOT, 4),
            new ItemStack(org.bukkit.Material.POTION, 1),
            new ItemStack(org.bukkit.Material.FIREWORK_ROCKET, 12)
        };

        int slot = 0;
        for (ItemStack item : sponsorItems) {
            if (slot >= 45) break;
            ItemStack display = item.clone();
            org.bukkit.inventory.meta.ItemMeta meta = display.getItemMeta();
            meta.setDisplayName("§e" + formatName(item.getType().name()));
            meta.setLore(List.of(
                "§7Click para patrocinar a un tributo",
                "§7con este item."
            ));
            display.setItemMeta(meta);
            gui.setItem(slot++, display);
        }

        // Jugadores vivos en la parte inferior
        for (int i = 0; i < alivePlayers.size() && i < 9; i++) {
            Player target = alivePlayers.get(i);
            ItemStack head = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§a" + target.getName());
            meta.setLore(List.of(
                "§7Click para seleccionar como objetivo",
                "§7Kills: §e" + gm.getPlayers().get(target.getUniqueId()).getKills(),
                "§7Sponsors recibidos: §6" + gm.getPlayers().get(target.getUniqueId()).getSponsorItemsReceived()
            ));
            head.setItemMeta(meta);
            gui.setItem(45 + i, head);
        }

        player.openInventory(gui);
    }

    private String formatName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<Player> alive = plugin.getGameManager().getAlivePlayersList();
            completions.addAll(alive.stream().map(Player::getName).toList());
        } else if (args.length == 2) {
            // Sugerir items comunes
            completions.addAll(List.of("GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "DIAMOND_SWORD", "BOW", "ARROW", "COOKED_BEEF", "ENDER_PEARL", "TOTEM_OF_UNDYING"));
        }
        
        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}