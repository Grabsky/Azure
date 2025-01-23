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

import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

// TO-DO: Disallow spying on players with higher group weight.
@Command(name = "admin_chat", aliases = "a", permission = "azure.command.adminchat", usage = "/a (message)")
public final class AdminChatCommand extends RootCommand {

    @Dependency
    private @UnknownNullability ChatManager chat;

    @Dependency
    private @UnknownNullability UserCache userCache;

    private static final ExceptionHandler.Factory ADMINCHAT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_ADMINCHAT_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting the rest of user input as a message.
        final String message = arguments.next(String.class, StringArgument.GREEDY).asRequired(ADMINCHAT_USAGE);
        // Sending message.
        Message.of(PluginConfig.CHAT_FORMATS_ADMIN)
                .placeholder("player", sender)
                .placeholder("displayname", sender.displayName())
                .placeholder("message", message)
                .broadcast(player -> player.hasPermission("azure.command.adminchat") == true);
    }

}
