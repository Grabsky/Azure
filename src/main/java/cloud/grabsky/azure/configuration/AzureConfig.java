package cloud.grabsky.azure.configuration;

import cloud.grabsky.azure.chat.holder.FormatHolder;
import cloud.grabsky.azure.chat.holder.TagsHolder;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

public final class AzureConfig implements JsonConfiguration {

    // Chat Settings

    @JsonPath("chat_settings.cooldown")
    public static Long CHAT_COOLDOWN;

    // Chat Settings > Formats

    @JsonPath("chat_settings.formats.default")
    public static String CHAT_FORMATS_DEFAULT;

    @JsonPath("chat_settings.formats.console")
    public static String CHAT_FORMATS_CONSOLE;

    @JsonPath("chat_settings.formats.extra")
    public static List<FormatHolder> CHAT_FORMATS_EXTRA;

    // Chat Settings > Message Tags

    @JsonPath("chat_settings.message_tags.default")
    public static TagResolver CHAT_MESSAGE_TAGS_DEFAULT;

    @JsonPath("chat_settings.message_tags.extra")
    public static List<TagsHolder> CHAT_MESSAGE_TAGS_EXTRA;

    // Chat Settings > Moderation > Message Deletion

    @JsonPath("chat_settings.moderation.message_deletion.enabled")
    public static boolean CHAT_MODERATION_MESSAGE_DELETION_ENABLED;

    @JsonPath("chat_settings.moderation.message_deletion.button_text")
    public static Component CHAT_MODERATION_MESSAGE_DELETION_BUTTON_TEXT;

    @JsonPath("chat_settings.moderation.message_deletion.button_hover_text")
    public static Component CHAT_MODERATION_MESSAGE_DELETION_BUTTON_HOVER_TEXT;

    // Chat Settings > Discord Webhooks

    @JsonPath("chat_settings.discord_webhooks.enabled")
    public static boolean CHAT_DISCORD_WEBHOOK_ENABLED;

    @JsonPath("chat_settings.discord_webhooks.discord_webhook_url")
    public static String CHAT_DISCORD_WEBHOOK_URL;

}
