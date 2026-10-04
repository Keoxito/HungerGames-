package com.hungergames.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class MessageManager {

    private final ConfigManager configManager;
    private final String prefix;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.prefix = colorize(configManager.getMessagesConfig().getString("prefix", "&8[&cHungerGames&8] &r"));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return prefix;
    }

    public String get(String key) {
        return colorize(configManager.getMessagesConfig().getString(key, "&cMensaje no encontrado: " + key));
    }

    public String get(String key, String... placeholders) {
        String message = get(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }
        return message;
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(getPrefix() + get(key, placeholders));
    }

    public void send(Player player, String key, String... placeholders) {
        player.sendMessage(getPrefix() + get(key, placeholders));
    }

    public void broadcast(String key, String... placeholders) {
        String message = getPrefix() + get(key, placeholders);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcastActionBar(String key, String... placeholders) {
        String message = get(key, placeholders);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.sendActionBar(colorize(message));
        }
    }

    public void broadcastTitle(String titleKey, String subtitleKey, int fadeIn, int stay, int fadeOut, String... placeholders) {
        String title = get(titleKey, placeholders);
        String subtitle = get(subtitleKey, placeholders);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public List<String> getList(String key) {
        return configManager.getMessagesConfig().getStringList(key).stream()
                .map(this::colorize)
                .collect(Collectors.toList());
    }
}