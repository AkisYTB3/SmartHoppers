package org.notionsmp.smarthoppers.managers;

import com.nexomc.nexo.api.NexoItems;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.notionsmp.smarthoppers.SmartHoppers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class ItemManager {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final NamespacedKey HOPPER_KEY = new NamespacedKey(SmartHoppers.getInstance(), "hopper_item");
    private ItemStack hopperItem;
    private boolean isNexoItem = false;
    private String nexoItemId = null;

    public ItemManager() {
        createHopperItem();
    }

    public void createHopperItem() {
        FileConfiguration config = SmartHoppers.getInstance().getConfigManager().getConfig();
        String materialStr = config.getString("hopper-item.material");

        if (materialStr.startsWith("nexo-")) {
            nexoItemId = materialStr.substring("nexo-".length());
            try {
                hopperItem = Objects.requireNonNull(NexoItems.itemFromId(nexoItemId)).build();
                isNexoItem = true;
            } catch (Exception e) {
                hopperItem = new ItemStack(Material.IRON_INGOT);
                isNexoItem = false;
            }
        } else {
            Material material = Material.valueOf(materialStr);
            hopperItem = new ItemStack(material);
            isNexoItem = false;
        }

        ItemMeta meta = hopperItem.getItemMeta();

        if (!isNexoItem) {
            meta.getPersistentDataContainer().set(HOPPER_KEY, PersistentDataType.BOOLEAN, true);
        }

        if (config.contains("hopper-item.itemname")) {
            meta.displayName(parseMiniMessage(config.getString("hopper-item.itemname")));
        }
        if (config.contains("hopper-item.lore")) {
            List<Component> lore = config.getStringList("hopper-item.lore").stream()
                    .map(this::parseMiniMessage)
                    .collect(Collectors.toList());
            meta.lore(lore);
        }
        if (config.contains("hopper-item.custom-model-data")) {
            meta.setCustomModelData(config.getInt("hopper-item.custom-model-data"));
        }
        if (config.getBoolean("hopper-item.Components.glint", false)) {
            meta.setEnchantmentGlintOverride(true);
        }

        if (isComponentsSystemSupported()) {
            applyModernComponents(meta, config);
        }

        hopperItem.setItemMeta(meta);
    }

    public static boolean isHopperItem(ItemStack item) {
        if (item == null) return false;

        String nexoId = NexoItems.idFromItem(item);
        if (nexoId != null) {
            ItemManager instance = SmartHoppers.getInstance().getItemManager();
            return nexoId.equals(instance.nexoItemId);
        }

        return item.hasItemMeta() &&
                item.getItemMeta().getPersistentDataContainer().has(HOPPER_KEY, PersistentDataType.BOOLEAN);
    }

    private Component parseMiniMessage(String text) {
        Component component = miniMessage.deserialize(text);
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE);
    }

    private void applyModernComponents(ItemMeta meta, FileConfiguration config) {
        try {
            if (config.contains("hopper-item.Components.hide_tooltip")) {
                setHideTooltip(meta, config.getBoolean("hopper-item.Components.hide_tooltip"));
            }
            if (config.contains("hopper-item.Components.itemmodel") && isItemModelSupported()) {
                meta.getClass().getMethod("setItemModel", NamespacedKey.class)
                        .invoke(meta, NamespacedKey.fromString(config.getString("hopper-item.Components.itemmodel")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isComponentsSystemSupported() {
        try {
            Class.forName("org.bukkit.inventory.meta.components.DataComponentType");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void setHideTooltip(ItemMeta meta, boolean hide) {
        try {
            if (isComponentsSystemSupported()) {
                Class<?> dataComponentType = Class.forName("org.bukkit.inventory.meta.components.DataComponentType");
                Class<?> tooltipDisplay = Class.forName("io.papermc.paper.datacomponent.item.TooltipDisplay");
                Object builder = tooltipDisplay.getMethod("tooltipDisplay").invoke(null);
                builder.getClass().getMethod("hideTooltip", boolean.class).invoke(builder, hide);
                Object component = dataComponentType.getField("TOOLTIP").get(null);
                meta.getClass().getMethod("set", dataComponentType, Object.class)
                        .invoke(meta, component, builder.getClass().getMethod("build").invoke(builder));
            } else {
                Method m = meta.getClass().getMethod("setHideTooltip", boolean.class);
                m.setAccessible(true);
                m.invoke(meta, hide);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isItemModelSupported() {
        try {
            ItemMeta.class.getMethod("setItemModel", NamespacedKey.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}