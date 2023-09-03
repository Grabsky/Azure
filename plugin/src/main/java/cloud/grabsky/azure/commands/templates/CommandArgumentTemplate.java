/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.commands.templates;

import cloud.grabsky.azure.commands.arguments.DirectionArgument;
import cloud.grabsky.azure.commands.arguments.GameModeArgument;
import cloud.grabsky.azure.commands.arguments.GameRuleArgument;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.commands.arguments.WorldEnvironmentArgument;
import cloud.grabsky.azure.commands.arguments.WorldTypeArgument;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.commands.RootCommandManager;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public enum CommandArgumentTemplate implements Consumer<RootCommandManager> {
    /* SINGLETON */ INSTANCE;

    @Override
    public void accept(final @NotNull RootCommandManager manager) {
        // org.bukkit.WorldType
        manager.setArgumentParser(WorldType.class, WorldTypeArgument.INSTANCE);
        manager.setCompletionsProvider(WorldType.class, WorldTypeArgument.INSTANCE);
        // org.bukkit.World.Environment
        manager.setArgumentParser(World.Environment.class, WorldEnvironmentArgument.INSTANCE);
        manager.setCompletionsProvider(World.Environment.class, WorldEnvironmentArgument.INSTANCE);
        // org.bukkit.GameRule
        manager.setArgumentParser(GameRule.class, GameRuleArgument.INSTANCE);
        manager.setCompletionsProvider(GameRule.class, GameRuleArgument.INSTANCE);
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
