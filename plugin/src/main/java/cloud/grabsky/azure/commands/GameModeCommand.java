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

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "gamemode", permission = "azure.command.gamemode", usage = "/gamemode (player) (gamemode)")
public final class GameModeCommand extends RootCommand {

    @Dependency
    private @UnknownNullability UserCache userCache;


    private static final ExceptionHandler.Factory GAMEMODE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_GAMEMODE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == true)
                            ? CompletionsProvider.of(Player.class)
                            : CompletionsProvider.of("@self");
            case 1 -> CompletionsProvider.of(GameMode.class);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final Player target = arguments.next(Player.class).asRequired(GAMEMODE_USAGE);
        final @Nullable GameMode mode = arguments.next(GameMode.class).asOptional();
        // ...
        if (mode == null) {
            Message.of(PluginLocale.COMMAND_GAMEMODE_INFO)
                    .placeholder("player", target)
                    .placeholder("mode", PluginLocale.getGameMode(target.getGameMode()))
                    .send(sender);
            return;
        }
        // ...
        if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        target.setGameMode(mode);
        // ...
        if (sender != target)
            Message.of(PluginLocale.COMMAND_GAMEMODE_SET_SUCCESS_SENDER)
                    .placeholder("player", target)
                    .placeholder("mode", PluginLocale.getGameMode(mode))
                    .send(sender);
        // ...
        Message.of(PluginLocale.COMMAND_GAMEMODE_SET_SUCCESS_TARGET)
                .placeholder("mode", PluginLocale.getGameMode(mode))
                .send(target);
        // Getting user object.
        final User targetUser = userCache.getUser(target);
        // Disabling vanish state if enabled.
        if (targetUser.isVanished() == true) {
            targetUser.setVanished(false, false);
            // Sending message to the player.
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", PluginLocale.DISABLED.color(NamedTextColor.RED)).send(target);
        }
    }

}
