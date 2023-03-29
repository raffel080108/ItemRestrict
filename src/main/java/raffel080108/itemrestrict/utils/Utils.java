/*
 *   Copyright 2023 Raphael Roehrig (alias "raffel080108"). All rights reserved.
 *   Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 *   You may not use the contents of this file or any other file that is part of this software, except in compliance with the license.
 *   You can obtain a copy of the license at https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package raffel080108.itemrestrict.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Utils {
    public int getCurrentAmount(Material material, ItemStack[] items) {
        int currentAmount = 0;
        for (ItemStack item : items) {
            if (item == null)
                continue;
            if (item.getType().equals(material))
                currentAmount += item.getAmount();
        }
        return currentAmount;
    }
}
