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

package me.raffel080108.itemrestrict.utils;

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
