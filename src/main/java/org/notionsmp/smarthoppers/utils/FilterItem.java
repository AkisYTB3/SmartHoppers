package org.notionsmp.smarthoppers.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class FilterItem implements ConfigurationSerializable {
    private ItemStack item;
    private boolean exactMatch;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("exactMatch", exactMatch);
        return map;
    }

    public static FilterItem deserialize(Map<String, Object> map) {
        ItemStack item = (ItemStack) map.get("item");
        boolean exactMatch = (boolean) map.getOrDefault("exactMatch", false);
        return new FilterItem(item, exactMatch);
    }
}