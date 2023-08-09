package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.argument.FloatArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command(name = "speed", permission = "azure.command.speed", usage = "/speed (player) (speed)")
public final class SpeedCommand extends RootCommand {

    private static final ExceptionHandler.Factory SPEED_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_SPEED_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        final Player target = arguments.next(Player.class).asRequired(SPEED_USAGE);
        final Float speed = arguments.next(Float.class, FloatArgument.ofRange(0.0F, 1.0F)).asOptional();
        // Handling FLY SPEED.
        if (target.isFlying() == true) {
            final float finalSpeed = (speed != null) ? speed : 0.1F; // default fly speed is 0.1F
            target.setFlySpeed(finalSpeed);
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_SPEED_SET_SUCCESS_FLY)
                    .placeholder("player", target)
                    .placeholder("speed", finalSpeed)
                    .send(sender);
            return;
        }
        // Handling WALK SPEED.
        final float finalSpeed = (speed != null) ? speed : 0.2F; // default walk speed is 0.2F
        target.setWalkSpeed(finalSpeed);
        // Sending message to command sender.
        Message.of(PluginLocale.COMMAND_SPEED_SET_SUCCESS_WALK)
                .placeholder("player", target)
                .placeholder("speed", finalSpeed)
                .send(sender);
    }

}
