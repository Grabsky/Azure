package cloud.grabsky.azure.configuration;

import cloud.grabsky.configuration.JsonAdapter;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.configuration.paper.adapter.StringComponentAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public final class PluginLocale implements JsonConfiguration {

    @JsonPath("missing_permissions")
    public static Component MISSING_PERMISSIONS;

    @JsonPath("reload_success")
    public static Component RELOAD_SUCCESS;

    @JsonPath("reload_failure")
    public static Component RELOAD_FAILURE;

    // ...

    @JsonPath("yes")
    public static Component YES;

    @JsonPath("no")
    public static Component NO;

    public static @UnknownNullability Component getBooleanShort(final boolean bool) {
        return bool == true ? YES : NO;
    }

    // ...

    @JsonPath("enabled")
    public static Component ENABLED;

    @JsonPath("disabled")
    public static Component DISABLED;

    public static @UnknownNullability Component getBooleanLong(final boolean bool) {
        return bool == true ? ENABLED : DISABLED;
    }

    // ...

    @JsonPath("adventure")
    public static Component ADVENTURE;

    @JsonPath("creative")
    public static Component CREATIVE;

    @JsonPath("spectator")
    public static Component SPECTATOR;

    @JsonPath("survival")
    public static Component SURVIVAL;

    public static @UnknownNullability Component getGameMode(final @NotNull GameMode mode) {
        return switch (mode) {
            case ADVENTURE -> PluginLocale.ADVENTURE;
            case CREATIVE -> PluginLocale.CREATIVE;
            case SPECTATOR -> PluginLocale.SPECTATOR;
            case SURVIVAL -> PluginLocale.SURVIVAL;
        };
    }

    // Chat

    @JsonPath("chat.on_cooldown")
    public static Component CHAT_ON_COOLDOWN;

    @JsonPath("chat.muted")
    public static String CHAT_MUTED;

    @JsonPath("chat.muted_permanent")
    public static Component CHAT_MUTED_PERMANENT;

    // Commands > Azure

    @JsonPath("commands.azure_help")
    public static Component COMMAND_AZURE_HELP;

    // Commands > Ban

    @JsonPath("commands.ban_usage")
    public static Component COMMAND_BAN_USAGE;

    @JsonPath("commands.ban_success")
    public static String COMMAND_BAN_SUCCESS;

    @JsonPath("commands.ban_success_permanent")
    public static String COMMAND_BAN_SUCCESS_PERMANENT;

    @JsonPath("commands.ban_disconnect_message")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_BAN_DISCONNECT_MESSAGE;

    @JsonPath("commands.ban_disconnect_message_permanent")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_BAN_DISCONNECT_MESSAGE_PERMANENT;

    // Commands > Unban

    @JsonPath("commands.unban_usage")
    public static Component COMMAND_UNBAN_USAGE;

    @JsonPath("commands.unban_success")
    public static String COMMAND_UNBAN_SUCCESS;

    @JsonPath("commands.unban_failure_player_not_banned")
    public static Component COMMAND_UNBAN_FAILURE_PLAYER_NOT_BANNED;

    // Commands > Unmute

    @JsonPath("commands.unmute_usage")
    public static Component COMMAND_UNMUTE_USAGE;

    @JsonPath("commands.unmute_success")
    public static String COMMAND_UNMUTE_SUCCESS;

    @JsonPath("commands.unmute_failure_player_not_muted")
    public static Component COMMAND_UNMUTE_FAILURE_PLAYER_NOT_BANNED;

    // Commands > Mute

    @JsonPath("commands.mute_usage")
    public static Component COMMAND_MUTE_USAGE;

    @JsonPath("commands.mute_success")
    public static String COMMAND_MUTE_SUCCESS;

    @JsonPath("commands.mute_success_permanent")
    public static String COMMAND_MUTE_SUCCESS_PERMANENT;

    // Command > Kick

    @JsonPath("commands.kick_usage")
    public static Component COMMAND_KICK_USAGE;

    @JsonPath("commands.kick_success")
    public static String COMMAND_KICK_SUCCESS;

    @JsonPath("commands.kick_disconnect_message")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_KICK_DISCONNECT_MESSAGE;

    // Commands > Delete

    @JsonPath("commands.delete_usage")
    public static Component COMMAND_DELETE_USAGE;

    @JsonPath("commands.delete_failure")
    public static Component COMMAND_DELETE_FAILURE;

    // Commands > Inventory

    @JsonPath("commands.inventory_usage")
    public static Component COMMAND_INVENTORY_USAGE;

    // Commands > Inventory

    @JsonPath("commands.enderchest_usage")
    public static Component COMMAND_ENDERCHEST_USAGE;

    // Commands > Gamemode

    @JsonPath("commands.gamemode_usage")
    public static Component COMMAND_GAMEMODE_USAGE;

    @JsonPath("commands.gamemode_info")
    public static String COMMAND_GAMEMODE_INFO;

    @JsonPath("commands.gamemode_set_success_sender")
    public static String COMMAND_GAMEMODE_SET_SUCCESS_SENDER;

    @JsonPath("commands.gamemode_set_success_target")
    public static String COMMAND_GAMEMODE_SET_SUCCESS_TARGET;

    // Commands > Give

    @JsonPath("commands.give_usage")
    public static Component COMMAND_GIVE_USAGE;

    @JsonPath("commands.give_success_sender")
    public static String COMMAND_GIVE_SUCCESS_SENDER;

    @JsonPath("commands.give_success_target")
    public static String COMMAND_GIVE_SUCCESS_TARGET;

    // Commands > Pack

    @JsonPath("commands.pack_help")
    public static Component COMMAND_PACK_HELP;

    @JsonPath("commands.pack_notify_confirm")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_PACK_NOTIFY_CONFIRM;

    @JsonPath("commands.pack_notification")
    public static Component COMMAND_PACK_NOTIFICATION;

    // Commands > Speed

    @JsonPath("commands.speed_usage")
    public static Component COMMAND_SPEED_USAGE;

    @JsonPath("commands.speed_set_success_walk")
    public static String COMMAND_SPEED_SET_SUCCESS_WALK;

    @JsonPath("commands.speed_set_success_fly")
    public static String COMMAND_SPEED_SET_SUCCESS_FLY;

    // Commands > Teleport

    @JsonPath("commands.teleport_usage")
    public static Component COMMAND_TELEPORT_USAGE;

    @JsonPath("commands.teleport_player_success_sender")
    public static String COMMAND_TELEPORT_PLAYER_SUCCESS_SENDER;

    @JsonPath("commands.teleport_player_success_target")
    public static String COMMAND_TELEPORT_PLAYER_SUCCESS_TARGET;

    @JsonPath("commands.teleport_player_success_destination")
    public static String COMMAND_TELEPORT_PLAYER_SUCCESS_DESTINATION;

    @JsonPath("commands.teleport_position_success_sender")
    public static String COMMAND_TELEPORT_POSITION_SUCCESS_SENDER;

    @JsonPath("commands.teleport_position_success_target")
    public static String COMMAND_TELEPORT_POSITION_SUCCESS_TARGET;

    @JsonPath("commands.teleport_player_failure_targets_are_the_same")
    public static Component TELEPORT_PLAYER_FAILURE_TARGETS_ARE_THE_SAME;

    // Commands > World

    @JsonPath("commands.world_help")
    public static Component COMMAND_WORLD_HELP;

    // Commands > World > Autoload

    @JsonPath("commands.world_autoload_usage")
    public static Component COMMAND_WORLD_AUTOLOAD_USAGE;

    @JsonPath("commands.world_autoload_success_on")
    public static String COMMAND_WORLD_AUTOLOAD_SUCCESS_ON;

    @JsonPath("commands.world_autoload_success_off")
    public static String COMMAND_WORLD_AUTOLOAD_SUCCESS_OFF;

    @JsonPath("commands.world_autoload_failure")
    public static String COMMAND_WORLD_AUTOLOAD_FAILURE;

    // Commands > World > Create

    @JsonPath("commands.world_create_usage")
    public static Component COMMAND_WORLD_CREATE_USAGE;

    @JsonPath("commands.world_create_success")
    public static String COMMAND_WORLD_CREATE_SUCCESS;

    @JsonPath("commands.world_create_failure_already_exists")
    public static String COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS;

    @JsonPath("commands.world_create_failure_other")
    public static String COMMAND_WORLD_CREATE_FAILURE_OTHER;

    // Commands > World > Description

    @JsonPath("commands.world_description_usage")
    public static Component COMMAND_WORLD_DESCRIPTION_USAGE;

    @JsonPath("commands.world_description_set_success")
    public static String COMMAND_WORLD_DESCRIPTION_SET_SUCCESS;

    @JsonPath("commands.world_description_reset_success")
    public static String COMMAND_WORLD_DESCRIPTION_RESET_SUCCESS;

    @JsonPath("commands.world_description_set_failure_not_in_range")
    public static String COMMAND_WORLD_DESCRIPTION_SET_FAILURE_NOT_IN_RANGE;

    // Commands > World > Delete

    @JsonPath("commands.world_delete_usage")
    public static Component COMMAND_WORLD_DELETE_USAGE;

    @JsonPath("commands.world_delete_success")
    public static String COMMAND_WORLD_DELETE_SUCCESS;

    @JsonPath("commands.world_delete_failure_primary_world")
    public static Component COMMAND_WORLD_DELETE_FAILURE_PRIMARY_WORLD;

    @JsonPath("commands.world_delete_failure_other")
    public static String COMMAND_WORLD_DELETE_FAILURE_OTHER;

    @JsonPath("commands.world_delete_confirm")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_DELETE_CONFIRM;

    // Commands > World > Gamerule

    @JsonPath("commands.world_gamerule_usage")
    public static Component COMMAND_WORLD_GAMERULE_USAGE;

    @JsonPath("commands.world_gamerule_info")
    public static String COMMAND_WORLD_GAMERULE_INFO;

    @JsonPath("commands.world_gamerule_set_success")
    public static String COMMAND_WORLD_GAMERULE_SET_SUCCESS;

    @JsonPath("commands.world_gamerule_set_failure")
    public static String COMMAND_WORLD_GAMERULE_SET_FAILURE;

    // Commands > World > List

    @JsonPath("commands.world_list_header")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_LIST_HEADER;

    @JsonPath("commands.world_list_footer")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_LIST_FOOTER;

    @JsonPath("commands.world_list_entry")
    public static String COMMAND_WORLD_LIST_ENTRY;

    @JsonPath("commands.world_list_entry_no_description")
    public static String COMMAND_WORLD_LIST_ENTRY_NO_DESCRIPTION;

    // Commands > World > Load

    @JsonPath("commands.world_load_usage")
    public static Component COMMAND_WORLD_LOAD_USAGE;

    @JsonPath("commands.world_load_success")
    public static String COMMAND_WORLD_LOAD_SUCCESS;

    @JsonPath("commands.world_load_failure_not_found")
    public static String COMMAND_WORLD_LOAD_FAILURE_NOT_FOUND;

    @JsonPath("commands.world_load_failure_other")
    public static String COMMAND_WORLD_LOAD_FAILURE_OTHER;

    // Commands > World > Info

    @JsonPath("commands.world_info")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_INFO;

    // Commands > World > Load

    @JsonPath("commands.world_import_usage")
    public static Component COMMAND_WORLD_IMPORT_USAGE;

    @JsonPath("commands.world_import_success")
    public static String COMMAND_WORLD_IMPORT_SUCCESS;

    @JsonPath("commands.world_import_failure_not_found")
    public static String COMMAND_WORLD_IMPORT_FAILURE_NOT_FOUND;

    @JsonPath("commands.world_import_failure_other")
    public static String COMMAND_WORLD_IMPORT_FAILURE_OTHER;

    // Commands > World > Spawnpoint

    @JsonPath("commands.world_spawnpoint_usage")
    public static Component COMMAND_WORLD_SPAWNPOINT_USAGE;

    @JsonPath("commands.world_spawnpoint_set_success")
    public static String COMMAND_WORLD_SPAWNPOINT_SET_SUCCESS;

    // Commands > World > Teleport

    @JsonPath("commands.world_teleport_usage")
    public static Component COMMAND_WORLD_TELEPORT_USAGE;

    @JsonPath("commands.world_teleport_success")
    public static String COMMAND_WORLD_TELEPORT_SUCCESS;

    // Commands > World > Time

    @JsonPath("commands.world_time_usage")
    public static Component COMMAND_WORLD_TIME_USAGE;

    @JsonPath("commands.world_time_info")
    public static String COMMAND_WORLD_TIME_INFO;

    @JsonPath("commands.world_time_set_success")
    public static String COMMAND_WORLD_TIME_SET_SUCCESS;

    // Commands > World > Unload

    @JsonPath("commands.world_unload_usage")
    public static Component COMMAND_WORLD_UNLOAD_USAGE;

    @JsonPath("commands.world_unload_success")
    public static String COMMAND_WORLD_UNLOAD_SUCCESS;

    @JsonPath("commands.world_unload_failure_primary_world")
    public static Component COMMAND_WORLD_UNLOAD_FAILURE_PRIMARY_WORLD;

    @JsonPath("commands.world_unload_failure_other")
    public static String COMMAND_WORLD_UNLOAD_FAILURE_OTHER;

    // Commands > World > Weather

    @JsonPath("commands.world_weather_usage")
    public static Component COMMAND_WORLD_WEATHER_USAGE;

    @JsonPath("commands.world_weather_set_success")
    public static String COMMAND_WORLD_WEATHER_SET_SUCCESS;

    @JsonPath("commands.world_weather_set_failure_invalid_type")
    public static String COMMAND_WORLD_WEATHER_SET_FAILURE_INVALID_TYPE;

    // Commands > Vanish

    @JsonPath("commands.vanish_usage")
    public static Component COMMAND_VANISH_USAGE;

    @JsonPath("commands.vanish_success")
    public static String COMMAND_VANISH_SUCCESS;

    @JsonPath("commands.vanish_success_target")
    public static String COMMAND_VANISH_SUCCESS_TARGET;

    // Commands > Spy

    @JsonPath("commands.spy_usage")
    public static Component COMMAND_SPY_USAGE;

    @JsonPath("commands.spy_success")
    public static String COMMAND_SPY_SUCCESS;

    @JsonPath("commands.spy_success_target")
    public static String COMMAND_SPY_SUCCESS_TARGET;

    @JsonPath("commands.spy_message_format")
    public static String COMMAND_SPY_MESSAGE_FORMAT;

    @JsonPath("commands.spy_message_format_console")
    public static String COMMAND_SPY_MESSAGE_FORMAT_CONSOLE;

    // Commands > Invulnerable

    @JsonPath("commands.invulnerable_usage")
    public static Component COMMAND_INVULNERABLE_USAGE;

    @JsonPath("commands.invulnerable_success")
    public static String COMMAND_INVULNERABLE_SUCCESS;

    @JsonPath("commands.invulnerable_success_target")
    public static String COMMAND_INVULNERABLE_SUCCESS_TARGET;

    // Commands > Heal

    @JsonPath("commands.heal_usage")
    public static Component COMMAND_HEAL_USAGE;

    @JsonPath("commands.heal_success")
    public static String COMMAND_HEAL_SUCCESS;

    @JsonPath("commands.heal_success_target")
    public static Component COMMAND_HEAL_SUCCESS_TARGET;

    // Commands > Feed

    @JsonPath("commands.feed_usage")
    public static Component COMMAND_FEED_USAGE;

    @JsonPath("commands.feed_success")
    public static String COMMAND_FEED_SUCCESS;

    @JsonPath("commands.feed_success_target")
    public static Component COMMAND_FEED_SUCCESS_TARGET;

    // Commands > Player

    @JsonPath("commands.player_usage")
    public static Component COMMAND_PLAYER_USAGE;

    @JsonPath("commands.player_hidden_entry")
    public static Component COMMAND_PLAYER_HIDDEN_ENTRY;

    @JsonPath("commands.player_success_online")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_PLAYER_SUCCESS_ONLINE;

    @JsonPath("commands.player_success_offline")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_PLAYER_SUCCESS_OFFLINE;

    @JsonPath("commands.player_failure")
    public static Component COMMAND_PLAYER_FAILURE;

    // Commands > Message

    @JsonPath("commands.message_usage")
    public static Component COMMAND_MESSAGE_USAGE;

    @JsonPath("commands.message_success_to")
    public static String COMMAND_MESSAGE_SUCCESS_TO;

    @JsonPath("commands.message_success_from")
    public static String COMMAND_MESSAGE_SUCCESS_FROM;

    @JsonPath("commands.message_failure_recipient_must_not_be_sender")
    public static Component COMMAND_MESSAGE_FAILURE_RECIPIENT_MUST_NOT_BE_SENDER;

    // Commands > Reply

    @JsonPath("commands.reply_usage")
    public static Component COMMAND_REPLY_USAGE;

    @JsonPath("commands.reply_success_to")
    public static String COMMAND_REPLY_SUCCESS_TO;

    @JsonPath("commands.reply_success_from")
    public static String COMMAND_REPLY_SUCCESS_FROM;

    @JsonPath("commands.reply_failure")
    public static Component COMMAND_REPLY_FAILURE;


    public static final class Commands implements JsonConfiguration {

        // Commands > General

        @JsonPath("missing_permissions")
        public static Component MISSING_PERMISSIONS;

        // Commands > Executors

        @JsonPath("invalid_executor_player")
        public static Component INVALID_EXECUTOR_PLAYER;

        @JsonPath("invalid_executor_console")
        public static Component INVALID_EXECUTOR_CONSOLE;

        // Commands > Arguments

        @JsonPath("invalid_boolean")
        public static String INVALID_BOOLEAN;

        @JsonPath("invalid_short")
        public static String INVALID_SHORT;

        @JsonPath("invalid_short_not_in_range")
        public static String INVALID_SHORT_NOT_IN_RANGE;

        @JsonPath("invalid_integer")
        public static String INVALID_INTEGER;

        @JsonPath("invalid_integer_not_in_range")
        public static String INVALID_INTEGER_NOT_IN_RANGE;

        @JsonPath("invalid_long")
        public static String INVALID_LONG;

        @JsonPath("invalid_long_not_in_range")
        public static String INVALID_LONG_NOT_IN_RANGE;

        @JsonPath("invalid_float")
        public static String INVALID_FLOAT;

        @JsonPath("invalid_float_not_in_range")
        public static String INVALID_FLOAT_NOT_IN_RANGE;

        @JsonPath("invalid_double")
        public static String INVALID_DOUBLE;

        @JsonPath("invalid_double_not_in_range")
        public static String INVALID_DOUBLE_NOT_IN_RANGE;

        @JsonPath("invalid_uuid")
        public static String INVALID_UUID;

        @JsonPath("invalid_player")
        public static String INVALID_PLAYER;

        @JsonPath("invalid_offline_player")
        public static String INVALID_OFFLINE_PLAYER;

        @JsonPath("invalid_world")
        public static String INVALID_WORLD;

        @JsonPath("invalid_enchantment")
        public static String INVALID_ENCHANTMENT;

        @JsonPath("invalid_material")
        public static String INVALID_MATERIAL;

        @JsonPath("invalid_entity_type")
        public static String INVALID_ENTITY_TYPE;

        @JsonPath("invalid_namespacedkey")
        public static String INVALID_NAMESPACEDKEY;

        @JsonPath("invalid_position")
        public static String INVALID_POSITION;

        /* CUSTOM */

        @JsonPath("invalid_gamemode")
        public static String INVALID_GAMEMODE;

        @JsonPath("invalid_gamerule")
        public static String INVALID_GAMERULE;

        @JsonPath("invalid_world_environment")
        public static String INVALID_WORLD_ENVIRONMENT;

        @JsonPath("invalid_world_type")
        public static String INVALID_WORLD_TYPE;

        @JsonPath("invalid_direction")
        public static String INVALID_DIRECTION;

        @JsonPath("invalid_interval")
        public static String INVALID_INTERVAL;

        @JsonPath("invalid_interval_not_in_range")
        public static String INVALID_INTERVAL_NOT_IN_RANGE;

    }

}
