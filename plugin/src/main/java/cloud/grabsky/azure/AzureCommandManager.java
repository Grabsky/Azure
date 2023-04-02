package cloud.grabsky.azure;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.commands.argument.BooleanArgument;
import cloud.grabsky.commands.argument.DoubleArgument;
import cloud.grabsky.commands.argument.EnchantmentArgument;
import cloud.grabsky.commands.argument.EntityTypeArgument;
import cloud.grabsky.commands.argument.FloatArgument;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.argument.LongArgument;
import cloud.grabsky.commands.argument.MaterialArgument;
import cloud.grabsky.commands.argument.NamespacedKeyArgument;
import cloud.grabsky.commands.argument.OfflinePlayerArgument;
import cloud.grabsky.commands.argument.PlayerArgument;
import cloud.grabsky.commands.argument.PositionArgument;
import cloud.grabsky.commands.argument.ShortArgument;
import cloud.grabsky.commands.argument.UUIDArgument;
import cloud.grabsky.commands.argument.WorldArgument;
import cloud.grabsky.commands.exception.IncompatibleSenderException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class AzureCommandManager implements Consumer<RootCommandManager> {

    @Override
    public void accept(final @NotNull RootCommandManager manager) {

        manager.setExceptionHandler(IncompatibleSenderException.class, (e, context) -> {
            Message.of((e.getExpectedType() == Player.class)
                            ? PluginLocale.Commands.INVALID_EXECUTOR_PLAYER
                            : (e.getExpectedType() == ConsoleCommandSender.class)
                                    ? PluginLocale.Commands.INVALID_EXECUTOR_CONSOLE
                                    : null
            ).send(context.getExecutor().asCommandSender());
        });

        // Boolean Argument

        manager.setExceptionHandler(BooleanArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_BOOLEAN)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // ShortArgument

        manager.setExceptionHandler(ShortArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_SHORT)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(ShortArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_SHORT_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // IntegerArgument

        manager.setExceptionHandler(IntegerArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_INTEGER)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(IntegerArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_INTEGER_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // LongArgument

        manager.setExceptionHandler(LongArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_LONG)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(LongArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_LONG_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // FloatArgument

        manager.setExceptionHandler(FloatArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_FLOAT)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(FloatArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_FLOAT_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // DoubleArgument

        manager.setExceptionHandler(DoubleArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_DOUBLE)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(DoubleArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_DOUBLE_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // UUIDArgument

        manager.setExceptionHandler(UUIDArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_UUID)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // PlayerArgument

        manager.setExceptionHandler(PlayerArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_PLAYER)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // OfflinePlayerArgument

        manager.setExceptionHandler(OfflinePlayerArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // WorldArgument

        manager.setExceptionHandler(WorldArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_WORLD)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // EnchantmentArgument

        manager.setExceptionHandler(EnchantmentArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_ENCHANTMENT)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // MaterialArgument

        manager.setExceptionHandler(MaterialArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_MATERIAL)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // EntityTypeArgument

        manager.setExceptionHandler(EntityTypeArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_ENTITY_TYPE)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // NamespacedKeyArgument

        manager.setExceptionHandler(NamespacedKeyArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_NAMESPACEDKEY)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        // PositionArgument

        manager.setExceptionHandler(PositionArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_POSITION)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

    }

}
