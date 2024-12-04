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
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "verify", permission = "azure.command.verify", usage = "/verify")
public final class VerifyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;;

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? (context.getExecutor().hasPermission("azure.command.verify.send_button") == true)
                        ? CompletionsProvider.of("send_component")
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ENABLED == false || plugin.getDiscord() == null) {
            Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_NOT_ENABLED).send(context.getExecutor().asCommandSender());
            return;
        }
        // Handling 'send_component' sub-command, which is responsible for sending button component to desired channel.
        if (arguments.peek().hasNext() == true && arguments.nextString().equalsIgnoreCase("send_component") == true) {
            // Checking required permissions.
            if (context.getExecutor().hasPermission("azure.command.verify.send_component") == true) {
                // Getting the text channel from provided ID.
                final @Nullable TextChannel channel = plugin.getDiscord().getTextChannelById(arguments.nextString()).orElse(null);
                // Sending error message in case channel does not not exist or is inaccessible.
                if (channel == null) {
                    Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_SEND_COMPONENT_FAILURE_INVALID_CHANNEL).send(context.getExecutor().asCommandSender());
                    return;
                }
                // Creating a message with button component.
                final MessageBuilder builder = new MessageBuilder().addComponents(ActionRow.of(
                        Button.create("verification_button", PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_BUTTON_STYLE, PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_BUTTON_LABEL)
                ));
                // Sending...
                builder.send(channel);
            }
            return;
        }
        // Getting command executor as instance of Player.
        final Player sender = context.getExecutor().asPlayer();
        // Getting User instance of this player.
        final User user = plugin.getUserCache().getUser(sender);
        // Sending error message if player is already verified.
        if (user.getDiscordId() != null) {
            Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_ALREADY_VERIFIED).send(sender);
            return;
        }
        // Requesting a code and starting verification process.
        final String token = plugin.getVerificationManager().requestCode(sender.getUniqueId());
        // Sending prompt message to the player.
        Message.of(PluginLocale.COMMAND_VERIFY_PROMPT).replace("<code>", token).send(sender);
    }

}
