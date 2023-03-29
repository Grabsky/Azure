package cloud.grabsky.azure.configuration;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

import static cloud.grabsky.azure.util.Iterables.reversed;

public final class PluginConfig implements JsonConfiguration {

    // Chat Settings

    @JsonPath("chat_settings.cooldown")
    public static Long CHAT_COOLDOWN;

    // Chat Settings > Formats

    @JsonPath("chat_settings.chat_format.default")
    public static String CHAT_FORMATS_DEFAULT;

    @JsonPath("chat_settings.chat_format.console")
    public static String CHAT_FORMATS_CONSOLE;

    @JsonPath("chat_settings.chat_format.extra")
    public static List<FormatHolder> CHAT_FORMATS_EXTRA;

    // Chat Settings > Formats > Allowed Tags

    @JsonPath("chat_settings.chat_format.allowed_tags.default")
    public static TagResolver CHAT_MESSAGE_TAGS_DEFAULT;

    @JsonPath("chat_settings.chat_format.allowed_tags.extra")
    public static List<TagsHolder> CHAT_MESSAGE_TAGS_EXTRA;

    // Chat Settings > Moderation > Message Deletion

    @JsonPath("chat_settings.moderation.message_deletion.enabled")
    public static boolean CHAT_MODERATION_MESSAGE_DELETION_ENABLED;

    @JsonPath("chat_settings.moderation.message_deletion.button")
    public static DeleteButton CHAT_MODERATION_MESSAGE_DELETION_BUTTON;

    // Chat Settings > Discord Webhooks

    @JsonPath("chat_settings.discord_webhooks.enabled")
    public static boolean CHAT_DISCORD_WEBHOOK_ENABLED;

    @JsonPath("chat_settings.discord_webhooks.discord_webhook_url")
    public static String CHAT_DISCORD_WEBHOOK_URL;

    /* ON RELOAD */

    @Override
    public void onReload() {
        ChatManager.CHAT_FORMATS_REVERSED = reversed(PluginConfig.CHAT_FORMATS_EXTRA);
        ChatManager.CHAT_TAGS_REVERSED = reversed(PluginConfig.CHAT_MESSAGE_TAGS_EXTRA);
    }

    /* SURROGATES */

    // Moshi should be able to create instance of the object despite the constructor being private.
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FormatHolder {

        @Getter(AccessLevel.PUBLIC)
        private final String group;

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
    public static final class DeleteButton {

        public enum Position { BEFORE, AFTER }

        @Getter(AccessLevel.PUBLIC)
        private final Position position;

        @Getter(AccessLevel.PUBLIC)
        private final Component text;

        @Getter(AccessLevel.PUBLIC)
        private final Component hover;

    }

}
