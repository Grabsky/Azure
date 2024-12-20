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
package cloud.grabsky.azure.discord;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.bedrock.components.Message;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.MessageComponentInteraction;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VerificationManager {

    // Holds information of whether this class has been already initialized or not.
    private static boolean isInitialized = false;

    // Stores all currently active codes.
    private final Cache<UUID, String> codes = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final java.awt.Color SUCCESS_COLOR = new java.awt.Color(87, 242, 135);
    private static final java.awt.Color FAILURE_COLOR = new java.awt.Color(237, 66, 69);

    public VerificationManager(final @NotNull Azure plugin, final @NotNull DiscordApi discord) {
        // Throwing IllegalStateException if already initialized.
        if (isInitialized == true)
            throw new IllegalStateException("VerificationManager is already initialized. Was the plugin reloaded?");
        // Registering InteractionCreate listener.
        discord.addInteractionCreateListener(event -> {
            final @Nullable MessageComponentInteraction interaction = event.getMessageComponentInteraction().orElse(null);
            // Getting server this interaction took place on.
            final Server server = event.getInteraction().getServer().orElseThrow();
            // ...
            if (interaction != null && interaction.getCustomId().equals("verification_button") == true) {
                // Sending error message if user already has a role.
                if (interaction.getUser().getRoles(server).stream().anyMatch(it -> it.getIdAsString().equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID) == true) == true) {
                    interaction.createImmediateResponder()
                            .addEmbed(new EmbedBuilder().setColor(FAILURE_COLOR).setDescription(PluginLocale.DISCORD_VERIFICATION_FAILURE_VERIFIED))
                            .setFlags(MessageFlag.EPHEMERAL)
                            .respond();
                    return;
                }
                interaction.respondWithModal("verification_modal",
                        PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_MODAL_LABEL,
                        ActionRow.of(
                                TextInput.create(TextInputStyle.SHORT, "verification_code", PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_MODAL_INPUT_LABEL, 7, 7, true)
                        )
                );
            }
        });
        // Registering ModalSubmit listener.
        discord.addModalSubmitListener(event -> {
            if (event.getModalInteraction().getCustomId().equals("verification_modal") == true) {
                final @Nullable ActionRow row = event.getModalInteraction().getComponents().getFirst().asActionRow().orElse(null);
                // Returning if there is no action row.
                if (row == null)
                    return;
                // Getting list of components in the action row.
                final List<LowLevelComponent> components = row.getComponents();
                // Iterating over the list of components...
                for (final var component : components) {
                    if (component.isTextInput() == true && component.asTextInput().get().getCustomId().equals("verification_code") == true) {
                        final String code = component.asTextInput().get().getValue();
                        // ...
                        final @Nullable UUID uniqueId = codes.asMap().entrySet().stream().filter(it -> it.getValue().equals(code) == true).map(Map.Entry::getKey).findFirst().orElse(null);
                        // Sending error message if UUID is null.
                        if (uniqueId == null) {
                            event.getModalInteraction().createImmediateResponder()
                                    .addEmbed(new EmbedBuilder().setColor(FAILURE_COLOR).setDescription(PluginLocale.DISCORD_VERIFICATION_FAILURE_INVALID_CODE))
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .respond();
                            // Returning...
                            return;
                        }
                        // Getting User object from the UUID.
                        final @Nullable User user = plugin.getUserCache().getUser(uniqueId);
                        // Throwing IllegalStateException if User object for this UUID returned 'null'.
                        if (user == null)
                            throw new IllegalStateException("Verification failed. Missing User object for: " + uniqueId);
                        // Updating Discord ID associated with this user.
                        user.setDiscordId(event.getModalInteraction().getUser().getIdAsString());
                        // Saving...
                        plugin.getUserCache().as(AzureUserCache.class).saveUser(user);
                        // Adding permission to the player, if configured.
                        if ("".equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION) == false)
                            // Loading LuckPerms' User and adding permission node to them.
                            plugin.getLuckPerms().getUserManager().modifyUser(uniqueId, (it) -> {
                                it.data().add(PermissionNode.builder(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION).build());
                            });
                        // Adding role if specified.
                        if (PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID.isEmpty() == false) {
                            // Getting configured server.
                            final Server server = event.getModalInteraction().getServer().orElseThrow();
                            // Getting verification role.
                            final @Nullable Role role = server.getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID).orElse(null);
                            // Getting Member object associated with this user, if the role still exists.
                            if (role != null)
                                server.addRoleToUser(event.getModalInteraction().getUser(), role);
                        }
                        // Sending success message to the user.
                        event.getModalInteraction().createImmediateResponder()
                                .addEmbed(new EmbedBuilder().setColor(SUCCESS_COLOR).setDescription(PluginLocale.DISCORD_VERIFICATION_SUCCESS))
                                .setFlags(MessageFlag.EPHEMERAL)
                                .respond();
                        // Sending success message to the player.
                        final Player player = Bukkit.getPlayer(uniqueId);
                        // ...
                        if (player != null && player.isOnline() == true)
                            Message.of(PluginLocale.COMMAND_VERIFY_SUCCESS).send(player);
                        // Invalidating...
                        codes.invalidate(uniqueId);
                        // Returning...
                        return;
                    }
                }
            }
        });
        // Marking as enabled.
        isInitialized = true;
    }

    /**
     * Returns verification code which is active for the next 5 minutes. Code expiration is handled by the {@link Cache} storage.
     */
    public @NotNull String requestCode(final @NotNull UUID uniqueId) {
        final @Nullable String existingToken = codes.getIfPresent(uniqueId);
        // Returning existing token if exists.
        if (existingToken != null)
            return existingToken;
        // Generate a new token otherwise...
        final Iterator<String> iterator = codes.asMap().values().iterator();
        // Token. Generated in the next step.
        String token = "";
        // Generating...
        while (token.isEmpty() == true || codes.asMap().containsValue(token) == true) {
            token = new SecureRandom().nextInt(100, 1000) + "-" + new SecureRandom().nextInt(100, 1000);
        }
        // Adding to cache.
        codes.put(uniqueId, token);
        // Returning...
        return token;
    }

}
