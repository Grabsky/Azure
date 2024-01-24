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
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TO-DO: Support for "@forever" selector which defaults to 0s, making the punishment permanent. (low priority)
@Command(name = "ban", permission = "azure.command.ban", usage = "/ban (player) (duration) (reason)")
public final class BanCommand extends RootCommand {

    private @Dependency Azure plugin;
    private @Dependency LuckPerms luckperms;


    private static final ExceptionHandler.Factory BAN_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_BAN_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(OfflinePlayer.class);
            case 1 -> IntervalArgument.ofRange(0L, 12L, Unit.YEARS);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting OfflinePlayer argument, this can be either a player name or their unique id.
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(BAN_USAGE);
        // Getting UUID.
        final UUID targetUniqueId = target.getUniqueId();
        // Getting User object from UUID - can be null.
        final @Nullable User targetUser = plugin.getUserCache().getUser(targetUniqueId);
        // Leaving the command block in case that User object for provided player does not exist.
        if (targetUser == null) {
            Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER).placeholder("input", targetUniqueId).send(sender);
            return;
        }
        // Getting duration.
        final Interval duration = arguments.next(Interval.class, IntervalArgument.ofRange(0L, 12L, Unit.YEARS)).asRequired(BAN_USAGE);
        // (optional) Getting the punishment reason.
        final @Nullable String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // ...
        if (sender instanceof Player senderOnline) {
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
                plugin.getBedrockScheduler().run(1L, (task) -> ban(sender, target, targetUser, reason, duration));
            });
            return;
        }
        // Otherwise, just banning.
        ban(sender, target, targetUser, reason, duration);
    }

    private static void ban(final @NotNull CommandSender sender, final @NotNull OfflinePlayer target, final @NotNull User targetUser, final @Nullable String reason, final @NotNull Interval duration) {
        // When duration is 0, punishment will be permanent - until manually removed.
        if (duration.as(Unit.MILLISECONDS) == 0) {
            // Banning the player. Player will be kicked manually for the sake of custimazble message.
            targetUser.ban(null, reason, sender);
            // Kicking with custom message.
            if (target.isOnline() && target instanceof Player onlineTarget) {
                onlineTarget.kick(
                        Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE_PERMANENT)
                                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                                .parse()
                );
            }
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_BAN_SUCCESS_PERMANENT)
                    .placeholder("player", targetUser)
                    .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                    // Sending to all players with specific permission.
                    .broadcast(receiver -> receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
            // Exiting the command block.
            return;
        }
        // Banning the player temporarily. Player will be kicked manually for the sake of custimazble message.
        targetUser.ban(duration, reason, sender);
        // Kicking with custom message.
        if (target.isOnline() && target instanceof Player onlineTarget) {
            // Kicking with custom message.
            onlineTarget.kick(
                    Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE)
                            .placeholder("duration_left", duration.toString())
                            .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                            .parse()
            );
        }
        // Sending success message to the sender.
        Message.of(PluginLocale.COMMAND_BAN_SUCCESS)
                .placeholder("player", targetUser)
                .placeholder("duration_left", duration.toString())
                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                // Sending to all players with specific permission.
                .broadcast(receiver -> receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
    }

}
