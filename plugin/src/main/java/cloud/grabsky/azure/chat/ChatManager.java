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
package cloud.grabsky.azure.chat;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginConfig.DeleteButton.Position;
import cloud.grabsky.azure.configuration.PluginConfig.FormatHolder;
import cloud.grabsky.azure.configuration.PluginConfig.TagsHolder;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback.Options;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static java.lang.System.currentTimeMillis;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.event.ClickEvent.callback;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class ChatManager implements Listener, MessageCreateListener {

    private final Azure plugin;

    private final UserManager luckPermsUserManager;
    private final Cache<UUID, SignedMessage.Signature> signatureCache;
    private final Map<UUID, Long> chatCooldowns;
    private final Map<UUID, UUID> lastRecipients;

    // TO-DO: Perform logout on onDisable call.
    private final DiscordApi discord;

    private static final MiniMessage EMPTY_MINIMESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String CHAT_MODERATION_PERMISSION = "azure.plugin.chat.can_delete_messages";
    private static final String CHAT_COOLDOWN_BYPASS_PERMISSION = "azure.plugin.chat.can_bypass_cooldown";

    public static List<FormatHolder> CHAT_FORMATS_REVERSED;
    public static List<TagsHolder> CHAT_TAGS_REVERSED;

    public ChatManager(final Azure plugin) {
        this.plugin = plugin;
        this.luckPermsUserManager = plugin.getLuckPerms().getUserManager();
        this.signatureCache = CacheBuilder.newBuilder()
                .expireAfterWrite(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_CACHE_EXPIRATION_RATE, TimeUnit.MINUTES)
                .build();
        this.chatCooldowns = new HashMap<>();
        this.lastRecipients = new HashMap<>();
        // ...
        this.discord = new DiscordApiBuilder()
                .addIntents(Intent.MESSAGE_CONTENT)
                .setToken(PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_BOT_TOKEN)
                .addListener(this)
                .login().join();
    }

    /**
     * Requests deletion of a message associated with provided {@link UUID} (signatureUUID).
     */
    public boolean deleteMessage(final @NotNull UUID signatureUUID) {
        final @Nullable SignedMessage.Signature signature = signatureCache.getIfPresent(signatureUUID);
        // ...
        if (signature != null) {
            // Deleting the message for the whole server, console *should* be exluded.
            plugin.getServer().deleteMessage(signature);
            // ...
            return true;
        }
        return false;
    }

    @EventHandler @SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
    public void onChatDecorate(final AsyncChatDecorateEvent event) {
        // Skipping cancelled and non-player events
        if (event.isCancelled() == true || event.player() == null)
            return;
        // ...
        final String message = PLAIN_SERIALIZER.serialize(event.originalMessage());
        // ...
        final ItemStack item = event.player().getInventory().getItemInMainHand();
        // Creating result Component using serializers player has access to
        final TagResolver matchingResolvers = this.findSuitableTagsCollection(event.player(), PluginConfig.CHAT_MESSAGE_TAGS_DEFAULT);
        // ...
        final Component result = (matchingResolvers.has("item") == true)
                ? EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers, Placeholder.component("item", empty().color(WHITE).append(item.displayName()).hoverEvent(item.asHoverEvent()) ))
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
        // ...
        final UUID signatureUUID = (event.signedMessage().signature() != null) ? UUID.randomUUID() : null;
        // Caching signatures...
        if (signatureUUID != null) {
            signatureCache.put(signatureUUID, event.signedMessage().signature());
        }
        // Customizing renderer...
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            // Getting the luckperms primary group
            final CachedMetaData metaData = luckPermsUserManager.getUser(source.getUniqueId()).getCachedData().getMetaData();
            // Console...
            if (viewer instanceof ConsoleCommandSender) {
                return MiniMessage.miniMessage().deserialize(
                        PluginConfig.CHAT_FORMATS_CONSOLE,
                        Placeholder.unparsed("signature_uuid", signatureUUID.toString()),
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
                // ...
                final String matchingChatFormat = this.findSuitableChatFormat(source, PluginConfig.CHAT_FORMATS_DEFAULT);
                // ...
                final Component formattedChat = MiniMessage.miniMessage().deserialize(
                        matchingChatFormat,
                        Placeholder.unparsed("player", source.getName()),
                        Placeholder.unparsed("group", requirePresent(metaData.getPrimaryGroup(), "")),
                        Placeholder.parsed("prefix", requirePresent(metaData.getPrefix(), "")),
                        Placeholder.parsed("suffix", requirePresent(metaData.getSuffix(), "")),
                        Placeholder.component("displayname", sourceDisplayName),
                        Placeholder.component("message", event.message())
                );
                // Adding "DELETE MESSAGE" button for allowed viewers
                if (PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_ENABLED == true && receiver.hasPermission(CHAT_MODERATION_PERMISSION) == true && source.hasPermission(CHAT_MODERATION_PERMISSION) == false) {
                    final Component button = PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON.getText()
                            .clickEvent(callback(audience -> this.deleteMessage(signatureUUID), Options.builder().uses(1).lifetime(Duration.ofMinutes(5)).build()))
                            .hoverEvent(showText(PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON.getHover()));
                    // ...
                    return (PluginConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON.getPosition() == Position.BEFORE)
                            ? empty().append(button).appendSpace().append(formattedChat)
                            : empty().append(formattedChat).appendSpace().append(button);
                }
                return formattedChat;
            }
            // Anything else...
            return message;
        });

    }

    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatForward(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        // Forwarding message to webhook...
        if (event.viewers().isEmpty() == false && PluginConfig.CHAT_DISCORD_WEBHOOK_ENABLED == true && PluginConfig.CHAT_DISCORD_WEBHOOK_URL != null) {
            // Serializing Component to plain String.
            final String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            // Getting username and replacing placeholders.
            final String username = PluginConfig.CHAT_DISCORD_WEBHOOK_USERNAME
                    .replace("<player>", player.getName())
                    .replace("<uuid>", player.getUniqueId().toString());
            // Constructing and sending message.
            new WebhookMessageBuilder()
                    .setDisplayName(username)
                    .setDisplayAvatar(new URL("https://minotar.net/armor/bust/" + player.getUniqueId() + "/100.png"))
                    .setContent(plainMessage)
                    .sendSilently(discord, PluginConfig.CHAT_DISCORD_WEBHOOK_URL);
        }
    }

    // TO-DO: Different foromat for console.
    // TO-DO: Support for more placeholders?
    @Override
    public void onMessageCreate(final @NotNull MessageCreateEvent event) {
        if (event.getChannel().getIdAsString().equals(PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_CHANNEL_ID) == true && event.getMessageAuthor().isRegularUser() == true) {
            // Getting the message components.
            final String username = event.getMessageAuthor().getName();
            final String message = event.getReadableMessageContent();
            // Sending the message.
            Message.of(PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_CHAT_FORMAT)
                    .placeholder("username", username)
                    .placeholder("message", message)
                    .broadcast();
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

}
