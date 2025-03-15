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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.entity.Player;

@Command(name = "schedule_restart", permission = "azure.command.schedule_restart", usage = "/schedule_restart (time)")
public final class ScheduleRestartCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(IntervalArgument.ofRange(1, 60, Unit.MINUTES)) : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting the specified interval before the restart is initiated.
        final Interval interval = arguments.next(Interval.class, IntervalArgument.ofRange(1, 60, Unit.MINUTES)).asOptional(Interval.of(60, Unit.SECONDS));
        // Sending immediate message to all players.
        Message.of(PluginLocale.COMMAND_SCHEDULE_RESTART_INITIAL).placeholder("time_left", interval).broadcast();
        // Scheduling the reminder of restart.
        plugin.getBedrockScheduler().run((long) interval.remove(15, Unit.SECONDS).as(Unit.TICKS), (_) -> {
            Message.of(PluginLocale.COMMAND_SCHEDULE_RESTART_REMINDER).placeholder("time_left", interval).broadcast();
        });
        // Scheduling the restart itself.
        plugin.getBedrockScheduler().run((long) interval.as(Unit.TICKS), (_) -> {
            plugin.getServer().shutdown();
        });
    }

}
