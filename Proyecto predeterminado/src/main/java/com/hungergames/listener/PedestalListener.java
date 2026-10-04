package com.hungergames.listener;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.pedestal.PedestalManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PedestalListener implements Listener {

    private final HungerGamesPlugin plugin;

    public PedestalListener(HungerGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Comando /hgpedestal activado - verificar si está en pedestal
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.QUARTZ_BLOCK) {
                Location loc = block.getLocation();
                // Verificar si es un pedestal registrado
                // Esto se maneja principalmente en el GameManager al iniciar
            }
        }
    }
}