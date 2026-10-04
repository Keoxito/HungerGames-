package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.event.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class EventCommand implements CommandExecutor {

    private final HungerGamesPlugin plugin;

    public EventCommand(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "command.players-only");
            return true;
        }

        if (!player.hasPermission("hungergames.owner")) {
            plugin.getMessageManager().send(sender, "command.no-permission");
            return true;
        }

        if (args.length == 0) {
            openEventGUI(player);
            return true;
        }

        String eventName = args[0];
        com.hungergames.arena.Arena arena = plugin.getGameManager().getCurrentArena();
        List<Player> players = plugin.getGameManager().getAlivePlayersList();

        if (arena == null || players.isEmpty()) {
            plugin.getMessageManager().send(player, "event.no-active-game");
            return true;
        }

        plugin.getEventManager().executeEvent(eventName, arena, players);
        plugin.getMessageManager().send(player, "event.triggered", "event", eventName);
        return true;
    }

    private void openEventGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§c§lPanel de Control de Eventos");
        
        String[] events = EventManager.EVENT_NAMES;
        String[] displayNames = EventManager.EVENT_DISPLAY_NAMES;
        Material[] icons = {
            Material.ZOMBIE_HEAD,      // mutts
            Material.BLAZE_ROD,        // fire_storm
            Material.LIGHTNING_ROD,    // storm
            Material.ENDER_CHEST,      // feast
            Material.CHEST,            // supply_drop
            Material.BEDROCK,          // arena_shift
            Material.TNT,              // traps
            Material.POTION,           // player_effects
            Material.NETHER_STAR,      // blessing
            Material.WITHER_SKELETON_SKULL // boss
        };

        for (int i = 0; i < events.length; i++) {
            ItemStack item = new ItemStack(icons[i % icons.length]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayNames[i]);
            meta.setLore(Arrays.asList(
                "§7ID: §e" + events[i],
                "§7Tipo: §e" + plugin.getEventManager().getEvent(events[i]).getType().getDisplayName(),
                "§7Cooldown: §e" + plugin.getEventManager().getEvent(events[i]).getCooldownSeconds() + "s",
                "",
                "§aClick izquierdo: §7Ejecutar evento",
                "§eClick derecho: §7Ver info del evento"
            ));
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        // Item de info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§b§lInformación");
        infoMeta.setLore(Arrays.asList(
            "§7Usa este panel para ejecutar eventos",
            "§7durante la partida.",
            "",
            "§c¡Cuidado! Algunos eventos son muy",
            "§cpoderosos y pueden cambiar el curso",
            "§cde la partida drásticamente."
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(49, info);

        // Item para dar item de owner
        ItemStack giveOwner = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta giveMeta = giveOwner.getItemMeta();
        giveMeta.setDisplayName("§6§lDar Item de Owner");
        giveMeta.setLore(Arrays.asList(
            "§7Click para darte el item de",
            "§7control de eventos."
        ));
        giveOwner.setItemMeta(giveMeta);
        gui.setItem(50, giveOwner);

        player.openInventory(gui);
    }
}