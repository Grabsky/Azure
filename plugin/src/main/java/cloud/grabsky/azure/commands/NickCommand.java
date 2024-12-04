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
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

@Command(name = "nick", permission = "azure.command.nick", usage = "/nick (player) (nick)")
public final class NickCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(
            TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.gradient(),
                    StandardTags.rainbow()
            )
    ).build();

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? (context.getExecutor().hasPermission("azure.command.nick.others") == true)
                        ? CompletionsProvider.of(Player.class)
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting next argument as Player which defaults to command sender if invalid.
        final Player target = (arguments.peek().next(Player.class).asNullable() != null)
                ? arguments.next(Player.class).asRequired()
                : context.getExecutor().asPlayer();
        // Getting next argument as raw (unparsed) nick / display-name.
        final @Nullable String nick = arguments.next(String.class).asOptional(null);
        // Sending error message if specified target is different from the sender, and sender does not have permissions to execute this command on other players.
        if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // Resetting display-name if not specified.
        if (nick == null) {
            // Sending message to the sender. If applicable.
            if (sender != target)
                Message.of(PluginLocale.COMMAND_NICK_SUCCESS_RESET).placeholder("player", target).send(sender);
            // Resetting target's display name.
            plugin.getUserCache().getUser(target).setDisplayName(null, null);
            // Sending message to the target.
            Message.of(PluginLocale.COMMAND_NICK_SUCCESS_RESET_TARGET).send(sender);
            return;
        }
        // Deserializing provided String to a Component.
        final Component displayName = MINI_MESSAGE.deserialize(nick.replace(" ", ""));
        // Sending an error message if nick contains invalid formatting or is not equal to the player's name. Name modifications are not allowed, only colors.
        if (nick.contains("&") == true || nick.contains("§") == true || plainText().serialize(displayName).equals(target.getName()) == false) {
            Message.of(PluginLocale.COMMAND_NICK_FAILURE_INVALID_FORMAT).placeholder("player", target).send(sender);
            return;
        }
        // Setting target's display-name.
        plugin.getUserCache().getUser(target).setDisplayName(nick, displayName);
        // Sending message to the sender. If applicable.
        if (sender != target)
            Message.of(PluginLocale.COMMAND_NICK_SUCCESS_SET).placeholder("player", target).send(sender);
        // Sending message to the target.
        Message.of(PluginLocale.COMMAND_NICK_SUCCESS_SET_TARGET).placeholder("nick", displayName).send(sender);
    }

}
