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
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@Command(name = "teleport", permission = "azure.command.teleport", usage = "/teleport (target) (destination) (--silent)")
public final class TeleportCommand extends RootCommand {

    private static final ExceptionHandler.Factory TELEPORT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_TELEPORT_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        final String input = context.getInput().at(index, "");
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> (input.equalsIgnoreCase("@self") == true || Bukkit.getPlayerExact(input) != null)
                        ? CompletionsProvider.of(Player.class, "@x @y @z")
                        : CompletionsProvider.EMPTY;
            case 2 -> (input.equalsIgnoreCase("@self") == true || Bukkit.getPlayerExact(input) != null)
                        ? CompletionsProvider.of("--silent")
                        : CompletionsProvider.of("@y @z");
            case 3 -> CompletionsProvider.of("@z");
            case 4 -> CompletionsProvider.of("--silent");
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Trying to resolve the command using Player as a destination...
        if (context.getInput().maxIndex() == 2 || context.getInput().at(3, "").equalsIgnoreCase("--silent") == true)
            this.onTeleportToPlayer(context, arguments);
        // ...or using Position otherwise...
        else this.onTeleportToPosition(context, arguments);
    }

    public void onTeleportToPlayer(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final Player target = arguments.next(Player.class).asRequired(TELEPORT_USAGE);
        final Player destination = arguments.next(Player.class).asRequired(TELEPORT_USAGE);
        final boolean isSilent = arguments.next(String.class).asOptional("--not-silent").equalsIgnoreCase("--silent")
                || target.canSee(destination) == false
                || destination.canSee(target) == false;
        // ...
        if (target == destination) {
            Message.of(PluginLocale.TELEPORT_PLAYER_FAILURE_TARGETS_ARE_THE_SAME).send(sender);
            return;
        }
        // ...
        if (sender != target && sender.hasPermission(this.getPermission() + ".player.others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // ...
        target.teleport(destination, TeleportCause.COMMAND);
        // ...
        if (sender != target && sender != destination)
            Message.of(PluginLocale.COMMAND_TELEPORT_PLAYER_SUCCESS_SENDER)
                    .placeholder("target", target)
                    .placeholder("destination", destination)
                    .send(sender);
        // ...
        if (isSilent == false) {
            Message.of(PluginLocale.COMMAND_TELEPORT_PLAYER_SUCCESS_TARGET).placeholder("destination", destination).send(target);
            Message.of(PluginLocale.COMMAND_TELEPORT_PLAYER_SUCCESS_DESTINATION).placeholder("target", target).send(destination);
        }
    }

    public void onTeleportToPosition(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final Player target = arguments.next(Player.class).asRequired(TELEPORT_USAGE);
        final Position destination = arguments.next(Position.class).asRequired(TELEPORT_USAGE);
        final boolean isSilent = arguments.next(String.class).asOptional("--not-silent").equalsIgnoreCase("--silent");
        // ...
        final Location location = new Location(target.getWorld(), destination.x(), destination.y(), destination.z(), target.getLocation().getYaw(), target.getLocation().getPitch());
        // ...
        if (sender != target && sender.hasPermission(this.getPermission() + ".position.others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // ...
        target.teleportAsync(location, TeleportCause.COMMAND);
        // ...
        if (sender != target)
            Message.of(PluginLocale.COMMAND_TELEPORT_POSITION_SUCCESS_SENDER)
                    .placeholder("target", target)
                    .placeholder("x", format("%.2f", destination.x()))
                    .placeholder("y", format("%.2f", destination.y()))
                    .placeholder("z", format("%.2f", destination.z()))
                    .send(sender);
        // ...
        if (isSilent == false) {
            Message.of(PluginLocale.COMMAND_TELEPORT_POSITION_SUCCESS_TARGET)
                    .placeholder("x", format("%.2f", destination.x()))
                    .placeholder("y", format("%.2f", destination.y()))
                    .placeholder("z", format("%.2f", destination.z()))
                    .send(target);
        }
    }

}
