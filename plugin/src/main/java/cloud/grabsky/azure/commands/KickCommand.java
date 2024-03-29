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
import cloud.grabsky.azure.user.AzureUser.DiscordLogger;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "kick", permission = "azure.command.kick", usage = "/kick (player) (reason)")
public final class KickCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;

    @Dependency
    private @UnknownNullability LuckPerms luckperms;


    private static final ExceptionHandler.Factory KICK_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_KICK_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting Player argument.
        final Player target = arguments.next(Player.class).asRequired(KICK_USAGE);
        // (optional) Getting the punishment reason.
        final @Nullable String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // ...
        if (sender instanceof Player senderOnline) {
            final UUID targetUniqueId = target.getUniqueId();
            // Getting group of the sender.
            final @Nullable Group senderGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(senderOnline).getPrimaryGroup());
            // Loading the target User.
            luckperms.getUserManager().loadUser(targetUniqueId).thenAccept(user -> {
                final @Nullable Group targetGroup = luckperms.getGroupManager().getGroup(user.getPrimaryGroup());
                // Comparing group weights.
                if (senderGroup == null || targetGroup == null || senderGroup.getWeight().orElse(0) <= targetGroup.getWeight().orElse(0)) {
                    Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                    return;
                }
                // Continuing... scheduling the rest of the logic onto the main thread.
                plugin.getBedrockScheduler().run(1L, (task) -> kick(sender, target, reason));
            });
            return;
        }
        // Otherwise, just kicking.
        kick(sender, target, reason);
    }

    private void kick(final @NotNull CommandSender sender, final @NotNull Player target, final @Nullable String reason) {
        target.kick(
                Message.of(PluginLocale.COMMAND_KICK_DISCONNECT_MESSAGE)
                        .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                        .parse()
        );
        // Sending success message to all players with specific permission.
        Message.of(PluginLocale.COMMAND_KICK_SUCCESS)
                .placeholder("player", target)
                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                // Sending to all players with specific permission.
                .broadcast(receiver -> receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
        // Logging...
        plugin.getPunishmentsFileLogger().log("Player " + target.getName() + " (" + target.getUniqueId() + ") has been KICKED by " + sender.getName() + " with a reason: " + reason);
        // Forwarding to Discord...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing type-specific embed.
            final EmbedBuilder embed = DiscordLogger.constructKick(plugin.getUserCache().getUser(target), sender, (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
            // Forwarding the message through configured webhook.
            new WebhookMessageBuilder().addEmbed(embed).sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL);
        }

    }

}
