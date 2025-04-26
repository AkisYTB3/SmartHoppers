package org.notionsmp.smarthoppers.utils;

import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Data
public class HopperData implements ConfigurationSerializable {
    private List<FilterItem> filterItems = new ArrayList<>();
    private boolean whitelist = true;
    private int currentPage = 0;
    private boolean enabled = false;

    public void addFilterItem(ItemStack item, boolean exactMatch) {
        filterItems.add(new FilterItem(item, exactMatch));
    }

    public void removeFilterItem(ItemStack item) {
        filterItems.removeIf(filterItem -> filterItem.getItem().isSimilar(item));
    }

    public FilterItem getFilterItem(ItemStack item) {
        for (FilterItem filterItem : filterItems) {
            if (filterItem.getItem().isSimilar(item)) {
                return filterItem;
            }
        }
        return null;
    }

    public boolean isItemAllowed(ItemStack item) {
        for (FilterItem filterItem : filterItems) {
            if (filterItem.getItem().isSimilar(item)) {
                return whitelist;
            }
        }
        return !whitelist;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (FilterItem item : filterItems) {
            serializedItems.add(item.serialize());
        }
        map.put("filterItems", serializedItems);
        map.put("whitelist", whitelist);
        map.put("currentPage", currentPage);
        map.put("enabled", enabled);
        return map;
    }

    public static HopperData deserialize(Map<String, Object> map) {
        HopperData data = new HopperData();

        try {
            if (map.containsKey("filterItems")) {
                List<?> rawList = (List<?>) map.get("filterItems");
                for (Object itemObj : rawList) {
                    if (itemObj instanceof Map) {
                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                        data.filterItems.add(FilterItem.deserialize(itemMap));
                    }
                }
            }

            data.whitelist = (boolean) map.getOrDefault("whitelist", true);
            data.currentPage = (int) map.getOrDefault("currentPage", 0);
            data.enabled = (boolean) map.getOrDefault("enabled", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
