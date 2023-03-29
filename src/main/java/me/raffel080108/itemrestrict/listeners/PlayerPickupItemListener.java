/*
 *   Copyright 2023 Raphael Roehrig (alias "raffel080108"). All rights reserved.
 *   Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 *   You may not use any content of this file or software except in compliance with the license.
 *   You can obtain a copy of the license at https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package me.raffel080108.itemrestrict.listeners;

import me.raffel080108.itemrestrict.ItemRestrict;
import me.raffel080108.itemrestrict.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class PlayerPickupItemListener implements Listener {
    private final ItemRestrict main;

    public PlayerPickupItemListener(ItemRestrict main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void playerAttemptPickupItemEvent(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled())
            return;

        ConfigurationSection restrictedItems = main.getConfig().getConfigurationSection("restricted-items");
        if (restrictedItems == null) {
            main.getLogger().severe("Could not find configuration-section \"restricted-items\", while attempting to process item-pickup event");
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("itemRestrict.bypass"))
            return;

        Item itemEntity = event.getItem();
        ItemStack itemToPickup = itemEntity.getItemStack();
        Material itemMaterial = itemToPickup.getType();
        String itemMaterialName = itemMaterial.name();
        if (!restrictedItems.getKeys(false).contains(itemMaterialName))
            return;

        Utils utils = new Utils();
        EntityEquipment equipment = player.getEquipment();
        Inventory inventory = player.getInventory();
        int maxAmount = restrictedItems.getInt(itemMaterialName);
        int currentAmount = utils.getCurrentAmount(itemMaterial, inventory.getContents()) +
                utils.getCurrentAmount(itemMaterial, equipment.getArmorContents());
        ItemStack offHand = equipment.getItemInOffHand();
        if (offHand.getType() == itemMaterial)
            currentAmount += offHand.getAmount();

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (topInventory.getType() == InventoryType.CRAFTING)
            currentAmount += utils.getCurrentAmount(itemMaterial, topInventory.getContents());

        ItemStack cursorItem = player.getItemOnCursor();
        if (cursorItem.getType() == itemMaterial)
            currentAmount += cursorItem.getAmount();

        if (currentAmount >= maxAmount) {
            event.setCancelled(true);
            return;
        }

        ItemStack offhand = player.getEquipment().getItemInOffHand();
        if (offhand.getType() == itemMaterial)
            currentAmount += offhand.getAmount();

        int pickupAmount = itemToPickup.getAmount(), newAmount = pickupAmount + currentAmount;
        if (newAmount > maxAmount) {
            event.setCancelled(true);
            player.playSound(itemEntity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, 2F);
            int amountTooMuch = newAmount - maxAmount;

            ItemStack newItem = itemToPickup.clone();
            newItem.setAmount(pickupAmount - amountTooMuch);
            inventory.addItem(newItem);

            itemToPickup.setAmount(amountTooMuch);
            itemEntity.setItemStack(itemToPickup);
        }
    }
}
