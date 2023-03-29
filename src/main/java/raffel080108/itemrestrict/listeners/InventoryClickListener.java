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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;

public final class InventoryClickListener implements Listener {
    private final ItemRestrict main;

    public InventoryClickListener(ItemRestrict main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void inventoryClickEvent(InventoryClickEvent event) {
        ConfigurationSection restrictedItems = main.getConfig().getConfigurationSection("restricted-items");
        if (restrictedItems == null) {
            main.getLogger().severe("Could not find configuration-section \"restricted-items\", while attempting to process item-pickup event");
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null)
            return;

        Player player = (Player) event.getWhoClicked();
        if (player.hasPermission("itemRestrict.bypass"))
            return;

        Inventory inventory = player.getInventory();
        EntityEquipment equipment = player.getEquipment();
        boolean moveToOtherInventory = event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY);
        Set<String> restrictedItemsKeys = restrictedItems.getKeys(false);
        ItemStack clickedItem = event.getCurrentItem();
        Utils utils = new Utils();
        if (moveToOtherInventory) {
            if (clickedInventory.equals(inventory))
                return;
            if (clickedItem == null)
                return;

            Material material = clickedItem.getType();
            String materialName = material.name();
            if (!restrictedItemsKeys.contains(materialName))
                return;

            int maxAmount = restrictedItems.getInt(materialName);
            int currentAmount = utils.getCurrentAmount(material, inventory.getContents()) +
                    utils.getCurrentAmount(material, equipment.getArmorContents());

            ItemStack offHand = equipment.getItemInOffHand();
            if (offHand.getType() == material)
                currentAmount += offHand.getAmount();

            if (currentAmount >= maxAmount) {
                event.setCancelled(true);
                return;
            }

            int itemAmount = clickedItem.getAmount();
            int newAmount = currentAmount + itemAmount;
            if (newAmount > maxAmount) {
                event.setCancelled(true);
                int amountTooMuch = newAmount - (maxAmount - currentAmount);

                ItemStack newItem = clickedItem.clone();
                newItem.setAmount(itemAmount - amountTooMuch);

                int leftOver = 0;
                HashMap<Integer, ItemStack> unableToAddItems = inventory.addItem(newItem);
                if (!unableToAddItems.isEmpty())
                    leftOver = unableToAddItems.entrySet().iterator().next().getValue().getAmount();

                clickedItem.setAmount(amountTooMuch + leftOver);
                clickedInventory.setItem(event.getSlot(), clickedItem);
            }
            return;
        }

        if (!clickedInventory.equals(inventory))
            return;

        ItemStack cursor = event.getCursor();
        if (cursor == null)
            return;

        Material material = cursor.getType();
        String materialName = material.name();
        if (!restrictedItemsKeys.contains(materialName))
            return;

        int maxAmount = restrictedItems.getInt(materialName);
        int currentAmount = utils.getCurrentAmount(material, inventory.getContents()) + utils.getCurrentAmount(material, equipment.getArmorContents());
        ItemStack offHand = equipment.getItemInOffHand();
        if (offHand.getType() == material)
            currentAmount += offHand.getAmount();

        if (currentAmount >= maxAmount) {
            event.setCancelled(true);
            return;
        }

        int allowedExtraAmount = maxAmount - currentAmount;
        int cursorAmount = cursor.getAmount();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            int clickedItemAmount = clickedItem.getAmount();
            Material clickedItemMaterial = clickedItem.getType();
            if (clickedItemMaterial.equals(material)) {
                if (Math.min(clickedItemMaterial.getMaxStackSize() - clickedItemAmount, cursorAmount + clickedItemAmount) + currentAmount <= maxAmount)
                    return;

                event.setCancelled(true);

                int amountTooMuch = cursorAmount - allowedExtraAmount;
                ItemStack newItem = cursor.clone();
                newItem.setAmount(amountTooMuch);
                player.setItemOnCursor(newItem);

                clickedItem.setAmount(clickedItemAmount + (cursorAmount - amountTooMuch));
            }
            return;
        }

        if (cursorAmount > allowedExtraAmount) {
            event.setCancelled(true);

            int amountTooMuch = cursorAmount - allowedExtraAmount;
            ItemStack newCursorItem = cursor.clone();
            newCursorItem.setAmount(amountTooMuch);
            player.setItemOnCursor(newCursorItem);

            ItemStack itemToAdd = cursor.clone();
            itemToAdd.setAmount(allowedExtraAmount);
            inventory.setItem(event.getSlot(), itemToAdd);
        }
    }
}
