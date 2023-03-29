/*
 *
 *     Copyright 2023 Raphael Roehrig (raffel080108)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *      “Commons Clause” License Condition v1.0
 *
 *      This Software is provided to you by the Licensor under the License, as defined above, subject to the following condition.
 *
 *      Without limiting other conditions in the License, the grant of rights under the License will not include, and the License does not grant to you, right to Sell the Software.
 *
 *      For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you under the License to provide to third parties, for a fee or other consideration (including without limitation fees for hosting or consulting/ support services related to the Software), a product or service whose value derives, entirely or substantially, from the functionality of the Software.
 *
 *      License: Apache License Version 2.0
 *      Licensor: Raphael Roehrig (raffel080108)
 *
 */

package me.raffel080108.itemrestrict.listeners;

import me.raffel080108.itemrestrict.ItemRestrict;
import me.raffel080108.itemrestrict.utils.Utils;
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
