/*
 *   Copyright 2023 Raphael Roehrig (alias "raffel080108"). All rights reserved.
 *   Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 *   You may not use the contents of this file or any other file that is part of this software, except in compliance with the license.
 *   You can obtain a copy of the license at https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package raffel080108.itemrestrict.listeners;

import raffel080108.itemrestrict.ItemRestrict;
import raffel080108.itemrestrict.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class InventoryCloseListener implements Listener {
    private final ItemRestrict main;

    public InventoryCloseListener(ItemRestrict main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void inventoryCloseEvent(InventoryCloseEvent event) {
        ConfigurationSection restrictedItems = main.getConfig().getConfigurationSection("restricted-items");
        if (restrictedItems == null) {
            main.getLogger().severe("Could not find configuration-section \"restricted-items\", while attempting to process item-pickup event");
            return;
        }

        Player player = (Player) event.getPlayer();
        if (player.hasPermission("itemRestrict.bypass"))
            return;

        Set<String> restrictedItemsKeys = restrictedItems.getKeys(false);
        ItemStack cursor = player.getItemOnCursor();
        Material material = cursor.getType();
        String materialName = material.name();
        if (!restrictedItemsKeys.contains(materialName))
            return;

        Utils utils = new Utils();
        Inventory inventory = player.getInventory();
        EntityEquipment equipment = player.getEquipment();
        int maxAmount = restrictedItems.getInt(materialName);
        int currentAmount = utils.getCurrentAmount(material, inventory.getContents()) + utils.getCurrentAmount(material, equipment.getArmorContents());
        ItemStack offHand = equipment.getItemInOffHand();
        if (offHand.getType().equals(material))
            currentAmount += offHand.getAmount();

        if (currentAmount >= maxAmount || cursor.getAmount() + currentAmount > maxAmount) {
            player.setItemOnCursor(null);
            player.getWorld().dropItem(player.getLocation(), cursor);
        }
    }
}

