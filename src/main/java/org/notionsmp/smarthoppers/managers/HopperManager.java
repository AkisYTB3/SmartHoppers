package org.notionsmp.smarthoppers.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.notionsmp.smarthoppers.SmartHoppers;
import org.notionsmp.smarthoppers.utils.HopperData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HopperManager {
    private final Map<Location, HopperData> hoppers = new HashMap<>();
    private File hoppersFile;

    public HopperManager() {
        loadHoppers();
    }

    private void loadHoppers() {
        hoppersFile = new File(SmartHoppers.getInstance().getDataFolder(), "hoppers.yml");

        if (!hoppersFile.exists()) {
            try {
                hoppersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(hoppersFile);

        for (String key : config.getKeys(false)) {
            ConfigurationSection hopperSection = config.getConfigurationSection(key);
            if (hopperSection == null) continue;

            String worldName = hopperSection.getString("world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue;

            Location location = new Location(
                    Bukkit.getWorld(worldName),
                    hopperSection.getInt("x"),
                    hopperSection.getInt("y"),
                    hopperSection.getInt("z")
            );

            ConfigurationSection dataSection = hopperSection.getConfigurationSection("data");
            if (dataSection == null) continue;

            try {
                HopperData data = HopperData.deserialize(dataSection.getValues(false));
                hoppers.put(location, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAllHoppers() {
        if (hoppersFile == null) {
            hoppersFile = new File(SmartHoppers.getInstance().getDataFolder(), "hoppers.yml");
        }

        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<Location, HopperData> entry : hoppers.entrySet()) {
            Location loc = entry.getKey();
            HopperData data = entry.getValue();

            String key = String.format("\"%s:%d,%d,%d\"",
                    loc.getWorld().getName(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ());

            ConfigurationSection hopperSection = config.createSection(key);
            hopperSection.set("world", loc.getWorld().getName());
            hopperSection.set("x", loc.getBlockX());
            hopperSection.set("y", loc.getBlockY());
            hopperSection.set("z", loc.getBlockZ());
            hopperSection.set("data", data.serialize());
        }

        try {
            config.save(hoppersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HopperData getHopperData(Hopper hopper) {
        return hoppers.computeIfAbsent(hopper.getLocation(), k -> new HopperData());
    }

    public void updateHopperData(Hopper hopper, HopperData data) {
        hoppers.put(hopper.getLocation(), data);
    }

    public void removeHopperData(Hopper hopper) {
        Location loc = hopper.getLocation();
        if (hoppers.remove(loc) != null) {
            saveAllHoppers();
        }
    }
}
