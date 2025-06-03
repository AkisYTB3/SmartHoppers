package org.notionsmp.smarthoppers.listeners;

import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.notionsmp.smarthoppers.SmartHoppers;
import org.notionsmp.smarthoppers.managers.ItemManager;
import org.notionsmp.smarthoppers.utils.FilterItem;
import org.notionsmp.smarthoppers.utils.HopperData;
import java.util.Objects;

public class HopperListener implements Listener {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (!(Objects.requireNonNull(event.getClickedBlock()).getState() instanceof Hopper hopper)) return;
        if (!event.getPlayer().hasPermission("smarthoppers.use")) return;
        if (!ProtectionLib.canInteract(event.getPlayer(), event.getClickedBlock().getLocation())
                || !ProtectionLib.canUse(event.getPlayer(), event.getClickedBlock().getLocation())) return;

        boolean useItem = SmartHoppers.getInstance().getConfigManager().getConfig().getBoolean("hopper-item.enabled");
        if (useItem) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (ItemManager.isHopperItem(item)) {
                event.setCancelled(true);
                SmartHoppers.getInstance().getGuiManager().openHopperGUI(event.getPlayer(), hopper);
            }
        } else if (event.getPlayer().isSneaking() && event.getPlayer().getInventory().getItemInMainHand().getType().isAir()) {
            event.setCancelled(true);
            SmartHoppers.getInstance().getGuiManager().openHopperGUI(event.getPlayer(), hopper);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        FileConfiguration guiConfig = SmartHoppers.getInstance().getConfigManager().getGuiConfig();
        String title = guiConfig.getString("settings.title");
        if (title == null || !event.getView().title().equals(miniMessage.deserialize(title))) return;
        event.setCancelled(true);
        HopperData hopperData = SmartHoppers.getInstance().getGuiManager().getPlayerEditingData(player);
        if (hopperData == null) return;
        int clickedSlot = event.getSlot();
        InventoryType clickedInventoryType = event.getClickedInventory().getType();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedInventoryType == InventoryType.CHEST) {
            if (clickedSlot == guiConfig.getInt("settings.slots.toggle")) {
                hopperData.setEnabled(!hopperData.isEnabled());
            } else if (clickedSlot == guiConfig.getInt("settings.slots.whitelist")) {
                hopperData.setWhitelist(!hopperData.isWhitelist());
            } else if (clickedSlot == guiConfig.getInt("settings.slots.next_page")) {
                hopperData.setCurrentPage(hopperData.getCurrentPage() + 1);
            } else if (clickedSlot == guiConfig.getInt("settings.slots.previous_page")) {
                hopperData.setCurrentPage(Math.max(0, hopperData.getCurrentPage() - 1));
            } else if (clickedItem != null && !clickedItem.getType().isAir()) {
                FilterItem filterItem = SmartHoppers.getInstance().getGuiManager().getFilterSlot(player, clickedSlot);
                if (filterItem != null) {
                    if (event.getClick() == ClickType.RIGHT) {
                        filterItem.setExactMatch(!filterItem.isExactMatch());
                    } else {
                        hopperData.removeFilterItem(filterItem.getItem());
                    }
                }
            }
            SmartHoppers.getInstance().getGuiManager().refreshHopperGUI(player, (Hopper) event.getInventory().getHolder());
        } else if (clickedInventoryType == InventoryType.PLAYER && clickedItem != null && !clickedItem.getType().isAir()) {
            ItemStack singleItem = clickedItem.clone();
            singleItem.setAmount(1);
            hopperData.addFilterItem(singleItem, false);
            SmartHoppers.getInstance().getGuiManager().refreshHopperGUI(player, (Hopper) event.getInventory().getHolder());
        }
    }

    @EventHandler
    public void onHopperBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Hopper hopper) {
            SmartHoppers.getInstance().getHopperManager().removeHopperData(hopper);
        }
    }
}
