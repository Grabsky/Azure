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

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command(name = "azure", permission = "azure.command.azure", usage = "/azure (...)")
public final class AzureCommand extends RootCommand {

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return (index == 0)
                ? (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".reload") == true)
                        ? CompletionsProvider.of("reload")
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        if (queue.hasNext() == false) {
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_AZURE_HELP).send(sender);
            return;
        } else if (queue.next(String.class).asRequired().equalsIgnoreCase("reload") == true && sender.hasPermission(this.getPermission() + ".reload") == true) {
            if (Azure.getInstance().onReload() == true) {
                // Sending success message to command sender.
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}
