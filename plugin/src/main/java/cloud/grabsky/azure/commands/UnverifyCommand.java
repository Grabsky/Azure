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
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "unverify", permission = "azure.command.unverify", usage = "/unverify")
public final class UnverifyCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;;

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ENABLED == false) {
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
        // Removing permission from the player, if configured.
        if (PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION.isEmpty() == false) {
            // Loading LuckPerms' User and removing permission node from them.
            plugin.getLuckPerms().getUserManager().modifyUser(sender.getUniqueId(), (it) -> {
                it.data().remove(PermissionNode.builder(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION).build());
            });
        }
        // Getting verification role.
        final @Nullable Role role = plugin.getDiscordIntegration().getGuild().getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID);
        // Getting Member object associated with this user, if the role still exists.
        if (role != null) {
            plugin.getDiscordIntegration().getGuild().retrieveMemberById(discordId).queue(member -> {
                plugin.getDiscordIntegration().getGuild().removeRoleFromMember(member, role).queue();
            });
        }
        // Sending prompt message to the player.
        Message.of(PluginLocale.COMMAND_UNVERIFY_SUCCESS).send(sender);
    }

}
