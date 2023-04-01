package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;

public final class SpeedCommand extends RootCommand {

    public SpeedCommand() {
        super("speed", null, "azure.command.speed", "/speed <player> [speed]", "Modify in-game movement speed.");
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? CompletionsProvider.of(Player.class)
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        final Player target = arguments.next(Player.class).asRequired();
        final Float speed = arguments.next(Float.class).asOptional();
        // Handling FLY SPEED.
        if (target.isFlying() == true) {
            final float finalSpeed = (speed != null) ? speed : 0.1F; // default fly speed is 0.1F
            if (inRange(finalSpeed, 0.0F, 1.0F) == true) {
                target.setFlySpeed(finalSpeed);
                // Sending message to command sender.
                Message.of(PluginLocale.SPEED_SET_SUCCESS_FLY)
                        .placeholder("player", target)
                        .placeholder("speed", finalSpeed)
                        .send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.SPEED_SET_FAILURE_VALUE_NOT_IN_RANGE)
                    .placeholder("min", 0.0F)
                    .placeholder("max", 1.0F)
                    .send(sender);
            return;
        }
        // Handling WALK SPEED.
        final float finalSpeed = (speed != null) ? speed : 0.2F; // default walk speed is 0.2F
        if (inRange(finalSpeed, 0.0F, 1.0F) == true) {
            target.setWalkSpeed(finalSpeed);
            // Sending message to command sender.
            Message.of(PluginLocale.SPEED_SET_SUCCESS_WALK)
                    .placeholder("player", target)
                    .placeholder("speed", finalSpeed)
                    .send(sender);
            return;
        }
        Message.of(PluginLocale.SPEED_SET_FAILURE_VALUE_NOT_IN_RANGE)
                .placeholder("min", 0.0F)
                .placeholder("max", 1.0F)
                .send(sender);
    }

}
