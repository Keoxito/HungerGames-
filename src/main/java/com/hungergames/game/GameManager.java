package com.hungergames.game;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.arena.Arena;
import com.hungergames.arena.ArenaManager;
import com.hungergames.event.EventManager;
import com.hungergames.pedestal.PedestalManager;
import com.hungergames.sponsor.SponsorManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class GameManager {

    private final HungerGamesPlugin plugin;
    private final ArenaManager arenaManager;
    private final EventManager eventManager;
    private final SponsorManager sponsorManager;
    private final PedestalManager pedestalManager;

    private GameState state = GameState.WAITING;
    private Arena currentArena;
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final Set<UUID> alivePlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> spectators = ConcurrentHashMap.newKeySet();
    
    private BukkitTask gameTask;
    private BukkitTask borderTask;
    private BukkitTask gracePeriodTask;
    
    private int gracePeriodSeconds = 60;
    private int gameDurationMinutes = 30;
    private int deathmatchDelayMinutes = 25;
    private int minPlayers = 2;
    private int maxPlayers = 24;
    
    private int currentGracePeriod;
    private int currentGameTime;
    private boolean deathmatchStarted = false;

    public GameManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        this.eventManager = plugin.getEventManager();
        this.sponsorManager = plugin.getSponsorManager();
        this.pedestalManager = plugin.getPedestalManager();
        loadConfig();
    }

    private void loadConfig() {
        gracePeriodSeconds = plugin.getConfigManager().getConfig().getInt("game.grace-period", 60);
        gameDurationMinutes = plugin.getConfigManager().getConfig().getInt("game.duration-minutes", 30);
        deathmatchDelayMinutes = plugin.getConfigManager().getConfig().getInt("game.deathmatch-delay", 25);
        minPlayers = plugin.getConfigManager().getConfig().getInt("game.min-players", 2);
        maxPlayers = plugin.getConfigManager().getConfig().getInt("game.max-players", 24);
    }

    public boolean startGame(String arenaName) {
        if (state != GameState.WAITING && state != GameState.RESTARTING) {
            return false;
        }

        Optional<Arena> arenaOpt = arenaManager.getArena(arenaName);
        if (arenaOpt.isEmpty()) return false;
        
        currentArena = arenaOpt.get();
        if (!currentArena.isEnabled()) return false;
        
        if (players.size() < minPlayers) return false;

        state = GameState.STARTING;
        plugin.getMessageManager().broadcast("game.starting", "arena", currentArena.getName());
        
        // Teletransportar jugadores a pedestales
        teleportToPedestals();
        
        // Iniciar subida de pedestales
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            state = GameState.PEDESTAL_RISING;
            pedestalManager.startRising(currentArena);
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                beginGracePeriod();
            }, 100L); // 5 segundos para subir
            
        }, 20L); // 1 segundo de delay

        return true;
    }

    private void teleportToPedestals() {
        List<Location> pedestals = currentArena.getPedestalLocations();
        List<Player> playerList = new ArrayList<>(getOnlinePlayers());
        
        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            Location pedestalLoc;
            if (i < pedestals.size()) {
                pedestalLoc = pedestals.get(i).clone().add(0, 1, 0);
            } else {
                pedestalLoc = currentArena.getRandomSpawnPoint().clone().add(0, 1, 0);
            }
            
            player.teleport(pedestalLoc);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.removePotionEffects();
            
            // Efecto de ceguera durante la subida
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 255));
        }
    }

    private void beginGracePeriod() {
        state = GameState.GRACE_PERIOD;
        currentGracePeriod = gracePeriodSeconds;
        alivePlayers.addAll(players.keySet());
        
        plugin.getMessageManager().broadcastTitle("game.grace-period-title", "game.grace-period-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("game.grace-period-start", "time", String.valueOf(gracePeriodSeconds));
        
        // Quitar ceguera y lentitud
        for (UUID uuid : alivePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            }
        }
        
        gracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentGracePeriod--;
                if (currentGracePeriod <= 10 || currentGracePeriod % 30 == 0) {
                    plugin.getMessageManager().broadcastActionBar("game.grace-period-countdown", "time", String.valueOf(currentGracePeriod));
                }
                
                if (currentGracePeriod <= 0) {
                    startMainGame();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startMainGame() {
        state = GameState.IN_PROGRESS;
        deathmatchStarted = false;
        currentGameTime = 0;
        
        plugin.getMessageManager().broadcastTitle("game.start-title", "game.start-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("game.started");
        
        // Iniciar borde
        startBorderShrink();
        
        // Iniciar eventos automáticos
        eventManager.startGameEvents(currentArena);
        
        // Task principal del juego
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentGameTime++;
                checkGameEnd();
                
                // Iniciar deathmatch
                if (!deathmatchStarted && currentGameTime >= deathmatchDelayMinutes * 60) {
                    startDeathmatch();
                }
                
                // Anuncios cada 5 minutos
                if (currentGameTime % 300 == 0 && currentGameTime < deathmatchDelayMinutes * 60) {
                    int remaining = deathmatchDelayMinutes * 60 - currentGameTime;
                    plugin.getMessageManager().broadcast("game.time-remaining", "time", formatTime(remaining));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startDeathmatch() {
        deathmatchStarted = true;
        state = GameState.DEATHMATCH;
        
        plugin.getMessageManager().broadcastTitle("game.deathmatch-title", "game.deathmatch-subtitle", 10, 70, 20);
        plugin.getMessageManager().broadcast("game.deathmatch-start");
        
        // Teletransportar vivos al centro de deathmatch
        if (currentArena.getDeathmatchCenter() != null) {
            for (UUID uuid : alivePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Location dmLoc = currentArena.getDeathmatchCenter().clone().add(
                        (Math.random() - 0.5) * 10, 1, (Math.random() - 0.5) * 10);
                    player.teleport(dmLoc);
                }
            }
        }
        
        // Reducir borde rápidamente al deathmatch
        if (borderTask != null) borderTask.cancel();
        startDeathmatchBorder();
    }

    private void startBorderShrink() {
        if (currentArena.getCenter() == null) return;
        
        double startRadius = currentArena.getRadius();
        double endRadius = currentArena.getDeathmatchRadius();
        long totalTicks = (long) (deathmatchDelayMinutes * 60 * 20);
        double shrinkPerTick = (startRadius - endRadius) / totalTicks;
        
        borderTask = new BukkitRunnable() {
            double currentRadius = startRadius;
            
            @Override
            public void run() {
                currentRadius -= shrinkPerTick;
                if (currentRadius < endRadius) currentRadius = endRadius;
                
                currentArena.getCenter().getWorld().setWorldBorderCenter(
                    currentArena.getCenter().getX(), currentArena.getCenter().getZ());
                currentArena.getCenter().getWorld().setWorldBorderSize(currentRadius * 2);
                
                if (currentRadius <= endRadius) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void startDeathmatchBorder() {
        if (currentArena.getDeathmatchCenter() == null) return;
        
        double startRadius = currentArena.getDeathmatchRadius();
        double endRadius = 5;
        long totalTicks = 5 * 60 * 20; // 5 minutos
        double shrinkPerTick = (startRadius - endRadius) / totalTicks;
        
        borderTask = new BukkitRunnable() {
            double currentRadius = startRadius;
            
            @Override
            public void run() {
                currentRadius -= shrinkPerTick;
                if (currentRadius < endRadius) currentRadius = endRadius;
                
                currentArena.getDeathmatchCenter().getWorld().setWorldBorderCenter(
                    currentArena.getDeathmatchCenter().getX(), currentArena.getDeathmatchCenter().getZ());
                currentArena.getDeathmatchCenter().getWorld().setWorldBorderSize(currentRadius * 2);
                
                if (currentRadius <= endRadius) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void checkGameEnd() {
        if (alivePlayers.size() <= 1) {
            endGame();
        }
    }

    public void endGame() {
        if (gameTask != null) gameTask.cancel();
        if (borderTask != null) borderTask.cancel();
        if (gracePeriodTask != null) gracePeriodTask.cancel();
        
        state = GameState.FINISHED;
        
        Player winner = null;
        if (alivePlayers.size() == 1) {
            winner = Bukkit.getPlayer(alivePlayers.iterator().next());
        }
        
        if (winner != null) {
            plugin.getMessageManager().broadcastTitle("game.winner-title", "game.winner-subtitle", 10, 70, 20, "player", winner.getName());
            plugin.getMessageManager().broadcast("game.winner", "player", winner.getName());
            
            // Recompensas
            giveRewards(winner);
        } else {
            plugin.getMessageManager().broadcast("game.no-winner");
        }
        
        // Restaurar bordes
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            world.setWorldBorderSize(60000000);
        }
        
        // Limpiar después de 10 segundos
        Bukkit.getScheduler().runTaskLater(plugin, this::cleanup, 200L);
    }

    private void giveRewards(Player winner) {
        List<String> rewards = plugin.getConfigManager().getConfig().getStringList("rewards.winner");
        for (String reward : rewards) {
            // Parsear recompensa: "ITEM:CANTIDAD" o "COMMAND:comando"
            if (reward.startsWith("ITEM:")) {
                String[] parts = reward.split(":");
                if (parts.length >= 3) {
                    ItemStack item = new ItemStack(org.bukkit.Material.getMaterial(parts[1]), Integer.parseInt(parts[2]));
                    winner.getInventory().addItem(item);
                }
            } else if (reward.startsWith("COMMAND:")) {
                String cmd = reward.substring(8).replace("%player%", winner.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }

    private void cleanup() {
        // Teletransportar todos al lobby
        Location lobby = currentArena != null ? currentArena.getLobbySpawn() : null;
        if (lobby == null) lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (spectators.contains(player.getUniqueId()) || alivePlayers.contains(player.getUniqueId())) {
                player.teleport(lobby);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.setHealth(20);
                player.setFoodLevel(20);
                player.removePotionEffects();
            }
        }
        
        players.clear();
        alivePlayers.clear();
        spectators.clear();
        currentArena = null;
        state = GameState.WAITING;
        
        plugin.getMessageManager().broadcast("game.cleanup");
    }

    public void forceStopGame() {
        if (gameTask != null) gameTask.cancel();
        if (borderTask != null) borderTask.cancel();
        if (gracePeriodTask != null) gracePeriodTask.cancel();
        
        cleanup();
        plugin.getMessageManager().broadcast("game.force-stopped");
    }

    public void addPlayer(Player player) {
        if (state != GameState.WAITING && state != GameState.STARTING) return;
        if (players.size() >= maxPlayers) return;
        
        players.put(player.getUniqueId(), new PlayerData(player));
        plugin.getMessageManager().send(player, "game.joined", "count", String.valueOf(players.size()), "max", String.valueOf(maxPlayers));
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        alivePlayers.remove(uuid);
        spectators.remove(uuid);
        
        if (state == GameState.IN_PROGRESS || state == GameState.DEATHMATCH || state == GameState.GRACE_PERIOD) {
            plugin.getMessageManager().broadcast("game.player-left", "player", player.getName(), "alive", String.valueOf(alivePlayers.size()));
            checkGameEnd();
        }
    }

    public void playerDied(Player player, Player killer) {
        UUID uuid = player.getUniqueId();
        alivePlayers.remove(uuid);
        spectators.add(uuid);
        
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(currentArena.getCenter().clone().add(0, 50, 0));
        
        String msg = killer != null ? 
            plugin.getMessageManager().get("game.player-killed", "victim", player.getName(), "killer", killer.getName()) :
            plugin.getMessageManager().get("game.player-died", "player", player.getName());
        plugin.getMessageManager().broadcast(msg);
        
        // Notificar a sponsors
        sponsorManager.onPlayerDeath(player);
        
        checkGameEnd();
    }

    public List<Player> getOnlinePlayers() {
        List<Player> result = new ArrayList<>();
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) result.add(p);
        }
        return result;
    }

    public List<Player> getAlivePlayersList() {
        List<Player> result = new ArrayList<>();
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) result.add(p);
        }
        return result;
    }

    public int getAliveCount() { return alivePlayers.size(); }
    public int getTotalPlayers() { return players.size(); }
    public GameState getGameState() { return state; }
    public Arena getCurrentArena() { return currentArena; }
    public boolean isPlayerAlive(Player player) { return alivePlayers.contains(player.getUniqueId()); }
    public boolean isPlayerInGame(Player player) { return players.containsKey(player.getUniqueId()); }
    public boolean isSpectator(Player player) { return spectators.contains(player.getUniqueId()); }
    
    public Map<UUID, PlayerData> getPlayers() { return new HashMap<>(players); }
    public Set<UUID> getAlivePlayers() { return new HashSet<>(alivePlayers); }
    public int getCurrentGameTime() { return currentGameTime; }

    public static class PlayerData {
        private final UUID uuid;
        private final String name;
        private int kills;
        private int sponsorItemsReceived;

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.kills = 0;
            this.sponsorItemsReceived = 0;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public int getKills() { return kills; }
        public void addKill() { kills++; }
        public int getSponsorItemsReceived() { return sponsorItemsReceived; }
        public void addSponsorItem() { sponsorItemsReceived++; }
    }
}