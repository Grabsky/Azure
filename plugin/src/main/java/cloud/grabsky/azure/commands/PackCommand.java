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
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.RootCommandInput;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "pack", permission = "azure.command.pack", usage = "/pack (...)")
public final class PackCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final RootCommandInput input = context.getInput();
        // Returning list of sub-commands when no argument was specified in the input.
        if (index == 0) return CompletionsProvider.of(
                Stream.of("apply", "notify")
                        .filter(literal -> sender.hasPermission(this.getPermission() + "." + literal) == true)
                        .toList()
        );
        // Getting the first literal (argument) of user input.
        final String literal = input.at(1, "").toLowerCase();
        // Returning empty completions provider when missing permission for that literal.
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // Returning sub-command-aware completions provider.
        return switch (literal) {
            case "apply" -> {
                yield (sender.hasPermission(this.getPermission() + ".apply.others") == true)
                        ? CompletionsProvider.of(Player.class)
                        : CompletionsProvider.EMPTY;
            }
            case "notify" -> CompletionsProvider.of("--confirm");
            // No completions by default.
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        switch (arguments.next(String.class).asOptional("help").toLowerCase()) {
            case "apply" -> {
                // Getting command sender as player.
                final Player sender = context.getExecutor().asPlayer();
                // Getting the target.
                final @NotNull Player target = arguments.next(Player.class).asOptional(sender);
                // Checking if command sender can target other players.
                if (sender != target && sender.hasPermission(this.getPermission() + ".apply.others") == false) {
                    Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                    return;
                }
                plugin.getResourcePackManager().sendResourcePacks(target);
            }
            case "notify" -> {
                final CommandSender sender = context.getExecutor().asCommandSender();
                // ...
                if (sender.hasPermission(this.getPermission() + ".notify") == true) {
                    final boolean isConfirm = arguments.next(String.class).asOptional("").equalsIgnoreCase("--confirm");
                    // Checking for --confirm flag.
                    if (isConfirm == true) {
                        // Getting the server audience.
                        final Server server = plugin.getServer();
                        // Playing notification sound.
                        if (PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND != null)
                            server.playSound(PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND);
                        // Sending notification message.
                        Message.of(PluginLocale.COMMAND_PACK_NOTIFICATION).send(server);
                        return;
                    }
                    Message.of(PluginLocale.COMMAND_PACK_NOTIFY_CONFIRM).replace("<input>", context.getInput().toString()).send(sender);
                    return;
                }
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            }
            // Showing help page when invalid argument is provided.
            default -> Message.of(PluginLocale.COMMAND_PACK_HELP).send(context.getExecutor().asCommandSender());
        }
    }

}
