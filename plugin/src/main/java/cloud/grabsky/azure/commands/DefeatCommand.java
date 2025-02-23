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
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

@Command(name = "defeat", permission = "azure.command.defeat", usage = "/defeat (--confirm)")
public final class DefeatCommand extends RootCommand {

    // Responsible for storing cooldowns for each player.
    private final Map<UUID, Long> cooldown = new HashMap<>();

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of("--confirm") : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Checking presence of the '--confirm' flag.
        final boolean isConfirm = arguments.next(String.class).asOptional("--no-confirm").equalsIgnoreCase("--confirm") == true;
        // Sending confirmation message to the sender..
        if (isConfirm == false) {
            Message.of(PluginLocale.COMMAND_DEFEAT_CONFIRM).replace("<input>", context.getInput().toString()).send(sender);
            return;
        }
        // Calculating time that is left for the sender to use the command again.
        final Interval timeLeft = Interval.between(cooldown.getOrDefault(sender.getUniqueId(), 0L) + 60_000L, System.currentTimeMillis(), Unit.MILLISECONDS);
        // Returning if the sender is on cooldown.
        if (timeLeft.as(Unit.MILLISECONDS) > 0) {
            Message.of(PluginLocale.COMMAND_ON_COOLDOWN).placeholder("time_left", timeLeft).send(sender);
            return;
        }
        // Setting the cooldown.
        cooldown.put(sender.getUniqueId(), System.currentTimeMillis());
        // Setting player's health to 0.
        sender.setHealth(0.0);
        // Sending success message to the sender.
        Message.of(PluginLocale.COMMAND_DEFEAT_SUCCESS).send(sender);
    }

}
