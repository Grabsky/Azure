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
package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.event.ResourcePackLoadEvent;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.resourcepack.ResourcePackManager;
import cloud.grabsky.azure.user.AzureUser;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.bedrock.components.ComponentBuilder;
import cloud.grabsky.bedrock.components.Message;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.javacord.api.entity.message.WebhookMessageBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerListener implements Listener {

    private final @NotNull Azure plugin;

    // Players with this permission are excluded from command filtering logic.
    private static final String PERMISSION_BYPASS_COMMAND_FILTER = "azure.plugin.bypass_command_filter";


    @EventHandler // We need to somehow ensure this is called AFTER AzureUserCache#onPlayerJoin(...) listener. I guess registration order is enough?
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Teleporting new players to spawn. (if enabled)
        if (player.hasPlayedBefore() == false && PluginConfig.GENERAL_TELEPORT_NEW_PLAYERS_TO_PRIMARY_WORLD_SPAWN == true) {
            // Getting the primary world.
            final World primaryWorld = plugin.getWorldManager().getPrimaryWorld();
            // Setting the respawn location.
            plugin.getBedrockScheduler().run(1L, (_) -> player.teleportAsync(plugin.getWorldManager().getSpawnPoint(primaryWorld)));
        }
        // Clearing title. (if enabled)
        if (PluginConfig.GENERAL_CLEAR_TITLE_ON_JOIN == true)
            player.clearTitle();
        // Setting join message to null because it needs to be handled manually.
        event.joinMessage(null);
        // Sending join message to audience that can see the player associated with the event.
        if (PluginConfig.CHAT_SERVER_JOIN_MESSAGE.isBlank() == false) {
            // Continuing only if resource-packs are not being sent on join. This means message should be postponed and handled within ResourcePackLoadEvent listener.
            if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == false) {
                // Getting LuckPerms' cached meta-data. This should never be null despite the warning.
                final CachedMetaData metaData = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData();
                // Sending join message to the audience.
                Message.of(PluginConfig.CHAT_SERVER_JOIN_MESSAGE)
                        .placeholder("player", player)
                        .placeholder("group", requirePresent(metaData.getPrimaryGroup(), ""))
                        .replace("<prefix>", requirePresent(metaData.getPrefix(), ""))
                        .replace("<suffix>", requirePresent(metaData.getSuffix(), ""))
                        .placeholder("displayname", player.displayName())
                        .broadcast(audience -> audience.canSee(player) == true);
            }
        }
        // Setting total number of resource-packs loaded by this player to 0. This is mainly for ease of use with other plugins.
        event.getPlayer().setMetadata("total_loaded_resource-packs", new FixedMetadataValue(plugin, 0));
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            // Sending resource-packs to the player. (next tick)
            plugin.getBedrockScheduler().run(1L, (_) -> plugin.getResourcePackManager().sendResourcePacks(player));
        }
        // Dispatching configured commands.
        PluginConfig.COMMAND_TRIGGERS_ON_JOIN.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(event.getPlayer(), command)));
    }

    @EventHandler // We need to somehow ensure this is called AFTER AzureUserCache#onPlayerJoin(...) listener. I guess registration order is enough?
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        // Setting quit message to null because it needs to be handled manually.
        event.quitMessage(null);
        // Sending quit message to audience that can see the player associated with the event.
        if (PluginConfig.CHAT_SERVER_QUIT_MESSAGE.isBlank() == false) {
            // Checking if player has successfully loaded all resource-packs before sending a quit message.
            if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == false || ResourcePackManager.isLoadingPacks(event.getPlayer()) == false) {
                // Getting LuckPerms' cached meta-data. This should never be null despite the warning.
                final CachedMetaData metaData = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData();
                // Sending quit message to the audience.
                Message.of(PluginConfig.CHAT_SERVER_QUIT_MESSAGE)
                        .placeholder("player", player)
                        .placeholder("group", requirePresent(metaData.getPrimaryGroup(), ""))
                        .replace("<prefix>", requirePresent(metaData.getPrefix(), ""))
                        .replace("<suffix>", requirePresent(metaData.getSuffix(), ""))
                        .placeholder("displayname", player.displayName())
                        .broadcast(audience -> audience.canSee(player) == true);
            }
        }
        // Dispatching configured commands.
        PluginConfig.COMMAND_TRIGGERS_ON_QUIT.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(event.getPlayer(), command)));
    }

    /* COMMAND TRIGGERS - RESOURCE PACK */

    @EventHandler @SneakyThrows
    public void onResourcePackLoad(final @NotNull ResourcePackLoadEvent event) {
        if (event.isInitial() == true && PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            final Player player = event.getPlayer();
            final UUID uniqueId = event.getPlayer().getUniqueId();
            // Getting LuckPerms' cached meta-data. This should never be null despite the warning.
            final CachedMetaData metaData = plugin.getLuckPerms().getUserManager().getUser(uniqueId).getCachedData().getMetaData();
            // Sending join message to the audience.
            Message.of(PluginConfig.CHAT_SERVER_JOIN_MESSAGE)
                    .placeholder("player", player)
                    .placeholder("group", requirePresent(metaData.getPrimaryGroup(), ""))
                    .replace("<prefix>", requirePresent(metaData.getPrefix(), ""))
                    .replace("<suffix>", requirePresent(metaData.getSuffix(), ""))
                    .placeholder("displayname", player.displayName())
                    .broadcast(audience -> audience.canSee(player) == true);
            // Continuing only if resource-packs are not being sent on join. This means message should be postponed and handled within ResourcePackLoadEvent listener.
            if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
                // Forwarding message to webhook...
                if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
                    // Setting message placeholders.
                    final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_JOIN_MESSAGE_FORMAT);
                    // Creating new instance of WebhookMessageBuilder.
                    final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
                    // Setting username if specified.
                    if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                        builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME));
                    // Setting avatar if specified.
                    if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                        builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR)).toURL());
                    // Sending the message.
                    builder.sendSilently(plugin.getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL);
                }
            }
        }
        // Dispatching configured commands.
        (event.isInitial() == true ? PluginConfig.COMMAND_TRIGGERS_ON_RESOURCES_FIRST_LOAD : PluginConfig.COMMAND_TRIGGERS_ON_RESOURCES_LOAD)
                .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(event.getPlayer(), command)));
    }

    /* WORLD RESPAWN - Respawns players on spawn-point of the main world. */

    @EventHandler
    public void onPlayerRespawn(final @NotNull PlayerRespawnEvent event) {
        // Setting respawn location to spawn point of the primary world. (if enabled)
        if (PluginConfig.GENERAL_RESPAWN_ON_PRIMARY_WORLD_SPAWN == true && event.isBedSpawn() == false && event.isAnchorSpawn() == false) {
            // Getting the primary world.
            final World primaryWorld = plugin.getWorldManager().getPrimaryWorld();
            // Setting the respawn location.
            event.setRespawnLocation(plugin.getWorldManager().getSpawnPoint(primaryWorld));
        }
    }

    /* DISCORD INTEGRATIONS - FORWARDING JOIN MESSAGE TO DISCORD SERVER */

    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinForward(final @NotNull PlayerJoinEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Continuing only if resource-packs are not being sent on join. This means message should be postponed and handled within ResourcePackLoadEvent listener.
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == false) {
            // Forwarding message to webhook...
            if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
                // Setting message placeholders.
                final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_JOIN_MESSAGE_FORMAT);
                // Creating new instance of WebhookMessageBuilder.
                final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
                // Setting username if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                    builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME));
                // Setting avatar if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                    builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR)).toURL());
                // Sending the message.
                builder.sendSilently(plugin.getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL);
            }
        }
    }

    /* DISCORD INTEGRATIONS - FORWARDING QUIT MESSAGE TO DISCORD SERVER */

    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitForward(final @NotNull PlayerQuitEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Skipping in case player has not loaded resource-packs.
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && ResourcePackManager.isLoadingPacks(event.getPlayer()) == true)
            return;
        // Forwarding message to webhook...
        if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
            // Setting message placeholders.
            final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_QUIT_MESSAGE_FORMAT);
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR)).toURL());
            // Sending the message.
            builder.sendSilently(plugin.getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL);
        }
    }

    /* INVULNERABLE PLAYERS - Enables void damage and prevents hunger loss for invulnerable players. */

    @EventHandler(ignoreCancelled = true)
    public void onVoidDamage(final @NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && player.isInvulnerable() == true && event.getCause() != EntityDamageEvent.DamageCause.VOID)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHungerLoss(final @NotNull FoodLevelChangeEvent event) {
        if (event.getEntity().getFoodLevel() > event.getFoodLevel() && event.getEntity() instanceof Player player && player.isInvulnerable() == true)
            event.setCancelled(true);
    }

    /* COMMAND FILTERING - Defined here because it's so small it doesn't deserve it's own class. */

    @EventHandler
    public void onPlayerSendCommandMap(final @NotNull PlayerCommandSendEvent event) {
        // Exiting if disabled or player has bypass permission.
        if (PluginConfig.COMMAND_FILTER_ENABLED == false || event.getPlayer().hasPermission(PERMISSION_BYPASS_COMMAND_FILTER) == true)
            return;
        // Constructing a new HashSet with default entries.
        final HashSet<String> commands = new HashSet<>(PluginConfig.COMMAND_FILTER_DEFAULT);
        // Sending debug message.
        if (plugin.isDebugEnabled() == true)
            plugin.getLogger().info("[DEBUG] Updating command map of " + event.getPlayer().getName() + "...");
        // Collecting commands to filter.
        PluginConfig.COMMAND_FILTER_EXTRA.stream()
                .filter(holder -> event.getPlayer().hasPermission(holder.getPermission()) == true)
                .flatMap(holder -> holder.getCommands().stream())
                .forEach(commands::add);
        // Filtering...
        event.getCommands().removeIf(it -> commands.contains(it) == PluginConfig.COMMAND_FILTER_USE_AS_BLACKLIST);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandPreprocess(final @NotNull PlayerCommandPreprocessEvent event) {
        // Exiting if disabled or player has bypass permission.
        if (PluginConfig.COMMAND_FILTER_ENABLED == false || PluginConfig.COMMAND_FILTER_BLOCK_FILTERED_COMMANDS == false || event.getPlayer().hasPermission(PERMISSION_BYPASS_COMMAND_FILTER) == true)
            return;
        // Getting the player.
        final Player player = event.getPlayer();
        final String message = event.getMessage().substring(1);
        // Checking matches for default command filter.
        if (PluginConfig.COMMAND_FILTER_DEFAULT.stream().anyMatch(message::startsWith) == PluginConfig.COMMAND_FILTER_USE_AS_BLACKLIST) {
            // Cancelling the event.
            event.setCancelled(true);
            // Sending error message to the player.
            Message.of(PluginConfig.BLOCKED_COMMAND_ERROR_MESSAGE).send(player);
            // Returning...
            return;
        }
        // Checking matches for extra command filters.
        for (final PluginConfig.CommandsHolder holder : PluginConfig.COMMAND_FILTER_EXTRA) {
            final String permission = holder.getPermission();
            // ...
            if (player.hasPermission(permission) == PluginConfig.COMMAND_FILTER_USE_AS_BLACKLIST)
                return;
            // Checking matches.
            if (holder.getCommands().stream().anyMatch(message::startsWith) == true) {
                // Cancelling the event.
                event.setCancelled(true);
                // Sending error message to the player.
                Message.of(PluginConfig.BLOCKED_COMMAND_ERROR_MESSAGE).send(player);
                // Returning...
                return;
            }
        }
    }

    /* UNKNOWN COMMAND MESSAGE - Changes error message that is sent when player tries to execute invalid/unknown command. Configurable. */

    @EventHandler
    public void onUnknownCommand(final @NotNull UnknownCommandEvent event) {
        if (PluginConfig.USE_BLOCKED_COMMAND_ERROR_MESSAGE_FOR_UNKNOWN_COMMAND == true)
            event.message(PluginConfig.BLOCKED_COMMAND_ERROR_MESSAGE);
    }

    /* DEATH MESSAGES */

    @SneakyThrows
    @EventHandler @SuppressWarnings({"deprecation", "UnstableApiUsage"}) // Cancelling death message using 'PlayerDeathEvent#deathMessage(null)' was not working, hence why deprecated method is used.
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event) {
        // Hiding death messages if enabled.
        if (PluginConfig.CHAT_HIDE_DEATH_MESSAGES == true) {
            event.setDeathMessage(null);
            event.deathMessage(null);
            return;
        }
        // Using plugin death messages if enabled.
        if (PluginConfig.CHAT_USE_PLUGIN_DEATH_MESSAGES == true && plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
            if (event.deathMessage() instanceof TranslatableComponent translatable) {
                final String text = PluginLocale.DEATH_MESSAGES.getOrDefault(translatable.key(), PluginLocale.DEATH_MESSAGES_DEFAULT);
                // Getting LuckPerms' cached meta-data. This should never be null despite the warning.
                final CachedMetaData metaData = plugin.getLuckPerms().getUserManager().getUser(event.getPlayer().getUniqueId()).getCachedData().getMetaData();
                // Preparing the message.
                final Message.StringMessage message = Message.of(text)
                        .replace("<prefix>", requirePresent(metaData.getPrefix(), ""))
                        .replace("<suffix>", requirePresent(metaData.getSuffix(), ""))
                        .placeholder("victim", event.getPlayer())
                        .placeholder("victim_displayname", event.getPlayer().displayName())
                        .placeholder("attacker", (event.getDamageSource().getCausingEntity() != null) ? event.getDamageSource().getCausingEntity().getName() : "N/A")
                        .placeholder("attacker_displayname", (event.getDamageSource().getCausingEntity() != null && event.getDamageSource().getCausingEntity() instanceof Player attacker) ? attacker.displayName() : ComponentBuilder.EMPTY);
                // Broadcasting the message.
                message.broadcast();
                // Preventing vanilla death message from appearing in chat.
                event.setDeathMessage(null);
                event.deathMessage(null);
                // Discord integration... Must be handled here because we're cancelling the death message right after this event is called.
                if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_URL.isEmpty() == true)
                    return;
                // Forwarding message to webhook...
                if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
                    // Setting message placeholders.
                    final String webhookMessage = MiniMessage.miniMessage().stripTags(text)
                            .replace("› ", "") // Very dirty workaround but this had to be done ASAP; Will be improved in the future.
                            .replace("<prefix>", "")
                            .replace("<suffix>", "")
                            .replace("<victim>", "**" + event.getPlayer().getName() + "**")
                            .replace("<victim_displayname>", "**" + event.getPlayer().getName() + "**")
                            .replace("<attacker>", "**" + (event.getDamageSource().getCausingEntity() != null ? event.getDamageSource().getCausingEntity().getName() : "") + "**")
                            .replace("<attacker_displayname>", "**" + (event.getDamageSource().getCausingEntity() != null ? event.getDamageSource().getCausingEntity().getName() : "") + "**");
                    // Creating new instance of WebhookMessageBuilder.
                    final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(webhookMessage);
                    // Setting username if specified.
                    if (PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                        builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_USERNAME));
                    // Setting avatar if specified.
                    if (PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                        builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_AVATAR)).toURL());
                    // Sending the message.
                    builder.sendSilently(plugin.getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_URL);
                }
            }
        }
    }

    /* HIDE ACHIEVEMENT MESSAGES */

    @EventHandler
    public void onAdvancementDone(final @NotNull PlayerAdvancementDoneEvent event) {
        if (PluginConfig.CHAT_HIDE_ADVANCEMENT_MESSAGES == true) {
            event.message(null);
            return;
        }
        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().doesAnnounceToChat() == true && PluginConfig.CHAT_ADVANCEMENT_MESSAGE_FORMAT.isBlank() == false) {
            // Cancelling vanilla message.
            event.message(null);
            // ...
            final Player player = event.getPlayer();
            final Message<String> message = Message.of(PluginConfig.CHAT_ADVANCEMENT_MESSAGE_FORMAT)
                    .placeholder("player", player)
                    .placeholder("displayname", player.displayName())
                    .placeholder("advancement", event.getAdvancement().displayName());
            // ...
            if (PluginConfig.CHAT_ADVANCEMENT_MESSAGE_SEND_GLOBAL == true)
                message.broadcast();
            else message.send(player);
        }
    }

    /* STORE MAXIMUM LEVEL */

    @EventHandler
    public void onLevelChange(final @NotNull PlayerLevelChangeEvent event) {
        final AzureUser user = ((AzureUser) plugin.getUserCache().getUser(event.getPlayer()));
        // Checking if new level is greater than maximum recorded level of this player.
        if (event.getNewLevel() > user.getMaxLevel() == true) {
            // Updating maximum level of this player.
            user.setMaxLevel(event.getNewLevel());
            // Saving...
            plugin.getUserCache().as(AzureUserCache.class).saveUser(user);
        }
    }

    /* TELEPORT PLAYER TO THE WORLD SPAWN WHEN ENTERING THROUGH THE END PORTAL */

    private static final NamespacedKey THE_END = new NamespacedKey("minecraft", "the_end");

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(final @NotNull PlayerTeleportEvent event) {
        if (PluginConfig.GENERAL_END_PORTAL_TELEPORTS_TO_SPAWN == true && event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL && event.getTo().getWorld().key().equals(THE_END) == true)
            event.setTo(plugin.getWorldManager().getSpawnPoint(event.getTo().getWorld()));
    }

    /* CANCEL GENERATION OF END PLATFORM */

    @EventHandler(ignoreCancelled = true)
    public void onPlatformGenerate(final @NotNull PortalCreateEvent event) {
        if (PluginConfig.GENERAL_DISABLE_END_PLATFORM_GENERATION == true && event.getWorld().key().equals(THE_END) == true)
            event.setCancelled(true);
    }

}
