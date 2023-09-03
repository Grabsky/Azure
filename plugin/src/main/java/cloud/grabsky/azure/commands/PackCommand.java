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
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.stream.Stream;

@Command(name = "pack", permission = "azure.command.pack", usage = "/pack (...)")
public final class PackCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? CompletionsProvider.of(Stream.of("apply", "notify").filter(it -> context.getExecutor().asCommandSender().hasPermission(this.getPermission() + "." + it) == true).toList())
                : (context.getInput().at(1).equalsIgnoreCase("notify") == true)
                        ? CompletionsProvider.of("--confirm")
                        : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        switch (arguments.next(String.class).asOptional("help").toLowerCase()) {
            case "apply" -> {
                context.getExecutor().asPlayer().setResourcePack(
                        PluginConfig.RESOURCE_PACK_URL,
                        PluginConfig.RESOURCE_PACK_HASH,
                        PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                        PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
                );
            }
            case "notify" -> {
                final CommandSender sender = context.getExecutor().asCommandSender();
                // ...
                if (sender.hasPermission(this.getPermission() + ".notify") == true) {
                    final boolean isConfirm = arguments.next(String.class).asOptional("").equalsIgnoreCase("--confirm");
                    // Checking for --confirm flag.
                    if (isConfirm == true) {
                        // Getting the server audience.
                        final Server server = plugin.getServer();
                        // Playing notification sound.
                        if (PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND != null)
                            server.playSound(PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND);
                        // Sending notification message.
                        Message.of(PluginLocale.COMMAND_PACK_NOTIFICATION).send(server);
                        return;
                    }
                    Message.of(PluginLocale.COMMAND_PACK_NOTIFY_CONFIRM).replace("<input>", context.getInput().toString()).send(sender);
                    return;
                }
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            }
            // Showing help page when invalid argument is provided.
            default -> Message.of(PluginLocale.COMMAND_PACK_HELP).send(context.getExecutor().asCommandSender());
        }
    }

}
