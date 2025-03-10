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
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "unmute", permission = "azure.command.unmute", usage = "/unmute (player)")
public final class UnmuteCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return switch (index) {
            case 0 -> (_) -> plugin.getUserCache().getUsers().stream().filter(User::isMuted).map(User::getName).toList();
            case 1 -> context.getInput().getInput().trim().endsWith("--silent") == false ? CompletionsProvider.of("--silent") : CompletionsProvider.EMPTY;
            default -> CompletionsProvider.EMPTY;
        };
    }

    private static final ExceptionHandler.Factory UNMUTE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_UNMUTE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting OfflinePlayer argument, this can be either a player name or their unique id.
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(UNMUTE_USAGE);
        // ...
        final User targetUser = plugin.getUserCache().getUser(target.getUniqueId());
        // Whether the action is silent.
        final boolean isSilent = arguments.next(String.class).asOptional("--non-silent").equalsIgnoreCase("--silent");
        // ...
        if (targetUser != null) {
            // Checking if player is muted.
            if (targetUser.isMuted() == true) {
                // Unmuting the player.
                targetUser.unmute(sender);
                // Sending success message to the sender.
                Message.of(PluginLocale.COMMAND_UNMUTE_SUCCESS)
                        .placeholder("player", targetUser.getName())
                        .broadcast(receiver -> isSilent == false || receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
                // Forwarding to Discord...
                if (isSilent == false && PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
                    // Constructing the message.
                    final String message = PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_UNMUTE_FORMAT
                            .replace("<name>", targetUser.getName())
                            .replace("<issuer>", sender instanceof Player ? sender.getName() : "Console");
                    // Forwarding the message through configured webhook.
                    if (message.isEmpty() == false)
                        plugin.getDiscordIntegration().getWebhookForwardingPunishments().send(message);
                }
                return;
            }
            // Sending failure message to the sender.
            Message.of(PluginLocale.COMMAND_UNMUTE_FAILURE_PLAYER_NOT_BANNED).send(sender);
            return;
        }
        Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER).send(sender);
    }

}
