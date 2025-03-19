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
package cloud.grabsky.azure.chat;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginConfig.DeleteButton.Position;
import cloud.grabsky.azure.configuration.PluginConfig.FormatHolder;
import cloud.grabsky.azure.configuration.PluginConfig.TagsHolder;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.ComponentBuilder;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.Conditions;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.fellbaum.jemoji.EmojiManager;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback.Options;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.SneakyThrows;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;
import static java.lang.System.currentTimeMillis;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.event.ClickEvent.callback;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class ChatManager implements Listener, MessageCreateListener {

    private final Azure plugin;

    private final UserManager luckPermsUserManager;
    private final Cache<UUID, SignedMessage.Signature> signatureCache;
    private final Map<UUID, Long> chatCooldowns;
    private final Map<UUID, UUID> lastRecipients;

    // Contains list of inappropriate words that are not allowed in chat.
    private Set<String> inappropriateWords;

    private static final MiniMessage EMPTY_MINIMESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String CHAT_MODERATION_PERMISSION = "azure.plugin.chat.can_delete_messages";
    private static final String CHAT_COOLDOWN_BYPASS_PERMISSION = "azure.plugin.chat.can_bypass_cooldown";

    public static List<FormatHolder> CHAT_FORMATS_REVERSED;
    public static List<TagsHolder> CHAT_TAGS_REVERSED;
    public static List<Component> AUTOMATED_MESSAGES_SHUFFLED;

    private static @Nullable ListIterator<Component> AUTOMATED_MESSAGES_ITERATOR;

    // Holds reference to a task responsible for sending automated chat messages.
    // It can be re-assigned from a static context. (PluginConfig#onReload)
    private static BukkitTask AUTOMATED_MESSAGES_TASK;

    // Raw type of Set<String> used for deserialization with Moshi.
    private static final Type LIST_STRING_TYPE = Types.newParameterizedType(Set.class, String.class);

    // Strict MiniMessage instance used for deserialization of translatable components and colors appended internally in Discord -> Minecraft chat forwarding.
    private static final MiniMessage STRICT_MINI_MESSAGE = MiniMessage.builder().tags(TagResolver.resolver(StandardTags.translatable(), StandardTags.color())).build();

    // Slightly less complex REGEX which (1) does not take backslashes into account (2) possibly breaks other edge-cases.
    private static final Pattern MENTION_REGEX = Pattern.compile("(<@!?(\\d+)>|<@&(\\d+)>|<#(\\d+)>|<a?:(\\w+):(\\d+)>)");

    private static final AllowedMentions NO_MENTIONS = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionRepliedUser(false)
            .setMentionUsers(false)
            .setMentionRoles(false)
            .build();

    public ChatManager(final Azure plugin) {
        this.plugin = plugin;
        this.luckPermsUserManager = plugin.getLuckPerms().getUserManager();
        this.signatureCache = CacheBuilder.newBuilder()
                .expireAfterWrite(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_CACHE_EXPIRATION_RATE, TimeUnit.MINUTES)
                .build();
        this.chatCooldowns = new HashMap<>();
        this.lastRecipients = new HashMap<>();
        this.inappropriateWords = new HashSet<>();

    }

    /**
     * Cancels previous, and schedules new automated messages task.
     */
    // NOTE: Perhaps in the future this (and related components) could be re-written to not be static.
    public static void scheduleAutomatedMessagesTask() {
        // Cancelling the task if already running.
        if (AUTOMATED_MESSAGES_TASK != null)
            AUTOMATED_MESSAGES_TASK.cancel();
        // "Invalidating" current iterator to make it auto-update during the first task iteration.
        AUTOMATED_MESSAGES_ITERATOR = null;
        // Returning in case feature is disabled or messages list is empty.
        if (PluginConfig.CHAT_AUTOMATED_MESSAGES_ENABLED == false || PluginConfig.CHAT_AUTOMATED_MESSAGES_CONTENTS.isEmpty() == true)
            return;
        // Scheduling the task.
        AUTOMATED_MESSAGES_TASK = Azure.getInstance().getBedrockScheduler().repeat(PluginConfig.CHAT_AUTOMATED_MESSAGES_INTERVAL * 20, PluginConfig.CHAT_AUTOMATED_MESSAGES_INTERVAL * 20, Long.MAX_VALUE, (_) -> {
            // Resetting iterator in case it reached the end.
            if (AUTOMATED_MESSAGES_ITERATOR == null || AUTOMATED_MESSAGES_ITERATOR.hasNext() == false) {
                AUTOMATED_MESSAGES_ITERATOR = AUTOMATED_MESSAGES_SHUFFLED.listIterator();
            }
            // Getting the next message.
            final Component message = AUTOMATED_MESSAGES_ITERATOR.next();
            // Sending message to all online players. Not using Server#sendMessage because this will make it appear in the console.
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            // Playing sound, if configured.
            if (PluginConfig.CHAT_AUTOMATED_MESSAGES_SOUND != null)
                Bukkit.getServer().playSound(PluginConfig.CHAT_AUTOMATED_MESSAGES_SOUND);
            // ...
            return true;
        });
    }

    /**
     * Loads list of inappropriate words.
     */
    @SuppressWarnings("unchecked")
    public void loadInappropriateWords() {
        final File file = new File(plugin.getDataFolder(), "inappropriate_words.json");
        try {
            // Ensuring the file exists.
            ensureResourceExistence(plugin, file);
            // Creating a JsonReader from provided file.
            final JsonReader reader = JsonReader.of(buffer(source(file)));
            // Reading the JSON file.
            final Set<String> set = (Set<String>) new Moshi.Builder().build().adapter(LIST_STRING_TYPE).fromJson(reader);
            // Closing the reader.
            reader.close();
            // Throwing exception in case list ended up being null. Unlikely to happen, but possible.
            if (set == null)
                throw new IllegalStateException("Deserialization of " + file.getPath() + " failed: " + null);
            // Updating the filtered words list.
            inappropriateWords = Collections.unmodifiableSet(set);
        } catch (final IllegalStateException | IOException e) {
            plugin.getLogger().severe("Reloading of '" + file.getName() + "' failed due to following error(s):");
            plugin.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null)
                plugin.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
        }
    }

    /**
     * Requests deletion of a message associated with provided {@link UUID} (signatureUUID).
     */
    public boolean deleteMessage(final @NotNull UUID signatureUUID) {
        final @Nullable SignedMessage.Signature signature = signatureCache.getIfPresent(signatureUUID);
        // Returning 'false' if signature for this message is not set.
        if (signature == null)
            return false;
        // Requesting deletion of this message.
        plugin.getServer().deleteMessage(signature);
        // Returning 'true' as message deletion has been requested.
        return true;
    }

    @EventHandler @SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
    public void onChatDecorate(final AsyncChatDecorateEvent event) {
        // Skipping cancelled and non-player events
        if (event.isCancelled() == true || event.player() == null)
            return;
        // ...
        String message = PLAIN_SERIALIZER.serialize(event.originalMessage());
        // ...
        final ItemStack item = event.player().getInventory().getItemInMainHand();
        // Creating result Component using serializers player has access to
        final TagResolver matchingResolvers = this.findSuitableTagsCollection(event.player(), PluginConfig.CHAT_MESSAGE_TAGS_DEFAULT);
        // Replacing all occurrences of <i>, [item] and [i] with <item>.
        message = message.replace("<i>", "<item>").replace("[item]", "<item>").replace("[i]", "<item>");
        // Preparing the result component.
        final Component result = (matchingResolvers.has("item") == true)
                ? (item.isEmpty() == false && item.getType() != Material.AIR)
                        ? EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers,
                                Placeholder.component("item",
                                        Component.text().color(getEffectiveColor(item))
                                                        .append(Component.text("["))
                                                        .append(item.getAmount() > 1 ? Component.text(item.getAmount() + "x ") : ComponentBuilder.EMPTY)
                                                        .append(item.effectiveName())
                                                        .append(Component.text("]"))
                                                        .hoverEvent(item.asHoverEvent())
                                                        .build()
                                ))
                        : EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers,
                                Placeholder.component("item",
                                        Component.text().color(WHITE)
                                                        .append(Component.text("["))
                                                        .append(Component.translatable("block.minecraft.air"))
                                                        .append(Component.text("]"))
                                                        .build()
                                ))
                : EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers);
        // Setting result, the rest is handled within AsyncChatEvent
        event.result(result);
    }

    @EventHandler @SuppressWarnings("DataFlowIssue")
    public void onChat(final AsyncChatEvent event) {
        // Cancelled events are not handled
        if (event.isCancelled() == true)
            return;
        // ...
        final Player player = event.getPlayer();
        final User user = plugin.getUserCache().getUser(player);
        // Mute handling...
        if (user.isMuted() == true) {
            // Cancelling the event, so message won't go through.
            event.setCancelled(true);
            // Getting the current mute punishment.
            final Punishment punishment = user.getMostRecentMute();
            // Preparing the message.
            final Component message = (punishment.isPermanent() == true)
                    ? PluginLocale.CHAT_MUTED_PERMANENT
                    : Message.of(PluginLocale.CHAT_MUTED)
                            .placeholder("duration_left", punishment.getDurationLeft().toString())
                            .parse();
            // Sending mute information to the player.
            Message.of(message).send(player);
            // Exiting the code block.
            return;
        }
        // Cooldown handling... if enabled and player does not have bypass permission.
        if (PluginConfig.CHAT_COOLDOWN > 0 && player.hasPermission(CHAT_COOLDOWN_BYPASS_PERMISSION) == false) {
            if (Interval.between(currentTimeMillis(), chatCooldowns.getOrDefault(player.getUniqueId(), 0L), Unit.MILLISECONDS).as(Unit.MILLISECONDS) < PluginConfig.CHAT_COOLDOWN) {
                event.setCancelled(true);
                Message.of(PluginLocale.CHAT_ON_COOLDOWN).send(player);
                return;
            }
            // ...setting cooldown
            chatCooldowns.put(player.getUniqueId(), currentTimeMillis());
        }
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        // Cancelling messages containing invalid characters. Mostly to ensure players are not using resource-pack-reserved characters in chat. (E000-F8FF)
        if (PluginConfig.CHAT_FILTERING_DISALLOW_INVALID_CHARACTERS == true && message.chars().anyMatch(it -> Conditions.inRange(it, 57344, 63743) == true) == true) {
            event.setCancelled(true);
            Message.of(PluginLocale.CHAT_MESSAGE_CONTAINS_INVALID_CHARACTERS).send(player);
            return;
        }
        // Cancelling messages containing inappropriate words.
        if (PluginConfig.CHAT_FILTERING_DISALLOW_INAPPROPRIATE_WORDS == true && inappropriateWords != null && inappropriateWords.isEmpty() == false) {
            final String[] words = message.split(" ");
            // Iterating over all words in a message.
            for (final String word : words) {
                // Cancelling message if the current word is in the list of inappropriate words.
                if (inappropriateWords.contains(word) == true) {
                    event.setCancelled(true);
                    Message.of(PluginLocale.CHAT_MESSAGE_CONTAINS_INAPPROPRIATE_WORDS).send(player);
                    // Executing punishment commands.
                    if (PluginConfig.CHAT_FILTERING_PUNISHMENT_COMMANDS.isEmpty() == false)
                        plugin.getBedrockScheduler().run(1L, (_) -> {
                            PluginConfig.CHAT_FILTERING_PUNISHMENT_COMMANDS.forEach(it -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("<player>", player.getName())));
                        });
                    // Returning...
                    return;
                }
            }
        }
        // ...
        final UUID signatureUUID = (event.signedMessage().signature() != null) ? UUID.randomUUID() : null;
        // Caching signatures...
        if (signatureUUID != null) {
            signatureCache.put(signatureUUID, event.signedMessage().signature());
        }
        // Customizing renderer...
        event.renderer((source, sourceDisplayName, msg, viewer) -> {
            // Getting the luckperms primary group
            final CachedMetaData metaData = luckPermsUserManager.getUser(source.getUniqueId()).getCachedData().getMetaData();
            // Console...
            if (viewer instanceof ConsoleCommandSender) {
                return MiniMessage.miniMessage().deserialize(
                        PlaceholderAPI.setPlaceholders(player, PluginConfig.CHAT_FORMATS_CONSOLE),
                        Placeholder.unparsed("signature_uuid", (signatureUUID != null) ? signatureUUID.toString() : "N/A"),
                        Placeholder.unparsed("player", source.getName()),
                        Placeholder.unparsed("group", requirePresent(metaData.getPrimaryGroup(), "")),
                        Placeholder.parsed("prefix", requirePresent(metaData.getPrefix(), "")),
                        Placeholder.parsed("suffix", requirePresent(metaData.getSuffix(), "")),
                        Placeholder.component("displayname", sourceDisplayName),
                        Placeholder.component("message", event.message())
                );
            }
            // Player...
            if (viewer instanceof Player receiver) {
                final String matchingChatFormat = this.findSuitableChatFormat(source, PluginConfig.CHAT_FORMATS_DEFAULT);
                // ...
                final Component formattedChat = MiniMessage.miniMessage().deserialize(
                        PlaceholderAPI.setPlaceholders(player, matchingChatFormat),
                        Placeholder.unparsed("player", source.getName()),
                        Placeholder.unparsed("group", requirePresent(metaData.getPrimaryGroup(), "")),
                        Placeholder.parsed("prefix", requirePresent(metaData.getPrefix(), "")),
                        Placeholder.parsed("suffix", requirePresent(metaData.getSuffix(), "")),
                        Placeholder.component("displayname", sourceDisplayName),
                        Placeholder.component("message", event.message())
                );
                // Adding "DELETE MESSAGE" button for allowed viewers
                if (PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_ENABLED == true && receiver.hasPermission(CHAT_MODERATION_PERMISSION) == true) {
                    final PluginConfig.DeleteButton buttonConfig = (source.hasPermission(CHAT_MODERATION_PERMISSION) == false)
                            ? PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_ACTIVE
                            : PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_INACTIVE;
                    // Creating the button component.
                    final Component button = (source.hasPermission(CHAT_MODERATION_PERMISSION) == false)
                            // Creating button with delete option. (Moderator seeing non-moderator's message)
                            ? buttonConfig.getText()
                                    .clickEvent(callback((_) -> this.deleteMessage(signatureUUID), Options.builder().uses(1).lifetime(Duration.ofMinutes(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_CACHE_EXPIRATION_RATE)).build()))
                                    .hoverEvent(showText(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_ACTIVE.getHover()))
                            // Creating button without delete option. (Moderator seeing self or other moderator's message)
                            : buttonConfig.getText().hoverEvent(showText(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_INACTIVE.getHover()));
                    // Appending the button to the chat format.
                    return (buttonConfig.getPosition() == Position.BEFORE)
                            ? empty().append(button).append(formattedChat)
                            : empty().append(formattedChat).append(button);
                }
                // Playing sound if message mentions player name.
                if (PluginConfig.CHAT_MENTION_SOUND != null && message.contains(player.getName()) == true)
                    player.playSound(PluginConfig.CHAT_MENTION_SOUND);
                // Returning the formatted chat.
                return formattedChat;
            }
            // Anything else...
            return msg;
        });

    }

    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatForward(final AsyncChatEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        if (event.viewers().isEmpty() == false) {
            // Serializing Component to plain String.
            final String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder()
                    .setAllowedMentions(NO_MENTIONS)
                    .setContent(plainMessage.startsWith("xaero-waypoint:") == true ? "[Xaero's Waypoint]" : plainMessage);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_AVATAR)).toURL());
            // Sending the message.
            builder.sendSilently(plugin.getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_URL);
        }
    }

    @Override @SuppressWarnings("deprecation") // Suppressing @Deprecated method(s) as adventure seems to not provide an alternative for that.
    public void onMessageCreate(final @NotNull MessageCreateEvent event) {
        // Skipping in case chat returning is not enabled.
        if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED == false)
            return;
        // Skipping irrelevant channels and bot replies.
        if (event.getChannel().getIdAsString().equals(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHANNEL_ID) == true && event.getMessageAuthor().isRegularUser() == true) {
            // Getting the message components.
            final String username = event.getMessageAuthor().getName();
            final String displayname = event.getMessageAuthor().getDisplayName();
            // Getting the message and stripping all tags and formatting. Not final because it's modified in the next step.
            String message = replaceMentions(event.getMessageContent(), event.getServer().get(), true);
            // Replacing all emojis in this message with a translatable component.
            message = EmojiManager.replaceAllEmojis(message, (emoji) -> "<white><lang:'" + emoji.getDiscordAliases().getFirst() + "'></white>");
            // Appending '(Attachment)' or similar string to the message if it contains an attachment like image etc.
            message = (event.getMessage().getAttachments().isEmpty() == false)
                    ? (message.isBlank() == false)
                            ? message + " " + PluginLocale.CHAT_ATTACHMENT
                            : PluginLocale.CHAT_ATTACHMENT
                    : message;
            // Appending '(Re: User)' in front of the message if it's a reply.
            message = (event.getMessage().getReferencedMessage().isPresent() == true)
                    ? "<#848484>(Re: <#A89468>" + event.getMessage().getReferencedMessage().map(ref -> ref.getAuthor().getName()).orElse("N/A") + "</#A89468>)</#848484> " + message
                    : message;
            // Parsing the message using MiniMessage, with just the translatable and color tags enabled.
            final Component finalMessage = STRICT_MINI_MESSAGE.deserialize(message);
            // Sending message to the console.
            Message.of(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CONSOLE_FORMAT)
                    .placeholder("username", username)
                    .placeholder("displayname", displayname)
                    .placeholder("message", finalMessage)
                    .send(Bukkit.getConsoleSender());
            // Sending message to all players.
            Message.of(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHAT_FORMAT)
                    .placeholder("username", username)
                    .placeholder("displayname", displayname)
                    .placeholder("message", finalMessage)
                    .send(Bukkit.getServer().filterAudience(audience -> audience instanceof Player));
        }

    }

    public void setLastRecipients(final @NotNull UUID first, final @NotNull UUID second) {
        this.lastRecipients.put(first, second);
        this.lastRecipients.put(second, first);
    }

    public @Nullable UUID getLastRecipient(final @NotNull UUID uniqueId) {
        return lastRecipients.get(uniqueId);
    }

    private @NotNull TagResolver findSuitableTagsCollection(final @NotNull Player player, final @NotNull TagResolver def) {
        return CHAT_TAGS_REVERSED.stream()
                .filter(holder -> player.hasPermission(holder.getPermission()) == true)
                .map(TagsHolder::getTags)
                .findFirst()
                .orElse(def);
    }

    private @NotNull String findSuitableChatFormat(final @NotNull Player player, final @NotNull String def) {
        return CHAT_FORMATS_REVERSED.stream()
                .filter(holder -> player.hasPermission(holder.getPermission()) == true)
                .map(FormatHolder::getFormat)
                .findFirst()
                .orElse(def);
    }

    /* UTILITY METHODS */

    @SuppressWarnings("UnstableApiUsage")
    private static TextColor getEffectiveColor(final @NotNull ItemStack item) {
        // Item name color has the highest priority.
        if (item.getItemMeta().itemName().color() != null)
            return item.getItemMeta().itemName().color();
        // Enchantment color is the next, but only when the rarity is not epic.
        else if (item.getItemMeta().hasEnchants() == true && item.getData(DataComponentTypes.RARITY) != ItemRarity.EPIC)
            return NamedTextColor.AQUA;
        // Rarity color is the last color to check.
        else if (item.hasData(DataComponentTypes.RARITY) == true)
            return item.getData(DataComponentTypes.RARITY).color();
        // No color was found, so it should default to WHITE.
        else return NamedTextColor.WHITE;
    }

    // Less complex and more performant alternative to Message#getReadableContent.
    private static String replaceMentions(String text, final Server server, boolean stripTags) {
        // Stripping legacy chat colors and tags if specified.
        if (stripTags == true)
            text = MiniMessage.miniMessage().stripTags(ChatColor.stripColor(text));
        // Creating new instance of Matcher from compiled pattern (above) and specified text.
        final Matcher matcher = MENTION_REGEX.matcher(text);
        // Preparing a result StringBuilder which will be filled by the code below.
        final StringBuilder result = new StringBuilder();
        // Matching...
        while (matcher.find() == true) {
            // Matching all possible mentions using groups.
            final String userId = matcher.group(2);
            final String roleId = matcher.group(3);
            final String channelId = matcher.group(4);
            final String emojiName = matcher.group(5);
            final String emojiId = matcher.group(6);
            // Creating replacement variable which defaults to the original text.
            String replacement = matcher.group(0);
            // User ID '<@1234567890>'
            if (userId != null)
                replacement = "<#A89468>@" + server.getApi().getCachedUserById(userId).map(org.javacord.api.entity.user.User::getName).orElse("invalid-user") + "</#A89468>";
                // Role ID '<@&123456780>'
            else if (roleId != null)
                replacement = "<#A89468>@" + server.getApi().getRoleById(roleId).map(Role::getName).orElse("invalid-role") + "</#A89468>";
                // Channel ID '<#1234567890>'
            else if (channelId != null)
                replacement =  "<#A89468>#" + server.getApi().getServerChannelById(channelId).map(ServerChannel::getName).orElse("invalid-channel") + "</#A89468>";
                // Emoji name '<:[NAME]:[ID]>'
            else if (emojiName != null)
                replacement = ":" + emojiName + ":";
            // If the replacement is null, keep the original mention.
            if (replacement == null)
                replacement = matcher.group(0);
            // Appending replacement (or original string) to the result.
            matcher.appendReplacement(result, replacement);
        }
        // Appending the rest of the matcher to the result.
        matcher.appendTail(result);
        // Building and returning the result, with all mentions replaced.
        return result.toString();
    }

}
