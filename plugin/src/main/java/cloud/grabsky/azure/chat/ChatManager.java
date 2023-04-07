package cloud.grabsky.azure.chat;

import cloud.grabsky.azure.Azure;
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
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static java.lang.System.currentTimeMillis;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.event.ClickEvent.callback;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class ChatManager implements Listener {

    private final UserManager luckPermsUserManager;
    private final Cache<UUID, SignedMessage.Signature> signatureCache;
    private final Map<UUID, Long> chatCooldowns;

    private static final MiniMessage EMPTY_MINIMESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String CHAT_MODERATION_PERMISSION = "azure.plugin.chat.can_delete_messages";
    private static final String CHAT_COOLDOWN_BYPASS_PERMISSION = "azure.plugin.chat.can_bypass_cooldown";

    public static List<FormatHolder> CHAT_FORMATS_REVERSED;
    public static List<TagsHolder> CHAT_TAGS_REVERSED;

    public ChatManager(final Azure azure) {
        this.luckPermsUserManager = azure.getLuckPerms().getUserManager();
        this.signatureCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
        this.chatCooldowns = new HashMap<>();
    }

    /**
     * Requests deletion of a message associated with provided {@link UUID} (signatureUUID).
     */
    public boolean deleteMessage(final UUID signatureUUID) {
        final SignedMessage.Signature signature = signatureCache.getIfPresent(signatureUUID);
        // ...
        if (signature != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.deleteMessage(signature));
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
        // ...
        final Component itemComponent = empty().color(WHITE).append(item.displayName()).hoverEvent(item.asHoverEvent());
        // Creating result Component using serializers player has access to
        final TagResolver matchingResolvers = this.findSuitableTagsCollection(event.player(), PluginConfig.CHAT_MESSAGE_TAGS_DEFAULT);
        // ...
        final Component result = EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers, Placeholder.component("item", itemComponent));
        // Setting result, the rest is handled within AsyncChatEvent
        event.result(result);
    }

    @EventHandler @SuppressWarnings("DataFlowIssue")
    public void onChat(final AsyncChatEvent event) {
        // Cancelled events are not handled
        if (event.isCancelled() == true)
            return;
        // Cooldown handling... if enabled and player does not have bypass permission
        if (PluginConfig.CHAT_COOLDOWN > 0 && event.getPlayer().hasPermission(CHAT_COOLDOWN_BYPASS_PERMISSION) == false) {
            if (Interval.between(currentTimeMillis(), chatCooldowns.getOrDefault(event.getPlayer().getUniqueId(), 0L), Unit.MILLISECONDS).as(Unit.MILLISECONDS) < PluginConfig.CHAT_COOLDOWN) {
                event.setCancelled(true);
                Message.of(PluginLocale.CHAT_ON_COOLDOWN).send(event.getPlayer());
                return;
            }
            // ...setting cooldown
            chatCooldowns.put(event.getPlayer().getUniqueId(), currentTimeMillis());
        }
        // ...
        final UUID signatureUUID = (event.signedMessage().signature() != null) ? UUID.randomUUID() : null;
        // ...
        if (signatureUUID != null) {
            signatureCache.put(signatureUUID, event.signedMessage().signature());
        }
        // Customizing renderer...
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            // Getting the luckperms primary group
            final CachedMetaData user = luckPermsUserManager.getUser(source.getUniqueId()).getCachedData().getMetaData();
            // Console...
            if (viewer instanceof ConsoleCommandSender) {
                return MiniMessage.miniMessage().deserialize(
                        PluginConfig.CHAT_FORMATS_CONSOLE,
                        Placeholder.unparsed("signature_uuid", signatureUUID.toString()),
                        Placeholder.unparsed("player", source.getName()),
                        Placeholder.unparsed("group", requirePresent(user.getPrimaryGroup(), "")),
                        Placeholder.parsed("prefix", requirePresent(user.getPrefix(), "")),
                        Placeholder.parsed("suffix", requirePresent(user.getSuffix(), "")),
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
                        Placeholder.unparsed("group", requirePresent(user.getPrimaryGroup(), "")),
                        Placeholder.parsed("prefix", requirePresent(user.getPrefix(), "")),
                        Placeholder.parsed("suffix", requirePresent(user.getSuffix(), "")),
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
        // Forwarding a webhook...
        if (PluginConfig.CHAT_DISCORD_WEBHOOK_ENABLED == true) {
            // TO-DO: ...
        }
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
                .filter(holder -> player.hasPermission("group." + holder.getGroup()) == true)
                .map(FormatHolder::getFormat)
                .findFirst()
                .orElse(def);
    }

}
