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
import cloud.grabsky.azure.user.AzureUser;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@Command(name = "spy", permission = "azure.command.spy", usage = "/spy (target) (state)")
public final class SpyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability ChatManager chat;

    @Dependency
    private @UnknownNullability UserCache userCache;

    private static final ExceptionHandler.Factory SPY_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_SPY_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> CompletionsProvider.of(Boolean.class);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Handling command without any arguments specified, which is equal to "toggle-spy-for-myself".
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            // Getting User of the sender.
            final AzureUser user = (AzureUser) userCache.getUser(sender);
            // Computing next spy state.
            final boolean nextSpyState = !user.isSpying();
            // Changing spy state.
            user.setSpying(nextSpyState);
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_SPY_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextSpyState == true)).send(sender);
            return;
        }
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting arguments.
        final Player target = arguments.next(Player.class).asRequired(SPY_USAGE);
        final Boolean state = arguments.next(Boolean.class).asOptional();
        // Exiting command block in case specified target is different from sender, and sender does not have permissions to "use" other players.
        if (sender != target && context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // Getting User object of the target.
        final AzureUser targetUser = (AzureUser) userCache.getUser(target);
        // Computing next spy state.
        final boolean nextSpyState = (state != null) ? state : !targetUser.isSpying();
        // Changing spy state.
        targetUser.setSpying(nextSpyState);
        // Sending success message to the sender. (if applicable)
        if (sender != target)
            Message.of(PluginLocale.COMMAND_SPY_SUCCESS)
                    .placeholder("player", target)
                    .placeholder("state", getColoredBooleanLong(nextSpyState == true))
                    .send(sender);
        // Sending success message to the target.
        Message.of(PluginLocale.COMMAND_SPY_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextSpyState == true)).send(target);
    }

    private Component getColoredBooleanLong(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanLong(true).color(GREEN) : PluginLocale.getBooleanLong(false).color(RED);
    }

}
