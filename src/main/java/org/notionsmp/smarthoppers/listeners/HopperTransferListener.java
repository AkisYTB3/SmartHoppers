package org.notionsmp.smarthoppers.listeners;

import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.notionsmp.smarthoppers.SmartHoppers;
import org.notionsmp.smarthoppers.utils.HopperData;

public class HopperTransferListener implements Listener {

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();
        Inventory initiator = event.getInitiator();

        if (destination.getHolder() instanceof Hopper destHopper) {
            HopperData hopperData = SmartHoppers.getInstance().getHopperManager().getHopperData(destHopper);
            if (hopperData == null || !hopperData.isEnabled()) return;

            if (initiator.equals(destination)) {
                if (!hopperData.isItemAllowed(event.getItem())) {
                    event.setCancelled(true);

                    SmartHoppers.getInstance().getServer().getScheduler().runTaskLater(SmartHoppers.getInstance(), () -> {
                        for (ItemStack item : source.getContents()) {
                            if (item == null || item.getType().isAir()) continue;
                            if (hopperData.isItemAllowed(item)) {
                                ItemStack cloned = item.clone();
                                cloned.setAmount(1);

                                destination.addItem(cloned);

                                item.setAmount(item.getAmount() - 1);
                                break;
                            }
                        }
                    }, 1L);
                }
            }
        }
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Hopper hopper)) return;

        HopperData hopperData = SmartHoppers.getInstance().getHopperManager().getHopperData(hopper);
        if (hopperData == null) return;
        if (!hopperData.isEnabled()) return;

        ItemStack item = event.getItem().getItemStack();
        if (!hopperData.isItemAllowed(item)) {
            event.setCancelled(true);
        }
    }
}
