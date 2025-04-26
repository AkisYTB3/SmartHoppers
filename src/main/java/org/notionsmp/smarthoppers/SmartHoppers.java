package org.notionsmp.smarthoppers;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.notionsmp.smarthoppers.commands.SmartHoppersCommand;
import org.notionsmp.smarthoppers.listeners.HopperListener;
import org.notionsmp.smarthoppers.listeners.HopperTransferListener;
import org.notionsmp.smarthoppers.managers.ConfigManager;
import org.notionsmp.smarthoppers.managers.GUIManager;
import org.notionsmp.smarthoppers.managers.HopperManager;
import org.notionsmp.smarthoppers.managers.ItemManager;

@Getter
public final class SmartHoppers extends JavaPlugin {
    @Getter
    private static SmartHoppers instance;
    private HopperManager hopperManager;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private ItemManager itemManager;
    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configManager = new ConfigManager();
        guiManager = new GUIManager();
        itemManager = new ItemManager();
        hopperManager = new HopperManager();
        getServer().getPluginManager().registerEvents(new HopperListener(), this);
        getServer().getPluginManager().registerEvents(new HopperTransferListener(), this);
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new SmartHoppersCommand());
    }

    @Override
    public void onDisable() {
        if (hopperManager != null) {
            hopperManager.saveAllHoppers();
        }
    }
}