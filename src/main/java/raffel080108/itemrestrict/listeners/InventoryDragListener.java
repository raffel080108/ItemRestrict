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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public final class InventoryDragListener implements Listener {
    private final ItemRestrict main;

    public InventoryDragListener(ItemRestrict main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void inventoryDragEvent(InventoryDragEvent event) {
        ConfigurationSection restrictedItems = main.getConfig().getConfigurationSection("restricted-items");
        if (restrictedItems == null) {
            main.getLogger().severe("Could not find configuration-section \"restricted-items\", while attempting to process item-pickup event");
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (player.hasPermission("itemRestrict.bypass"))
            return;

        Inventory inventory = player.getInventory();
        EntityEquipment equipment = player.getEquipment();

        int amountToBeAdded = 0;
        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            int slot = entry.getKey(), topInventoryMaxIndex = event.getView().getTopInventory().getSize() - 1;

            if (slot <= topInventoryMaxIndex)
                continue;

            int newItemAmount = entry.getValue().getAmount();
            ItemStack previousItem = inventory.getItem(inventory.getSize() - 1 - (slot - topInventoryMaxIndex - 1));
            if (previousItem == null || previousItem.getType() == Material.AIR)
                amountToBeAdded += newItemAmount;
            else amountToBeAdded += previousItem.getAmount() - newItemAmount;
        }

        if (amountToBeAdded == 0)
            return;

        Set<String> restrictedItemsKeys = restrictedItems.getKeys(false);
        Utils utils = new Utils();

        Material material = event.getOldCursor().getType();
        String materialName = material.name();
        if (!restrictedItemsKeys.contains(materialName))
            return;

        int maxAmount = restrictedItems.getInt(materialName);
        int currentAmount = utils.getCurrentAmount(material, inventory.getContents()) + utils.getCurrentAmount(material, equipment.getArmorContents());
        ItemStack offHand = equipment.getItemInOffHand();
        if (offHand.getType().equals(material))
            currentAmount += offHand.getAmount();

        if (currentAmount >= maxAmount || amountToBeAdded + currentAmount > maxAmount)
            event.setCancelled(true);
    }
}
