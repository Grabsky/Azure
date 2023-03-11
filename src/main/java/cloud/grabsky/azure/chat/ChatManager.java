package cloud.grabsky.azure.chat;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public final class ChatManager implements Listener {

    public static final Duration SIGNATURE_EXPIRATION_RATE = Duration.of(5, ChronoUnit.MINUTES);

    private final Cache<UUID, SignedMessage.Signature> signatureCache = CacheBuilder.newBuilder()
            .expireAfterWrite(SIGNATURE_EXPIRATION_RATE)
            .build();

    public boolean deleteMessage(final Audience audience, final UUID uuid) {
        final SignedMessage.Signature signature = signatureCache.getIfPresent(uuid);
        // ...
        if (signature != null) {
            audience.deleteMessage(signature);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onChatDecorate(final AsyncChatDecorateEvent event) {
        event.result(MiniMessage.miniMessage().deserialize(
                "<player>: <message>",
                Placeholder.component("player", event.player().displayName()),
                Placeholder.component("message", event.originalMessage().color(NamedTextColor.GRAY))
        ));
    }

    @EventHandler
    public void onChat(final AsyncChatEvent event) {
        if (event.isCancelled() == false && event.signedMessage().signature() != null) {
            final UUID messageUUID = UUID.randomUUID();
            // ...
            signatureCache.put(messageUUID, event.signedMessage().signature());
            // ...
            event.renderer((source, sourceDisplayName, message, viewer) -> {
                final ClickEvent onClick = ClickEvent.runCommand("/delete " + source.getName() + " " + messageUUID);
                final HoverEvent<Component> onHover = HoverEvent.showText(text("Click to delete."));
                // ...
                if (viewer instanceof Player receiver && receiver.hasPermission("can.delete") == true)
                    return text()
                            .append(text("×", NamedTextColor.GRAY).clickEvent(onClick).hoverEvent(onHover))
                            .appendSpace()
                            .append(message)
                            .build();
                else return message;
            });
        }
    }

}
