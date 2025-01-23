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
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command(name = "back", permission = "azure.command.back", usage = "/back")
public final class BackCommand extends RootCommand {

    private static final NamespacedKey PREVIOUS_LOCATION = new NamespacedKey("azure", "previous_location");

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting the previous location of the player.
        final @Nullable Location previousLocation = sender.getPersistentDataContainer().get(PREVIOUS_LOCATION, DataType.LOCATION);
        // Checking if previous location is not null.
        if (previousLocation == null) {
            Message.of(PluginLocale.COMMAND_BACK_FAILURE_NO_PREVIOUS_LOCATION).send(sender);
            return;
        }
        // Getting the current location of the player.
        final Location currentLocation = sender.getLocation();
        // Teleporting to the previous location.
        sender.teleportAsync(previousLocation, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(isSuccess -> {
            // Updating the previous location if teleportation was successful.
            if (isSuccess == true) sender.getPersistentDataContainer().set(PREVIOUS_LOCATION, DataType.LOCATION, currentLocation);
        });
        // Sending message to command sender.
        Message.of(PluginLocale.COMMAND_BACK_SUCCESS).send(sender);
    }

}
