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

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;

@Command(name = "azure", permission = "azure.command.azure", usage = "/azure (...)")
public final class AzureCommand extends RootCommand {

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return (index == 0)
                ? (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".reload") == true)
                        ? CompletionsProvider.of("reload")
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        if (queue.hasNext() == false) {
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_AZURE_HELP).send(sender);
            return;
        } else if (queue.next(String.class).asRequired().equalsIgnoreCase("reload") == true && sender.hasPermission(this.getPermission() + ".reload") == true) {
            if (Azure.getInstance().onReload() == true) {
                // Sending success message to command sender.
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}
