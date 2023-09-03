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
