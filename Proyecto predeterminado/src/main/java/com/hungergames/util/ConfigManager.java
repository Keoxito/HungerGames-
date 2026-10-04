package com.hungergames.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    private FileConfiguration arenasConfig;
    private File arenasFile;
    
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        // config.yml principal
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // arenas.yml
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear arenas.yml", e);
            }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);

        // messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void saveArenas() {
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar arenas.yml", e);
        }
    }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getArenasConfig() { return arenasConfig; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
}