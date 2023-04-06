package cloud.grabsky.azure.configuration;

import cloud.grabsky.configuration.JsonAdapter;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.configuration.paper.adapter.StringComponentAdapter;
import net.kyori.adventure.text.Component;

public final class PluginLocale implements JsonConfiguration {

    @JsonPath("missing_permissions")
    public static Component MISSING_PERMISSIONS;

    @JsonPath("reload_success")
    public static Component RELOAD_SUCCESS;

    @JsonPath("reload_failure")
    public static Component RELOAD_FAILURE;

    // Chat

    @JsonPath("chat.on_cooldown")
    public static Component CHAT_ON_COOLDOWN;

    // Commands > Azure

    @JsonPath("commands.azure_help")
    public static Component COMMAND_AZURE_HELP;

    // Commands > Delete

    @JsonPath("commands.delete_usage")
    public static Component COMMAND_DELETE_USAGE;

    @JsonPath("commands.delete_failure")
    public static Component COMMAND_DELETE_FAILURE;

    // Commands > Give

    @JsonPath("commands.give_usage")
    public static Component COMMAND_GIVE_USAGE;

    @JsonPath("commands.give_sent")
    public static String COMMAND_GIVE_SENDER;

    @JsonPath("commands.give_received")
    public static String COMMAND_GIVE_TARGET;

    // Commands > World

    @JsonPath("commands.world_help")
    public static Component COMMAND_WORLD_HELP;

    // Commands > World > Create

    @JsonPath("commands.world_create_usage")
    public static Component COMMAND_WORLD_CREATE_USAGE;

    @JsonPath("commands.world_create_success")
    public static String COMMAND_WORLD_CREATE_SUCCESS;

    @JsonPath("commands.world_create_failure_already_exists")
    public static String COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS;

    @JsonPath("commands.world_create_failure_other")
    public static String COMMAND_WORLD_CREATE_FAILURE_OTHER;

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

    // Commands > World > Info

    @JsonPath("commands.world_info")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_INFO;

    // Commands > World > Load

    @JsonPath("commands.world_load_usage")
    public static Component COMMAND_WORLD_LOAD_USAGE;

    @JsonPath("commands.world_load_success")
    public static String COMMAND_WORLD_LOAD_SUCCESS;

    @JsonPath("commands.world_load_failure_not_found")
    public static String COMMAND_WORLD_LOAD_FAILURE_NOT_FOUND;

    @JsonPath("commands.world_load_failure_other")
    public static String COMMAND_WORLD_LOAD_FAILURE_OTHER;

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

    // Commands > World > Weather

    @JsonPath("commands.world_weather_usage")
    public static Component COMMAND_WORLD_WEATHER_USAGE;

    @JsonPath("commands.world_weather_set_success")
    public static String COMMAND_WORLD_WEATHER_SET_SUCCESS;

    @JsonPath("commands.world_weather_set_failure_invalid_type")
    public static String COMMAND_WORLD_WEATHER_SET_FAILURE_INVALID_TYPE;

    // Commands > Speed

    @JsonPath("commands.speed_usage")
    public static Component COMMAND_SPEED_USAGE;

    @JsonPath("commands.speed_set_success_walk")
    public static String COMMAND_SPEED_SET_SUCCESS_WALK;

    @JsonPath("commands.speed_set_success_fly")
    public static String COMMAND_SPEED_SET_SUCCESS_FLY;

    // Commands > Pack

    @JsonPath("commands.pack_help")
    public static Component COMMAND_PACK_HELP;

    @JsonPath("commands.pack_notify_confirm")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_PACK_NOTIFY_CONFIRM;

    @JsonPath("commands.pack_notification")
    public static Component COMMAND_PACK_NOTIFICATION;

    // Commands > Gamemode

    @JsonPath("commands.gamemode_usage")
    public static Component COMMAND_GAMEMODE_USAGE;

    @JsonPath("commands.gamemode_info")
    public static String COMMAND_GAMEMODE_INFO;

    @JsonPath("commands.gamemode_set_success_sender")
    public static String COMMAND_GAMEMODE_SET_SUCCESS_SENDER;

    @JsonPath("commands.gamemode_set_success_target")
    public static String COMMAND_GAMEMODE_SET_SUCCESS_TARGET;


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

    }

}
