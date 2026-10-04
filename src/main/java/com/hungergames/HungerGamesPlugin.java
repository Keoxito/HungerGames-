package com.hungergames;

import com.hungergames.arena.Arena;
import com.hungergames.arena.ArenaManager;
import com.hungergames.command.*;
import com.hungergames.event.*;
import com.hungergames.game.GameManager;
import com.hungergames.game.GameState;
import com.hungergames.listener.*;
import com.hungergames.pedestal.PedestalManager;
import com.hungergames.sponsor.SponsorManager;
import com.hungergames.util.ConfigManager;
import com.hungergames.util.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class HungerGamesPlugin extends JavaPlugin {

    private static HungerGamesPlugin instance;
    
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private SponsorManager sponsorManager;
    private PedestalManager pedestalManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Inicializar managers
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this.configManager);
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this);
        this.sponsorManager = new SponsorManager(this);
        this.pedestalManager = new PedestalManager(this);
        this.eventManager = new EventManager(this);

        // Registrar comandos
        registerCommands();
        
        // Registrar listeners
        registerListeners();
        
        // Cargar arenas
        arenaManager.loadArenas();
        
        // Iniciar task de pedestales
        pedestalManager.startPedestalTask();
        
        // Iniciar task de eventos automáticos
        eventManager.startAutoEvents();
        
        getLogger().log(Level.INFO, "HungerGames Plugin activado correctamente!");
        getLogger().log(Level.INFO, "Versión: 1.0.0 | Minecraft: 1.21.1");
    }

    @Override
    public void onDisable() {
        // Detener juego si está en curso
        if (gameManager.getGameState() != GameState.WAITING) {
            gameManager.forceStopGame();
        }
        
        // Guardar arenas
        arenaManager.saveArenas();
        
        // Limpiar pedestales
        pedestalManager.cleanup();
        
        // Detener eventos
        eventManager.stopAutoEvents();
        
        getLogger().log(Level.INFO, "HungerGames Plugin desactivado correctamente!");
    }

    private void registerCommands() {
        getCommand("hg").setExecutor(new MainCommand(this));
        getCommand("hgpedestal").setExecutor(new PedestalCommand(this));
        getCommand("hgsponsor").setExecutor(new SponsorCommand(this));
        getCommand("hgevent").setExecutor(new EventCommand(this));
        getCommand("hgstart").setExecutor(new StartCommand(this));
        getCommand("hgstop").setExecutor(new StopCommand(this));
        getCommand("hglobby").setExecutor(new LobbyCommand(this));
        getCommand("hgarena").setExecutor(new ArenaCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new PedestalListener(this), this);
        getServer().getPluginManager().registerEvents(new SponsorListener(this), this);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }

    public static HungerGamesPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public GameManager getGameManager() { return gameManager; }
    public SponsorManager getSponsorManager() { return sponsorManager; }
    public PedestalManager getPedestalManager() { return pedestalManager; }
    public EventManager getEventManager() { return eventManager; }
}