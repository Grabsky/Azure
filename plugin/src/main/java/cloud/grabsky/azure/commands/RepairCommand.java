/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;

import org.jetbrains.annotations.NotNull;

@Command(name = "repair", permission = "azure.command.repair", usage = "/repair")
public final class RepairCommand extends RootCommand {

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Checking if item in hand is not air.
        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
            // Checking if item in hand can be repaired.
            if (sender.getInventory().getItemInMainHand().getType().getMaxDurability() > 1) {
                // Repairing item and sending success message to the sender.
                sender.getInventory().getItemInMainHand().editMeta(Damageable.class, (meta) -> meta.setDamage(0));
                Message.of(PluginLocale.COMMAND_REPAIR_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.COMMAND_REPAIR_FAILURE_ITEM_NOT_REPAIRABLE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.COMMAND_REPAIR_FAILURE_NO_ITEM_IN_HAND).send(sender);
    }

}
