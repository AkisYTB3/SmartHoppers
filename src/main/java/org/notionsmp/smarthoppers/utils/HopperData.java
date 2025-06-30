package org.notionsmp.smarthoppers.utils;

import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Data
public class HopperData implements ConfigurationSerializable {
    private final List<FilterItem> filterItems = new ArrayList<>();
    private boolean whitelist = true;
    private int currentPage = 0;
    private boolean enabled = false;

    public void addFilterItem(ItemStack item, boolean exactMatch) {
        ItemStack clonedItem = item.clone();
        for (FilterItem fi : filterItems) {
            if (exactMatch && fi.isExactMatch() && fi.getItem().equals(clonedItem)) return;
            if (!exactMatch && !fi.isExactMatch() && fi.getItem().getType() == clonedItem.getType()) return;
        }
        filterItems.add(new FilterItem(clonedItem, exactMatch));
    }

    public void removeFilterItem(ItemStack item) {
        filterItems.removeIf(fi -> fi.getItem().equals(item));
    }

    public boolean isItemAllowed(ItemStack item) {
        for (FilterItem fi : filterItems) {
            boolean match = fi.isExactMatch()
                    ? fi.getItem().equals(item)
                    : fi.getItem().getType() == item.getType();
            if (match) return whitelist;
        }
        return !whitelist;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Map<String, Object>> serializedItems = new ArrayList<>(filterItems.size());
        for (FilterItem fi : filterItems) {
            serializedItems.add(fi.serialize());
        }
        map.put("filterItems", serializedItems);
        map.put("whitelist", whitelist);
        map.put("currentPage", currentPage);
        map.put("enabled", enabled);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static HopperData deserialize(Map<String, Object> map) {
        HopperData data = new HopperData();

        Object rawList = map.get("filterItems");
        if (rawList instanceof List<?> list) {
            for (Object itemObj : list) {
                if (itemObj instanceof Map<?, ?> itemMap) {
                    data.filterItems.add(FilterItem.deserialize((Map<String, Object>) itemMap));
                }
            }
        }

        Object wl = map.get("whitelist");
        if (wl instanceof Boolean b) data.whitelist = b;

        Object page = map.get("currentPage");
        if (page instanceof Number n) data.currentPage = n.intValue();

        Object en = map.get("enabled");
        if (en instanceof Boolean b) data.enabled = b;

        return data;
    }
}
