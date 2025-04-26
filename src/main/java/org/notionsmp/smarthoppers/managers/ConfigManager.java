package org.notionsmp.smarthoppers.managers;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.notionsmp.smarthoppers.SmartHoppers;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigManager {
    private FileConfiguration config;
    private FileConfiguration guiConfig;

    public ConfigManager() {
        loadConfigs();
    }

    private void loadConfigs() {
        SmartHoppers.getInstance().saveDefaultConfig();
        config = SmartHoppers.getInstance().getConfig();

        File guiFile = new File(SmartHoppers.getInstance().getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            SmartHoppers.getInstance().saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void reloadConfigs() {
        SmartHoppers.getInstance().reloadConfig();
        config = SmartHoppers.getInstance().getConfig();
        File guiFile = new File(SmartHoppers.getInstance().getDataFolder(), "gui.yml");
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void saveConfigs() {
        try {
            config.save(new File(SmartHoppers.getInstance().getDataFolder(), "config.yml"));
            guiConfig.save(new File(SmartHoppers.getInstance().getDataFolder(), "gui.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}