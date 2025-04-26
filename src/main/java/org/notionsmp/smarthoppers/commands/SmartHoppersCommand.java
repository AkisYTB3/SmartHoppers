package org.notionsmp.smarthoppers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;
import org.notionsmp.smarthoppers.SmartHoppers;

@CommandAlias("smarthoppers|sh")
public class SmartHoppersCommand extends BaseCommand {

    @Default
    public void onDefault(Player player) {
        player.sendMessage("Usage: /smarthoppers <item|reload>");
    }

    @Subcommand("item")
    @CommandPermission("smarthoppers.item")
    public void onItem(Player player) {
        player.getInventory().addItem(SmartHoppers.getInstance().getItemManager().getHopperItem());
    }

    @Subcommand("reload")
    @CommandPermission("smarthoppers.reload")
    public void onReload(Player player) {
        SmartHoppers.getInstance().getConfigManager().reloadConfigs();
        SmartHoppers.getInstance().getItemManager().createHopperItem();
        SmartHoppers.getInstance().getGuiManager().loadGUIItems();
        player.sendMessage("Configs reloaded!");
    }
}