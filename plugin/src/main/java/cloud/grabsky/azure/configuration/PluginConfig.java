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
package cloud.grabsky.azure.configuration;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import com.squareup.moshi.Json;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.javacord.api.entity.activity.ActivityType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.azure.util.Iterables.reversed;

public final class PluginConfig implements JsonConfiguration {

    // General Settings

    @JsonPath("general_settings.clear_title_on_join")
    public static boolean GENERAL_CLEAR_TITLE_ON_JOIN;

    @JsonPath("general_settings.respawn_on_primary_world_spawn")
    public static boolean GENERAL_RESPAWN_ON_PRIMARY_WORLD_SPAWN;

    @JsonPath("general_settings.teleport_new_players_to_primary_world_spawn")
    public static boolean GENERAL_TELEPORT_NEW_PLAYERS_TO_PRIMARY_WORLD_SPAWN;

    @JsonPath("general_settings.use_blocked_command_error_message_for_unknown_command")
    public static boolean USE_BLOCKED_COMMAND_ERROR_MESSAGE_FOR_UNKNOWN_COMMAND;

    // Chat Settings

    @JsonPath("chat_settings.cooldown")
    public static Long CHAT_COOLDOWN;

    // Chat Settings > Filtering

    @JsonPath("chat_settings.filtering.disallow_invalid_characters")
    public static boolean CHAT_FILTERING_DISALLOW_INVALID_CHARACTERS;

    @JsonPath("chat_settings.filtering.disallow_inappropriate_words")
    public static boolean CHAT_FILTERING_DISALLOW_INAPPROPRIATE_WORDS;

    @JsonPath("chat_settings.filtering.punishment_commands")
    public static List<String> CHAT_FILTERING_PUNISHMENT_COMMANDS;

    // Chat Settings > Chat Format

    @JsonPath("chat_settings.chat_format.default")
    public static String CHAT_FORMATS_DEFAULT;

    @JsonPath("chat_settings.chat_format.console")
    public static String CHAT_FORMATS_CONSOLE;

    @JsonPath("chat_settings.chat_format.extra")
    public static List<FormatHolder> CHAT_FORMATS_EXTRA;

    // Chat Settings > Chat Format > Tags

    @JsonPath("chat_settings.chat_format.tags.default")
    public static TagResolver CHAT_MESSAGE_TAGS_DEFAULT;

    @JsonPath("chat_settings.chat_format.tags.extra")
    public static List<TagsHolder> CHAT_MESSAGE_TAGS_EXTRA;

    // Chat Settings > Moderation > Message Deletion

    @JsonPath("chat_settings.moderation.message_deletion.enabled")
    public static boolean CHAT_MODERATION_MESSAGE_DELETION_ENABLED;

    @JsonPath("chat_settings.moderation.message_deletion.cache_expiration_rate")
    public static long CHAT_MODERATION_MESSAGE_DELETION_CACHE_EXPIRATION_RATE;

    @JsonPath("chat_settings.moderation.message_deletion.button")
    public static DeleteButton CHAT_MODERATION_MESSAGE_DELETION_BUTTON;


    // Punishment Settings

    @JsonPath("punishment_settings.default_reason")
    public static String PUNISHMENT_SETTINGS_DEFAULT_REASON;


    // Command Filter

    @JsonPath("command_filter.enabled")
    public static boolean COMMAND_FILTER_ENABLED;

    @JsonPath("command_filter.use_as_blacklist")
    public static boolean COMMAND_FILTER_USE_AS_BLACKLIST;

    @JsonPath("command_filter.block_filtered_commands")
    public static boolean COMMAND_FILTER_BLOCK_FILTERED_COMMANDS;

    @JsonPath("command_filter.blocked_command_error_message")
    public static Component BLOCKED_COMMAND_ERROR_MESSAGE;

    @JsonPath("command_filter.default")
    public static List<String> COMMAND_FILTER_DEFAULT;

    @JsonPath("command_filter.extra")
    public static List<CommandsHolder> COMMAND_FILTER_EXTRA;


    // Resource Pack

    @JsonPath("resource_pack.public_access_address")
    public static String RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS;

    @JsonPath("resource_pack.port")
    public static int RESOURCE_PACK_PORT;

    @JsonPath("resource_pack.pack_files")
    public static List<String> RESOURCE_PACK_FILES;

    @JsonPath("resource_pack.send_on_join")
    public static boolean RESOURCE_PACK_SEND_ON_JOIN;

    @JsonPath("resource_pack.is_required")
    public static boolean RESOURCE_PACK_IS_REQUIRED;

    @JsonPath("resource_pack.prompt_message")
    public static Component RESOURCE_PACK_PROMPT_MESSAGE;

    @JsonNullable @JsonPath("resource_pack.notification_sound")
    public static @Nullable Sound RESOURCE_PACK_NOTIFICATION_SOUND;


    // Vanish

    @JsonNullable @JsonPath("vanish.bossbar")
    public static BossBar VANISH_BOSS_BAR;


    // Discord Integrations

    @JsonPath("discord_integrations.enabled")
    public static boolean DISCORD_INTEGRATIONS_ENABLED;

    @JsonPath("discord_integrations.discord_bot_token")
    public static String DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN;

    @JsonPath("discord_integrations.discord_bot_activity")
    public static ActivityWrapper DISCORD_INTEGRATIONS_BOT_ACTIVITY;

    // Discord Integrations > Chat Forwarding

    @JsonPath("discord_integrations.chat_forwarding.enabled")
    public static boolean DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED;

    @JsonPath("discord_integrations.chat_forwarding.channel_id")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHANNEL_ID;

    @JsonPath("discord_integrations.chat_forwarding.webhook_url")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_URL;

    @JsonPath("discord_integrations.chat_forwarding.webhook_username")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_USERNAME;

    @JsonPath("discord_integrations.chat_forwarding.webhook_avatar")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_AVATAR;

    @JsonPath("discord_integrations.chat_forwarding.chat_format")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHAT_FORMAT;

    @JsonPath("discord_integrations.chat_forwarding.console_format")
    public static String DISCORD_INTEGRATIONS_CHAT_FORWARDING_CONSOLE_FORMAT;

    // Discord Integrations > Join and Quit Forwarding

    @JsonPath("discord_integrations.join_and_quit_forwarding.enabled")
    public static boolean DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED;

    @JsonPath("discord_integrations.join_and_quit_forwarding.webhook_url")
    public static String DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL;

    @JsonPath("discord_integrations.join_and_quit_forwarding.webhook_username")
    public static String DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME;

    @JsonPath("discord_integrations.join_and_quit_forwarding.webhook_avatar")
    public static String DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR;

    @JsonPath("discord_integrations.join_and_quit_forwarding.join_message_format")
    public static String DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_JOIN_MESSAGE_FORMAT;

    @JsonPath("discord_integrations.join_and_quit_forwarding.quit_message_format")
    public static String DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_QUIT_MESSAGE_FORMAT;

    // Discord Integrations > Start and Stop Forwarding

    @JsonPath("discord_integrations.start_and_stop_forwarding.enabled")
    public static boolean DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_ENABLED;

    @JsonPath("discord_integrations.start_and_stop_forwarding.webhook_url")
    public static String DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL;

    @JsonPath("discord_integrations.start_and_stop_forwarding.webhook_username")
    public static String DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME;

    @JsonPath("discord_integrations.start_and_stop_forwarding.webhook_avatar")
    public static String DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR;

    @JsonPath("discord_integrations.start_and_stop_forwarding.start_message_format")
    public static String DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_START_MESSAGE_FORMAT;

    @JsonPath("discord_integrations.start_and_stop_forwarding.stop_message_format")
    public static String DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_STOP_MESSAGE_FORMAT;


    // Discord Integrations > Punishments Forwarding

    @JsonPath("discord_integrations.punishments_forwarding.enabled")
    public static boolean DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED;

    @JsonPath("discord_integrations.punishments_forwarding.webhook_url")
    public static String DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL;

    // Server Links

    @JsonPath("server_links")
    public static List<ServerLinkWrapper> SERVER_LINKS;


    // Disabled Recipes

    @JsonPath("disabled_recipes")
    public static List<NamespacedKey> DISABLED_RECIPES;


    /* CONFIGURATION LIFECYCLE */

    @Override @SuppressWarnings("UnstableApiUsage")
    public void onReload() {
        ChatManager.CHAT_FORMATS_REVERSED = reversed(PluginConfig.CHAT_FORMATS_EXTRA);
        ChatManager.CHAT_TAGS_REVERSED = reversed(PluginConfig.CHAT_MESSAGE_TAGS_EXTRA);
        // Iterating over disabled recipes list and removing them from the server. May not be the best place to do that.
        DISABLED_RECIPES.forEach(key -> {
            if (Bukkit.removeRecipe(key) == false)
                Azure.getInstance().getLogger().warning("Unknown recipe \"" + key.asString() + "\" could not be disabled.");
        });
        // Updating recipes for all players.
        Bukkit.updateRecipes();
        // Removing server links currently stored within the server.
        Bukkit.getServerLinks().getLinks().forEach(it -> {
            Bukkit.getServerLinks().removeLink(it);
        });
        // Adding new links.
        SERVER_LINKS.forEach(it -> {
            try {
                Bukkit.getServerLinks().addLink(it.name, new URI(it.url));
            } catch (final URISyntaxException e) {
                Bukkit.getLogger().severe("Conversion of server link URI failed due to following error(s):");
                Bukkit.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        });
    }


    /* SURROGATES */

    // Moshi should be able to create instance of the object despite the constructor being private.
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FormatHolder {

        @Getter(AccessLevel.PUBLIC)
        private final String permission;

        @Getter(AccessLevel.PUBLIC)
        private final String format;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TagsHolder {

        @Getter(AccessLevel.PUBLIC)
        private final String permission;

        @Getter(AccessLevel.PUBLIC)
        private final TagResolver tags;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CommandsHolder {

        @Getter(AccessLevel.PUBLIC)
        private final String permission;

        @Getter(AccessLevel.PUBLIC)
        private final List<String> commands;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DeleteButton {

        public enum Position { BEFORE, AFTER }

        @Getter(AccessLevel.PUBLIC)
        private final Position position;

        @Getter(AccessLevel.PUBLIC)
        private final Component text;

        @Getter(AccessLevel.PUBLIC)
        private final Component hover;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ActivityWrapper {

        @Json(name = "refresh_rate")
        @Getter(AccessLevel.PUBLIC)
        private final int refreshRate;

        @Getter(AccessLevel.PUBLIC)
        private final ActivityType type;

        @Getter(AccessLevel.PUBLIC)
        private final String state;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ServerLinkWrapper {

        @Getter(AccessLevel.PUBLIC)
        private final Component name;

        @Getter(AccessLevel.PUBLIC)
        private final String url;


    }

}
