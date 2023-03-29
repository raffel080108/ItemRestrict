/*
 *   Copyright 2023 Raphael Roehrig (alias "raffel080108"). All rights reserved.
 *   Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 *   You may not use the contents of this file or any other file that is part of this software, except in compliance with the license.
 *   You can obtain a copy of the license at https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package raffel080108.itemrestrict.commands;

import raffel080108.itemrestrict.ItemRestrict;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

public final class ReloadCommand {
    private final ItemRestrict main;

    public ReloadCommand(ItemRestrict main) {
        this.main = main;
    }

    @Command("itemRestrict reload")
    @CommandPermission("itemRestrict.reload")
    private void reloadCommand(CommandActor sender) {
        main.saveDefaultConfig();
        main.reloadConfig();
        sender.reply("§aConfiguration reloaded");
        main.getLogger().info("Configuration reloaded by " + sender.getName());
    }
}
