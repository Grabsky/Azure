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
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.entity.Player;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "unverify", permission = "azure.command.unverify", usage = "/unverify")
public final class UnverifyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;;

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ENABLED == false || plugin.getDiscord() == null) {
            Message.of(PluginLocale.COMMAND_UNVERIFY_FAILURE_NOT_ENABLED).send(context.getExecutor().asCommandSender());
            return;
        }
        // Getting command executor as player.
        final Player sender = context.getExecutor().asPlayer();
        // Getting User instance of this player.
        final User user = plugin.getUserCache().getUser(sender);
        // Getting Discord ID associated with this user.
        final @Nullable String discordId = user.getDiscordId();
        // Sending error message if player is already verified.
        if (discordId == null) {
            Message.of(PluginLocale.COMMAND_UNVERIFY_FAILURE_NOT_VERIFIED).send(sender);
            return;
        }
        // Removing Discord ID from the User object.
        user.setDiscordId(null);
        // Saving...
        plugin.getUserCache().as(AzureUserCache.class).saveUser(user);
        // Getting configured server.
        final @Nullable Server server = plugin.getDiscord().getServerById(PluginConfig.DISCORD_INTEGRATIONS_DISCORD_SERVER_ID).orElse(null);
        // Sending error if configured server is inaccessible.
        if (server == null)
            return;
        // Getting verification role.
        final @Nullable Role role = server.getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID).orElse(null);
        // Getting Member object associated with this user, if the role still exists.
        if (role != null)
            plugin.getDiscord().getUserById(discordId).thenAccept(it -> server.removeRoleFromUser(it, role));
        // Sending prompt message to the player.
        Message.of(PluginLocale.COMMAND_UNVERIFY_SUCCESS).send(sender);
    }

}
