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
package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.bedrock.components.Message;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.cacheddata.CachedMetaData;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.javacord.api.entity.message.WebhookMessageBuilder;

import java.net.URI;
import java.util.HashSet;

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
            plugin.getBedrockScheduler().run(1L, (task) -> player.teleportAsync(plugin.getWorldManager().getSpawnPoint(primaryWorld)));
        }
        // Clearing title. (if enabled)
        if (PluginConfig.GENERAL_CLEAR_TITLE_ON_JOIN == true)
            player.clearTitle();
        // Setting join message to null because it needs to be handled manually.
        event.joinMessage(null);
        // Sending join message to audience that can see the player associated with the event.
        if (PluginConfig.CHAT_SERVER_JOIN_MESSAGE.isBlank() == false) {
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

    @EventHandler // We need to somehow ensure this is called AFTER AzureUserCache#onPlayerJoin(...) listener. I guess registration order is enough?
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        // Setting quit message to null because it needs to be handled manually.
        event.quitMessage(null);
        // Sending quit message to audience that can see the player associated with the event.
        if (PluginConfig.CHAT_SERVER_JOIN_MESSAGE.isBlank() == false) {
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

    /* WORLD RESPAWN - Respawns players on spawn-point of the main world. */

    @EventHandler
    public void onPlayerRespawn(final @NotNull PlayerRespawnEvent event) {
        // Setting respawn location to spawn point of the primary world. (if enabled)
        if (PluginConfig.GENERAL_RESPAWN_ON_PRIMARY_WORLD_SPAWN == true) {
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

    /* DISCORD INTEGRATIONS - FORWARDING QUIT MESSAGE TO DISCORD SERVER */

    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitForward(final @NotNull PlayerQuitEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
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

    /* HIDE DEATH MESSAGES */

    @EventHandler @SuppressWarnings("deprecation") // Cancelling death message using 'PlayerDeathEvent#deathMessage(null)' was not working, hence why deprecated method is used.
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event) {
        if (PluginConfig.CHAT_HIDE_DEATH_MESSAGES == true)
            event.setDeathMessage(null);
    }

    /* HIDE ACHIEVEMENT MESSAGES */

    @EventHandler
    public void onAdvancementDone(final @NotNull PlayerAdvancementDoneEvent event) {
        if (PluginConfig.CHAT_HIDE_ADVANCEMENT_MESSAGES == true) {
            event.message(null);
            return;
        }
        if (PluginConfig.CHAT_ADVANCEMENT_MESSAGE_FORMAT.isBlank() == false) {
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

}
