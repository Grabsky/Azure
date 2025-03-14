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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

@Command(name = "hat", permission = "azure.command.hat", usage = "/hat")
public final class HatCommand extends RootCommand {

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting the held item and helmet from the player.
        final ItemStack held = sender.getInventory().getItemInMainHand();
        final ItemStack helmet = sender.getInventory().getHelmet();
        // Replacing helmet with the held item.
        sender.getInventory().setItemInMainHand(helmet);
        sender.getInventory().setHelmet(held);
        // Sending success message to the player.
        Message.of(PluginLocale.COMMAND_HAT_SUCCESS).send(sender);
    }

}
