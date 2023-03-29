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

