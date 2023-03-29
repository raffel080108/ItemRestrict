/*
 *   Copyright 2023 Raphael Roehrig (alias "raffel080108"). All rights reserved.
 *   Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 *   You may not use any contents of this file or software except in compliance with the license.
 *   You can obtain a copy of the license at https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package raffel080108.itemrestrict;

import raffel080108.itemrestrict.commands.ReloadCommand;
import raffel080108.itemrestrict.listeners.InventoryClickListener;
import raffel080108.itemrestrict.listeners.InventoryCloseListener;
import raffel080108.itemrestrict.listeners.InventoryDragListener;
import raffel080108.itemrestrict.listeners.PlayerPickupItemListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.logging.Logger;

public final class ItemRestrict extends JavaPlugin {

    @Override
    public void onEnable() {
        Logger log = getLogger();
        log.info("Loading configuration...");
        saveDefaultConfig();
        reloadConfig();

        log.info("Setting up events & commands...");
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerPickupItemListener(this), this);
        pluginManager.registerEvents(new InventoryClickListener(this), this);
        pluginManager.registerEvents(new InventoryDragListener(this), this);
        pluginManager.registerEvents(new InventoryCloseListener(this), this);

        BukkitCommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.register(new ReloadCommand(this));
        commandHandler.registerBrigadier();

        log.info("Plugin started!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin stopped!");
    }
}
