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
import cloud.grabsky.azure.user.AzureUser;
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
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@Command(name = "vanish", permission = "azure.command.vanish", usage = "/vanish (target) (state)")
public final class VanishCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    private static final ExceptionHandler.Factory VANISH_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_VANISH_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> CompletionsProvider.of(Boolean.class);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Handling command without any arguments specified, which is equal to "toggle-vanish-for-myself".
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            // Getting User of the sender.
            final AzureUser user = (AzureUser) plugin.getUserCache().getUser(sender);
            // Computing next vanish state.
            final boolean nextVanishState = !user.isVanished();
            // Changing vanish state.
            user.setVanished(nextVanishState);
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextVanishState == true)).send(sender);
            return;
        }
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting arguments.
        final Player target = arguments.next(Player.class).asRequired(VANISH_USAGE);
        final Boolean state = arguments.next(Boolean.class).asOptional();
        // Exiting command block in case specified target is different from sender, and sender does not have permissions to "use" other players.
        if (sender != target && context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // Getting User object of the target.
        final AzureUser targetUser = (AzureUser) plugin.getUserCache().getUser(target);
        // Computing next vanish state.
        final boolean nextVanishState = (state != null) ? state : !targetUser.isVanished();
        // Changing vanish state.
        targetUser.setVanished(nextVanishState);
        // Sending success message to the sender. (if applicable)
        if (sender != target)
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS)
                    .placeholder("player", target)
                    .placeholder("state", getColoredBooleanLong(nextVanishState == true))
                    .send(sender);
        // Sending success message to the target.
        Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextVanishState == true)).send(target);
    }

    private Component getColoredBooleanLong(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanLong(true).color(GREEN) : PluginLocale.getBooleanLong(false).color(RED);
    }

}
