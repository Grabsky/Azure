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

    // Commands > World > Info

    @JsonPath("commands.world_info")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_INFO;

    // Commands > World > Teleport

    @JsonPath("commands.world_teleport")
    public static String COMMAND_WORLD_TELEPORT;

    // Commands > World > Create

    @JsonPath("commands.world_create_success")
    public static String COMMAND_WORLD_CREATE_SUCCESS;

    @JsonPath("commands.world_create_failure_already_exists")
    public static String COMMAND_WORLD_CREATE_FAILURE_ALREADY_EXISTS;

    // Commands > World > Gamerule

    @JsonPath("commands.world_gamerule_view")
    public static String COMMAND_WORLD_GAMERULE_VIEW;

    @JsonPath("commands.world_gamerule_set_success")
    public static String COMMAND_WORLD_GAMERULE_SUCCESS;

    @JsonPath("commands.world_gamerule_set_failure")
    public static String COMMAND_WORLD_GAMERULE_FAILURE;

    // Commands > World > Delete

    @JsonPath("commands.world_delete_success")
    public static String COMMAND_WORLD_DELETE_SUCCESS;

    @JsonPath("commands.world_delete_failure")
    public static String COMMAND_WORLD_DELETE_FAILURE;

    @JsonPath("commands.world_delete_confirm")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_DELETE_CONFIRM;

}
