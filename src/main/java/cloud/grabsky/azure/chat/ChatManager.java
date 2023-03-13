package cloud.grabsky.azure.chat;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.chat.holder.FormatHolder;
import cloud.grabsky.azure.chat.holder.TagsHolder;
import cloud.grabsky.azure.configuration.AzureConfig;
import cloud.grabsky.azure.configuration.AzureLocale;
import cloud.grabsky.bedrock.BedrockScheduler;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static java.lang.System.currentTimeMillis;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class ChatManager implements Listener {

    private final BedrockScheduler scheduler;
    private final UserManager luckPermsUserManager;
    private final Cache<UUID, SignedMessage.Signature> signatureCache;
    private final Map<UUID, Long> chatCooldowns;

    private static final MiniMessage EMPTY_MINIMESSAGE = MiniMessage.builder().tags(TagResolver.empty()).build();

    private static final String CHAT_MODERATION_PERMISSION = "azure.plugin.chat.can_delete_messages";
    private static final String CHAT_COOLDOWN_BYPASS_PERMISSION = "azure.plugin.chat.can_bypass_cooldown";

    public ChatManager(final Azure azure) {
        this.scheduler = azure.getBedrockScheduler();
        this.luckPermsUserManager = azure.getLuckPerms().getUserManager();
        this.signatureCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        this.chatCooldowns = new HashMap<>();
    }

    /**
     * Requests deletion of a message associated with provided {@link UUID} (signatureUUID).
     */
    public boolean deleteMessage(final Audience audience, final UUID signatureUUID) {
        final SignedMessage.Signature signature = signatureCache.getIfPresent(signatureUUID);
        // ...
        if (signature != null) {
            audience.deleteMessage(signature);
            return true;
        }
        return false;
    }

    @EventHandler @SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
    public void onChatDecorate(final AsyncChatDecorateEvent event) {
        // Skipping cancelled and non-player events
        if (event.isCancelled() == true || event.player() == null) {
            return;
        }
        final String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        // ...
        final ItemStack item = event.player().getInventory().getItemInMainHand();
        // ...
        final Component itemComponent = empty().color(WHITE).append(item.displayName()).hoverEvent(item.asHoverEvent());
        // Creating result Component using serializers player has access to
        final TagResolver matchingResolvers = AzureConfig.CHAT_MESSAGE_TAGS_EXTRA
                .stream()
                .filter(holder -> event.player().hasPermission(holder.getPermission()) == true)
                .map(TagsHolder::getTags)
                .findFirst()
                .orElse(AzureConfig.CHAT_MESSAGE_TAGS_DEFAULT);
        // ...
        final Component result = EMPTY_MINIMESSAGE.deserialize(message, matchingResolvers, Placeholder.component("item", itemComponent));
        // Setting result, the rest is handled within AsyncChatEvent
        event.result(result);
    }

    @EventHandler @SuppressWarnings("DataFlowIssue")
    public void onChat(final AsyncChatEvent event) {
        if (event.isCancelled() == false && event.signedMessage().signature() != null) {
            final UUID sourceUUID = event.getPlayer().getUniqueId();
            // Cooldown handling... if enabled and player does not have bypass permission
            if (AzureConfig.CHAT_COOLDOWN > 0 && event.getPlayer().hasPermission(CHAT_COOLDOWN_BYPASS_PERMISSION) == false) {
                if (Interval.between(currentTimeMillis(), chatCooldowns.getOrDefault(sourceUUID, 0L), Unit.MILLISECONDS).as(Unit.MILLISECONDS) < AzureConfig.CHAT_COOLDOWN) {
                    event.setCancelled(true);
                    sendMessage(event.getPlayer(), AzureLocale.CHAT_ON_COOLDOWN);
                    return;
                }
                // ...setting cooldown
                chatCooldowns.put(sourceUUID, currentTimeMillis());
            }
            final UUID signatureUUID = UUID.randomUUID();
            // ...
            signatureCache.put(signatureUUID, event.signedMessage().signature());
            // ...
            event.renderer((source, sourceDisplayName, message, viewer) -> {
                // Getting the luckperms primary group
                // TO-DO: Multi-group support
                final User user = luckPermsUserManager.getUser(sourceUUID);
                final String userPrimaryGroup = user.getPrimaryGroup();
                // Console...
                if (viewer instanceof ConsoleCommandSender) return MiniMessage.miniMessage().deserialize(
                        AzureConfig.CHAT_FORMATS_CONSOLE,
                        Placeholder.unparsed("signature_uuid", signatureUUID.toString()),
                        Placeholder.unparsed("group", userPrimaryGroup),
                        Placeholder.unparsed("player", event.getPlayer().getName()),
                        Placeholder.component("message", event.message())
                );
                // Player...
                if (viewer instanceof Player receiver) {
                    // ...
                    final String matchingChatFormat = AzureConfig.CHAT_FORMATS_EXTRA
                            .stream()
                            .filter(holder -> holder.getGroup().equals(userPrimaryGroup) == true)
                            .map(FormatHolder::getFormat)
                            .findFirst()
                            .orElse(AzureConfig.CHAT_FORMATS_DEFAULT);
                    // ...
                    final Component formattedChat = MiniMessage.miniMessage().deserialize(
                            matchingChatFormat,
                            Placeholder.unparsed("group", userPrimaryGroup),
                            Placeholder.unparsed("player", source.getName()),
                            Placeholder.component("message", event.message())
                    );
                    // Adding "DELETE MESSAGE" button for allowed viewers
                    if (AzureConfig.CHAT_MODERATION_MESSAGE_DELETION_ENABLED == true && receiver.hasPermission(CHAT_MODERATION_PERMISSION) == true /* && source.hasPermission(CHAT_MODERATION_PERMISSION) == false */) {
                        final ClickEvent onClick = ClickEvent.runCommand("/delete " + source.getName() + " " + signatureUUID);
                        final HoverEvent<Component> onHover = HoverEvent.showText(AzureConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_HOVER_TEXT);
                        // ...
                        return empty().append(AzureConfig.CHAT_MODERATION_MESSAGE_DELETION_BUTTON_TEXT.clickEvent(onClick).hoverEvent(onHover).appendSpace()).append(formattedChat);
                    }
                    return formattedChat;
                }
                // Anything else...
                return message;
            });
        }
        // Forwarding a webhook...
        if (AzureConfig.CHAT_DISCORD_WEBHOOK_ENABLED == true) {
            // TO-DO: ...
        }
    }

}
