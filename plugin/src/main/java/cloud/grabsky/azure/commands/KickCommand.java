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
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
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
        return (index == 0) ? CompletionsProvider.of(Player.class) : context.getInput().getInput().trim().endsWith("--silent") == false ? CompletionsProvider.of("--silent") : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting Player argument.
        final Player target = arguments.next(Player.class).asRequired(KICK_USAGE);
        // (optional) Getting the punishment reason.
        final String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // Checking whether punishment should be silent.
        final boolean isSilent = reason.contains("--silent");
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
                plugin.getBedrockScheduler().run(1L, (_) -> kick(sender, target, reason.split("--silent")[0].trim(), isSilent));
            });
            return;
        }
        // Otherwise, just kicking.
        kick(sender, target, reason.split("--silent")[0].trim(), isSilent);
    }

    private void kick(final @NotNull CommandSender sender, final @NotNull Player target, final @Nullable String reason, final boolean isSilent) {
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
                .broadcast(receiver -> isSilent == false || receiver instanceof ConsoleCommandSender || receiver.hasPermission("azure.command.ban") == true);
        // Logging...
        plugin.getPunishmentsFileLogger().log("Player " + target.getName() + " (" + target.getUniqueId() + ") has been KICKED by " + sender.getName() + " with a reason: " + reason);
        // Forwarding to Discord...
        if (isSilent == false && PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing the message.
            final String message = PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_KICK_FORMAT
                    .replace("<name>", this.getName())
                    .replace("<issuer>", sender instanceof Player ? sender.getName() : "Console")
                    .replace("<reason>", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
            // Forwarding the message through configured webhook.
            if (message.isEmpty() == false)
                plugin.getDiscordIntegration().getWebhookForwardingPunishments().send(message);
        }

    }

}
