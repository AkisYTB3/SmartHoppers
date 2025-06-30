package org.notionsmp.smarthoppers;

import co.aikar.commands.PaperCommandManager;
import com.nexomc.protectionlib.ProtectionLib;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.notionsmp.smarthoppers.commands.SmartHoppersCommand;
import org.notionsmp.smarthoppers.listeners.HopperListener;
import org.notionsmp.smarthoppers.listeners.HopperTransferListener;
import org.notionsmp.smarthoppers.managers.ConfigManager;
import org.notionsmp.smarthoppers.managers.GUIManager;
import org.notionsmp.smarthoppers.managers.HopperManager;
import org.notionsmp.smarthoppers.managers.ItemManager;
import org.notionsmp.smarthoppers.utils.Metrics;

import java.util.List;

@Getter
public final class SmartHoppers extends JavaPlugin {
    @Getter
    private static SmartHoppers instance;
    private HopperManager hopperManager;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private ItemManager itemManager;
    private PaperCommandManager commandManager;
    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        foliaLib = new FoliaLib(this);
        instance = this;
        ProtectionLib.init(this);
        saveDefaultConfig();
        configManager = new ConfigManager();
        guiManager = new GUIManager();
        itemManager = new ItemManager();
        hopperManager = new HopperManager();
        getServer().getPluginManager().registerEvents(new HopperListener(), this);
        getServer().getPluginManager().registerEvents(new HopperTransferListener(), this);
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new SmartHoppersCommand());

        registerRecipe();

        initMetrics();
    }

    private void initMetrics() {
        Metrics metrics = new Metrics(this, 25706);
    }

    private void registerRecipe() {
        FileConfiguration config = getConfig();
        if (config.getBoolean("hopper-item.recipe.enabled", true)) {
            ShapedRecipe recipe = new ShapedRecipe(
                    new NamespacedKey(this, "smart_hopper_item"),
                    itemManager.getHopperItem()
            );

            List<String> shape = config.getStringList("hopper-item.recipe.shape");
            recipe.shape(shape.toArray(new String[0]));

            ConfigurationSection ingredients = config.getConfigurationSection("hopper-item.recipe.ingredients");
            if (ingredients != null) {
                for (String key : ingredients.getKeys(false)) {
                    Material material = Material.valueOf(ingredients.getString(key));
                    recipe.setIngredient(key.charAt(0), material);
                }
            }

            try {
                Bukkit.addRecipe(recipe);
            } catch (IllegalStateException e) {
                getLogger().warning("Failed to register recipe: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        if (hopperManager != null) {
            hopperManager.saveAllHoppers();
        }
    }
}