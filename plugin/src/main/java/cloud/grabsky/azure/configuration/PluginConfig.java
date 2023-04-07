package cloud.grabsky.azure.configuration;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.listener.CommandFilterListener;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import com.squareup.moshi.Json;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cloud.grabsky.azure.util.Iterables.reversed;
import static java.lang.System.out;

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

    // Commands Settings

    @JsonPath("commands_settings.auto_unregister")
    public static Set<CommandNode> AUTO_UNREGISTER;

    @JsonPath("commands_settings.blacklisted_commands")
    public static Set<CommandNode> BLACKLISTED_COMMANDS;

    // Resource Pack

    @JsonPath("resource_pack.send_on_join")
    public static boolean RESOURCE_PACK_SEND_ON_JOIN;

    @JsonPath("resource_pack.is_required")
    public static boolean RESOURCE_PACK_IS_REQUIRED;

    @JsonPath("resource_pack.pack_url")
    public static String RESOURCE_PACK_URL;

    @JsonPath("resource_pack.pack_hash")
    public static String RESOURCE_PACK_HASH;

    @JsonPath("resource_pack.prompt_message")
    public static Component RESOURCE_PACK_PROMPT_MESSAGE;

    @JsonNullable @JsonPath("resource_pack.notification_sound")
    public static @Nullable Sound RESOURCE_PACK_NOTIFICATION_SOUND;


    /* ON RELOAD */

    @Override
    public void onReload() {
        ChatManager.CHAT_FORMATS_REVERSED = reversed(PluginConfig.CHAT_FORMATS_EXTRA);
        ChatManager.CHAT_TAGS_REVERSED = reversed(PluginConfig.CHAT_MESSAGE_TAGS_EXTRA);
        // ...
        final CommandMap map = Bukkit.getServer().getCommandMap();
        // ...
        out.println("BEFORE: ");
        BLACKLISTED_COMMANDS.forEach(out::println);
        CommandFilterListener.BLACKLISTED_COMMANDS = BLACKLISTED_COMMANDS.stream()
                .<String>mapMulti((it, consumer) -> {
                    if (it.matchReferences == true) {
                        final @Nullable Command command = map.getCommand(it.node);
                        // ...
                        map.getKnownCommands().forEach((key, knownCommand) -> {
                            if (command == knownCommand)
                                consumer.accept(key);
                        });
                    } else consumer.accept(it.node);
                })
                .collect(Collectors.toSet());
        out.println("AFTER: ");
        BLACKLISTED_COMMANDS.forEach(out::println);
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

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CommandNode {

        @Getter(AccessLevel.PUBLIC)
        private final String node;

        @Json(name = "match_references")
        @Getter(AccessLevel.PUBLIC)
        private final Boolean matchReferences;

    }

    // Moshi should be able to create instance of the object despite the constructor being private.
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GroupCommandWhitelist {

        @Getter(AccessLevel.PUBLIC)
        private final String group;

        @Json(name = "whitelisted_commands")
        @Getter(AccessLevel.PUBLIC)
        private final Set<CommandNode> whitelistedCommands;

    }

}
