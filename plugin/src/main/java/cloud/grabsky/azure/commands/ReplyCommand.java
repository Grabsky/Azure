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

import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

// TO-DO: Disallow spying on players with higher group weight.
@Command(name = "reply", aliases = "r", permission = "azure.command.reply", usage = "/reply (message)")
public final class ReplyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability ChatManager chat;

    @Dependency
    private @UnknownNullability UserCache userCache;


    private static final ExceptionHandler.Factory REPLY_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_REPLY_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting UUID of command executor.
        final UUID senderUniqueId = sender.getUniqueId();
        // Getting UUID of last recipient. May be null.
        final @Nullable UUID lastRecipient = chat.getLastRecipient(senderUniqueId);
        // Getting last recipient (as Player object) from stored (or not) UUID.
        final @Nullable Player target = (lastRecipient != null)
                ? (Bukkit.getPlayer(lastRecipient) != null) ? Bukkit.getPlayer(lastRecipient) : null
                : null;
        // Sending error message when player has nobody to reply to.
        if (target == null) {
            Message.of(PluginLocale.COMMAND_REPLY_FAILURE).send(sender);
            return;
        }
        // Don't expose hidden players. This will be (soon) moved to the PlayerArgument
        if (sender.canSee(target) == false) {
            Message.of(PluginLocale.Commands.INVALID_PLAYER).placeholder("input", target.getName()).send(sender);
            return;
        }
        // Getting the rest of user input as a message.
        final String message = arguments.next(String.class, StringArgument.GREEDY).asRequired(REPLY_USAGE);
        // Sending messages...
        Message.of(PluginLocale.COMMAND_REPLY_SUCCESS_TO).placeholder("target", target).placeholder("message", message).send(sender);
        Message.of(PluginLocale.COMMAND_REPLY_SUCCESS_FROM).placeholder("sender", sender).placeholder("message", message).send(target);
        // Forwarding message to spies...
        Message.of(PluginLocale.COMMAND_SPY_MESSAGE_FORMAT)
                .placeholder("sender", sender)
                .placeholder("target", target)
                .placeholder("message", message)
                .broadcast(player -> {
                    // Preventing duplicated messages.
                    if (player.equals(sender) == true || player.equals(target) == true)
                        return false;
                    // Returning false for players that are currently not spying.
                    return userCache.getUser(player).isSpying() == true;
                });
        // Sending message to the console...
        Message.of(PluginLocale.COMMAND_SPY_MESSAGE_FORMAT_CONSOLE).placeholder("sender", sender).placeholder("target", target).placeholder("message", message).send(Bukkit.getConsoleSender());
        // Updating recipients...
        chat.setLastRecipients(sender.getUniqueId(), target.getUniqueId());
    }

}
