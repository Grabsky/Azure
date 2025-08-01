/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.user.AzurePunishment;
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
            case 1 -> CompletionsProvider.of(IntervalArgument.ofRange(0L, 12L, Unit.YEARS).provide(context, "permanent"));
            default -> context.getInput().getInput().trim().endsWith("--silent") == false ? CompletionsProvider.of("--silent") : CompletionsProvider.EMPTY;
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
        final Interval duration = arguments.next(Interval.class, IntervalArgument.DEFAULT_RANGE).asRequired(BAN_USAGE);
        // (optional) Getting the punishment reason.
        final String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // Checking whether punishment should be silent.
        final boolean isSilent = reason.contains("--silent");
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
                plugin.getBedrockScheduler().run(1L, (_) -> ban(sender, target, targetUser, reason.split("--silent")[0].trim().trim(), duration, isSilent));
            });
            return;
        }
        // Otherwise, just banning.
        ban(sender, target, targetUser, reason.split("--silent")[0].trim().trim(), duration, isSilent);
    }

    private void ban(final @NotNull CommandSender sender, final @NotNull OfflinePlayer target, final @NotNull User targetUser, final @Nullable String reason, final @NotNull Interval duration, final boolean isSilent) {
        final String finalReason = (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON;

        // Permanently banning the player when duration is 0.
        if (duration.as(Unit.MILLISECONDS) == Long.MAX_VALUE) {
            final AzurePunishment punishment = (AzurePunishment) targetUser.ban(null, reason, sender);
            // Kicking with custom message.
            if (target.isOnline() && target instanceof Player onlineTarget) {
                onlineTarget.kick(
                        Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE_PERMANENT).placeholder("reason", finalReason).parse()
                );
            }
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_BAN_SUCCESS_PERMANENT)
                    .placeholder("player", targetUser.getName())
                    .placeholder("reason", finalReason)
                    // Sending to all players with specific permission.
                    .broadcast(receiver ->  isSilent == false || receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
            // Forwarding to Discord...
            if (isSilent == false && PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
                // Constructing the message.
                final String message = PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_PERMANENT_BAN_FORMAT
                        .replace("<name>", targetUser.getName())
                        .replace("<issuer>", punishment.getIssuer())
                        .replace("<reason>", finalReason);
                // Forwarding the message through configured webhook.
                if (message.isEmpty() == false)
                    plugin.getDiscordIntegration().getWebhookClients().get("PUNISHMENTS").send(message);
            }

        // Otherwise, banning player temporarily.
        } else {
            final AzurePunishment punishment = (AzurePunishment) targetUser.ban(duration, reason, sender);
            // Kicking with custom message.
            if (target.isOnline() && target instanceof Player onlineTarget) {
                // Kicking with custom message.
                onlineTarget.kick(
                        Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE).placeholder("duration_left", duration.toString()).placeholder("reason", finalReason).parse()
                );
            }
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_BAN_SUCCESS)
                    .placeholder("player", targetUser.getName())
                    .placeholder("duration_left", duration.toString())
                    .placeholder("reason", finalReason)
                    // Sending to all players with specific permission.
                    .broadcast(receiver -> isSilent == false || receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
            // Forwarding to Discord...
            if (isSilent == false && PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
                // Constructing the message.
                final String message = PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_BAN_FORMAT
                        .replace("<name>", targetUser.getName())
                        .replace("<issuer>", punishment.getIssuer())
                        .replace("<duration>", duration.toString())
                        .replace("<reason>", finalReason);
                // Forwarding the message through configured webhook.
                if (message.isEmpty() == false)
                    plugin.getDiscordIntegration().getWebhookClients().get("PUNISHMENTS").send(message);
            }
        }
    }

}
