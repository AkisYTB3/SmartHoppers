package org.notionsmp.smarthoppers.managers;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.notionsmp.smarthoppers.SmartHoppers;
import org.notionsmp.smarthoppers.utils.FilterItem;
import org.notionsmp.smarthoppers.utils.HopperData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GUIManager {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Map<String, GUIItem> guiItems = new HashMap<>();
    private Map<Player, HopperData> playerEditingMap = new HashMap<>();

    public GUIManager() {
        loadGUIItems();
    }

    public void loadGUIItems() {
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        for (String key : guiConfig.getConfigurationSection("items").getKeys(false)) {
            Material material = Material.valueOf(guiConfig.getString("items." + key + ".material"));
            String itemName = guiConfig.getString("items." + key + ".itemname");
            List<String> lore = guiConfig.getStringList("items." + key + ".lore");
            int customModelData = guiConfig.getInt("items." + key + ".custom-model-data", 0);
            boolean glint = guiConfig.getBoolean("items." + key + ".Components.glint", false);
            guiItems.put(key, new GUIItem(material, itemName, lore, customModelData, glint));
        }
    }

    public void openHopperGUI(Player player, Hopper hopper) {
        HopperData hopperData = SmartHoppers.getInstance().getHopperManager().getHopperData(hopper);
        if (hopperData == null) return;

        playerEditingMap.put(player, hopperData);
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        Component title = miniMessage.deserialize(guiConfig.getString("settings.title", "<gray>Smart Hopper Settings"));
        Inventory gui = Bukkit.createInventory(null, 54, title);
        setupGUI(gui, hopperData);
        player.openInventory(gui);
    }

    public void refreshHopperGUI(Player player, Hopper hopper) {
        Inventory openInventory = player.getOpenInventory().getTopInventory();
        setupGUI(openInventory, playerEditingMap.get(player));
        player.updateInventory();
    }

    private void setupGUI(Inventory gui, HopperData hopperData) {
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        gui.clear();

        for (String slotStr : guiConfig.getStringList("settings.slots.placeholder")) {
            if (slotStr.contains("..")) {
                String[] parts = slotStr.split("\\.\\.");
                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                for (int i = from; i <= to; i++) {
                    gui.setItem(i, createGUIItem("placeholder"));
                }
            } else {
                gui.setItem(Integer.parseInt(slotStr), createGUIItem("placeholder"));
            }
        }

        gui.setItem(guiConfig.getInt("settings.slots.toggle"),
                hopperData.isEnabled() ? createGUIItem("enabled") : createGUIItem("disabled"));

        gui.setItem(guiConfig.getInt("settings.slots.whitelist"),
                hopperData.isWhitelist() ? createGUIItem("whitelist") : createGUIItem("blacklist"));

        int itemsPerPage = getItemsPerPage();
        int start = hopperData.getCurrentPage() * itemsPerPage;
        int end = Math.min(start + itemsPerPage, hopperData.getFilterItems().size());

        if (hopperData.getFilterItems().size() > end) {
            gui.setItem(guiConfig.getInt("settings.slots.next_page"), createGUIItem("next_page"));
        }
        if (hopperData.getCurrentPage() > 0) {
            gui.setItem(guiConfig.getInt("settings.slots.previous_page"), createGUIItem("previous_page"));
        }

        for (int i = start; i < end; i++) {
            FilterItem filterItem = hopperData.getFilterItems().get(i);
            ItemStack item = filterItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();

            List<Component> lore = meta.lore() != null ? meta.lore() : new ArrayList<>();
            if (filterItem.isExactMatch()) {
                for (String line : guiConfig.getStringList("exact_match_description")) {
                    lore.add(miniMessage.deserialize(line));
                }
            }
            meta.lore(lore);
            item.setItemMeta(meta);
            gui.setItem(getItemSlot(i - start), item);
        }
    }

    private int getItemsPerPage() {
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        int count = 0;
        for (String slotStr : guiConfig.getStringList("settings.slots.items")) {
            if (slotStr.contains("..")) {
                String[] range = slotStr.split("\\.\\.");
                count += Integer.parseInt(range[1]) - Integer.parseInt(range[0]) + 1;
            } else {
                count++;
            }
        }
        return count;
    }

    private int getItemSlot(int index) {
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        for (String slotStr : guiConfig.getStringList("settings.slots.items")) {
            if (slotStr.contains("..")) {
                String[] range = slotStr.split("\\.\\.");
                int availableSlots = Integer.parseInt(range[1]) - Integer.parseInt(range[0]) + 1;
                if (index < availableSlots) return Integer.parseInt(range[0]) + index;
                index -= availableSlots;
            } else {
                if (index == 0) return Integer.parseInt(slotStr);
                index--;
            }
        }
        return -1;
    }

    public ItemStack createGUIItem(String type) {
        GUIItem guiItem = guiItems.get(type);
        if (guiItem == null) return null;
        ItemStack item = new ItemStack(guiItem.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(miniMessage.deserialize(guiItem.getItemName()));
        if (!guiItem.getLore().isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            guiItem.getLore().forEach(line -> loreComponents.add(miniMessage.deserialize(line)));
            meta.lore(loreComponents);
        }
        if (guiItem.getCustomModelData() != 0) meta.setCustomModelData(guiItem.getCustomModelData());
        if (guiItem.hasGlint()) meta.setEnchantmentGlintOverride(guiItem.getGlint());
        item.setItemMeta(meta);
        return item;
    }

    public HopperData getPlayerEditingData(Player player) {
        return playerEditingMap.get(player);
    }

    public void removePlayerEditing(Player player) {
        playerEditingMap.remove(player);
    }

    @Getter
    private static class GUIItem {
        private final Material material;
        private final String itemName;
        private final List<String> lore;
        private final int customModelData;
        private final Boolean glint;

        public GUIItem(Material material, String itemName, List<String> lore, int customModelData, Boolean glint) {
            this.material = material;
            this.itemName = itemName;
            this.lore = lore;
            this.customModelData = customModelData;
            this.glint = glint;
        }

        public boolean hasGlint() {
            return glint != null;
        }

        public boolean getGlint() {
            return glint != null && glint;
        }
    }
}