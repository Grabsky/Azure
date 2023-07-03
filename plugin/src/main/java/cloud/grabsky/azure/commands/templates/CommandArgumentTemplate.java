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
