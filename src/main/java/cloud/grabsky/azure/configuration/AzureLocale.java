package cloud.grabsky.azure.configuration;

import cloud.grabsky.configuration.JsonAdapter;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.configuration.paper.adapter.StringComponentAdapter;
import net.kyori.adventure.text.Component;

public final class AzureLocale implements JsonConfiguration {

    @JsonPath("commands.azure_reload_success")
    public static Component AZURE_RELOAD_SUCCESS;

    @JsonPath("commands.azure_reload_failure")
    public static Component AZURE_RELOAD_FAILURE;

    @JsonPath("commands.give_usage")
    public static Component COMMAND_GIVE_USAGE;

    @JsonPath("commands.give_sent")
    public static String COMMAND_GIVE_SENDER;

    @JsonPath("commands.give_received")
    public static String COMMAND_GIVE_TARGET;

    @JsonPath("commands.world_help")
    public static Component COMMAND_WORLD_HELP;

    @JsonPath("commands.world_info") @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_INFO;

    @JsonPath("commands.world_teleport")
    public static String COMMAND_WORLD_TELEPORT;

    @JsonPath("commands.world_create_success")
    public static String COMMAND_WORLD_CREATE;

    @JsonPath("commands.world_delete_success")
    public static String COMMAND_WORLD_DELETE;

    @JsonPath("commands.world_delete_confirm") @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WORLD_DELETE_CONFIRM;

}
