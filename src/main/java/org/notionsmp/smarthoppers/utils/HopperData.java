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
        FilterItem newFilter = new FilterItem(item.clone(), exactMatch);

        boolean alreadyExists = filterItems.stream().anyMatch(fi -> {
            if (exactMatch) {
                return fi.getItem().equals(newFilter.getItem());
            } else {
                return fi.getItem().getType() == newFilter.getItem().getType();
            }
        });

        if (!alreadyExists) {
            filterItems.add(newFilter);
        }
    }

    public void removeFilterItem(ItemStack item) {
        filterItems.removeIf(fi -> fi.getItem().equals(item));
    }

    public FilterItem getFilterItem(ItemStack item) {
        return filterItems.stream()
                .filter(fi -> fi.getItem().equals(item))
                .findFirst()
                .orElse(null);
    }

    public boolean isItemAllowed(ItemStack item) {
        return filterItems.stream()
                .filter(fi -> fi.isExactMatch() ? fi.getItem().equals(item) : fi.getItem().getType() == item.getType())
                .findFirst()
                .map(fi -> whitelist)
                .orElse(!whitelist);
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

    @SuppressWarnings("unchecked")
    public static HopperData deserialize(Map<String, Object> map) {
        HopperData data = new HopperData();
        if (map.containsKey("filterItems")) {
            Object rawList = map.get("filterItems");
            if (rawList instanceof List<?> list) {
                for (Object itemObj : list) {
                    if (itemObj instanceof Map<?, ?> itemMap) {
                        data.filterItems.add(FilterItem.deserialize((Map<String, Object>) itemMap));
                    }
                }
            }
        }
        if (map.containsKey("whitelist")) {
            data.whitelist = (boolean) map.get("whitelist");
        }
        if (map.containsKey("currentPage")) {
            data.currentPage = (int) map.get("currentPage");
        }
        if (map.containsKey("enabled")) {
            data.enabled = (boolean) map.get("enabled");
        }
        return data;
    }
}