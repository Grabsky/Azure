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
import cloud.grabsky.azure.api.user.User;
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
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "verify", permission = "azure.command.verify", usage = "/verify")
public final class VerifyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;;

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? (context.getExecutor().hasPermission("azure.command.verify.send_button") == true)
                        ? CompletionsProvider.of("send_component")
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ENABLED == false || plugin.getDiscord() == null) {
            Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_NOT_ENABLED).send(context.getExecutor().asCommandSender());
            return;
        }
        // Handling 'send_component' sub-command, which is responsible for sending button component to desired channel.
        if (arguments.peek().hasNext() == true && arguments.nextString().equalsIgnoreCase("send_component") == true) {
            // Checking required permissions.
            if (context.getExecutor().hasPermission("azure.command.verify.send_component") == true) {
                // Getting the text channel from provided ID.
                final @Nullable TextChannel channel = plugin.getDiscord().getTextChannelById(arguments.nextString()).orElse(null);
                // Sending error message in case channel does not not exist or is inaccessible.
                if (channel == null) {
                    Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_SEND_COMPONENT_FAILURE_INVALID_CHANNEL).send(context.getExecutor().asCommandSender());
                    return;
                }
                // Creating a message with button component.
                final MessageBuilder builder = new MessageBuilder().addComponents(ActionRow.of(
                        Button.create("verification_button", PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_BUTTON_STYLE, PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_BUTTON_LABEL)
                ));
                // Sending...
                builder.send(channel);
            }
            return;
        }
        // Getting command executor as instance of Player.
        final Player sender = context.getExecutor().asPlayer();
        // Getting User instance of this player.
        final User user = plugin.getUserCache().getUser(sender);
        // Sending error message if player is already verified.
        if (user.getDiscordId() != null) {
            Message.of(PluginLocale.COMMAND_VERIFY_FAILURE_ALREADY_VERIFIED).send(sender);
            return;
        }
        // Requesting a code and starting verification process.
        final String token = plugin.getVerificationManager().requestCode(sender.getUniqueId());
        // Sending prompt message to the player.
        Message.of(PluginLocale.COMMAND_VERIFY_PROMPT).replace("<code>", token).send(sender);
    }

}
