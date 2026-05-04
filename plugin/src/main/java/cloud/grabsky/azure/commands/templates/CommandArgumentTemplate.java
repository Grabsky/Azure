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
package cloud.grabsky.azure.commands.templates;

import cloud.grabsky.azure.commands.arguments.DirectionArgument;
import cloud.grabsky.azure.commands.arguments.GameModeArgument;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.commands.RootCommandManager;
import org.bukkit.GameMode;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public enum CommandArgumentTemplate implements Consumer<RootCommandManager> {
    /* SINGLETON */ INSTANCE;

    @Override
    public void accept(final @NotNull RootCommandManager manager) {
        // org.bukkit.GameMode
        manager.setArgumentParser(GameMode.class, GameModeArgument.INSTANCE);
        manager.setCompletionsProvider(GameMode.class, GameModeArgument.INSTANCE);
        // cloud.grabsky.azure.commands.arguments.DirectionArgument.Direction
        manager.setArgumentParser(DirectionArgument.Direction.class, DirectionArgument.INSTANCE);
        manager.setCompletionsProvider(DirectionArgument.Direction.class, DirectionArgument.INSTANCE);
        // cloud.grabsky.bedrock.util.Interval
        manager.setArgumentParser(Interval.class, IntervalArgument.DEFAULT_RANGE);
        manager.setCompletionsProvider(Interval.class, IntervalArgument.DEFAULT_RANGE);
    }

}
