package com.hungergames.event;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.event.impl.*;
import com.hungergames.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EventManager {

    private final HungerGamesPlugin plugin;
    private final List<GameEvent> allEvents = new ArrayList<>();
    private final List<GameEvent> automaticEvents = new ArrayList<>();
    private final Map<String, GameEvent> eventsByName = new HashMap<>();
    
    private BukkitTask autoEventTask;
    private boolean autoEventsEnabled = true;
    private int autoEventInterval = 120; // segundos
    private int minAutoEventInterval = 60;
    private int maxAutoEventInterval = 300;

    public EventManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        registerEvents();
        loadConfig();
    }

    private void registerEvents() {
        // Eventos de mutaciones
        registerEvent(new MuttsEvent(plugin));
        
        // Eventos ambientales
        registerEvent(new FireEvent(plugin));
        registerEvent(new StormEvent(plugin));
        
        // Eventos de suministros
        registerEvent(new FeastEvent(plugin));
        registerEvent(new SupplyDropEvent(plugin));
        
        // Cambios de arena
        registerEvent(new ArenaShiftEvent(plugin));
        
        // Trampas
        registerEvent(new TrapEvent(plugin));
        
        // Efectos en jugadores
        registerEvent(new PlayerEffectEvent(plugin));
        
        // Bendiciones
        registerEvent(new BlessingEvent(plugin));
        
        // Jefes
        registerEvent(new BossEvent(plugin));
        
        // Configurar cuáles son automáticos
        for (GameEvent event : allEvents) {
            if (event.getType() != EventType.BOSS && event.getType() != EventType.BLESSING) {
                automaticEvents.add(event);
            }
        }
        
        plugin.getLogger().info("Registrados " + allEvents.size() + " eventos (" + automaticEvents.size() + " automáticos)");
    }

    private void registerEvent(GameEvent event) {
        allEvents.add(event);
        eventsByName.put(event.getName().toLowerCase(), event);
    }

    private void loadConfig() {
        autoEventsEnabled = plugin.getConfigManager().getConfig().getBoolean("events.auto-enabled", true);
        autoEventInterval = plugin.getConfigManager().getConfig().getInt("events.auto-interval", 120);
        minAutoEventInterval = plugin.getConfigManager().getConfig().getInt("events.min-interval", 60);
        maxAutoEventInterval = plugin.getConfigManager().getConfig().getInt("events.max-interval", 300);
    }

    public void startAutoEvents() {
        if (!autoEventsEnabled) return;
        
        autoEventTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGameManager().getGameState().isPlaying()) {
                    triggerRandomAutoEvent();
                }
            }
        }.runTaskTimer(plugin, autoEventInterval * 20L, autoEventInterval * 20L);
    }

    public void stopAutoEvents() {
        if (autoEventTask != null) {
            autoEventTask.cancel();
            autoEventTask = null;
        }
    }

    private void triggerRandomAutoEvent() {
        GameManager gameManager = plugin.getGameManager();
        Arena arena = gameManager.getCurrentArena();
        List<Player> alivePlayers = gameManager.getAlivePlayersList();
        
        if (arena == null || alivePlayers.isEmpty()) return;
        
        // Filtrar eventos que pueden ejecutarse
        List<GameEvent> availableEvents = new ArrayList<>();
        for (GameEvent event : automaticEvents) {
            if (event.canExecute(arena, alivePlayers)) {
                availableEvents.add(event);
            }
        }
        
        if (availableEvents.isEmpty()) return;
        
        // Elegir evento aleatorio ponderado por tipo
        GameEvent chosen = availableEvents.get(new Random().nextInt(availableEvents.size()));
        chosen.execute(arena, alivePlayers);
        
        plugin.getLogger().info("Evento automático ejecutado: " + chosen.getName());
    }

    public void executeEvent(String eventName, Arena arena, List<Player> players) {
        GameEvent event = eventsByName.get(eventName.toLowerCase());
        if (event == null) {
            plugin.getLogger().warning("Evento no encontrado: " + eventName);
            return;
        }
        
        if (event.canExecute(arena, players)) {
            event.execute(arena, players);
        } else {
            plugin.getLogger().info("Evento " + eventName + " no puede ejecutarse ahora (cooldown o requisitos)");
        }
    }

    public void startGameEvents(Arena arena) {
        // Eventos iniciales al empezar la partida
        List<Player> alivePlayers = plugin.getGameManager().getAlivePlayersList();
        
        // Anunciar que los eventos automáticos están activos
        plugin.getMessageManager().broadcast("event.auto-enabled");
    }

    public List<GameEvent> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    public List<GameEvent> getAutomaticEvents() {
        return new ArrayList<>(automaticEvents);
    }

    public GameEvent getEvent(String name) {
        return eventsByName.get(name.toLowerCase());
    }

    public List<GameEvent> getEventsByType(EventType type) {
        List<GameEvent> result = new ArrayList<>();
        for (GameEvent event : allEvents) {
            if (event.getType() == type) result.add(event);
        }
        return result;
    }

    public void reload() {
        stopAutoEvents();
        loadConfig();
        startAutoEvents();
    }

    // Nombres de eventos para referencia
    public static final String[] EVENT_NAMES = {
        // Mutaciones
        "mutts",
        
        // Ambientales
        "fire_storm",
        "storm",
        
        // Suministros
        "feast",
        "supply_drop",
        
        // Cambios de arena
        "arena_shift",
        
        // Trampas
        "traps",
        
        // Efectos jugadores
        "player_effects",
        
        // Bendiciones
        "blessing",
        
        // Jefes
        "boss"
    };
    
    public static final String[] EVENT_DISPLAY_NAMES = {
        "§cMutaciones",
        "§6Tormenta de Fuego",
        "§bTormenta de Rayos",
        "§dBanquete",
        "§bDrop de Suministros",
        "§dCambio de Arena",
        "§8Trampas Mortales",
        "§eEfectos Globales",
        "§aBendición del Capitolio",
        "§c§lJefe de la Arena"
    };
}