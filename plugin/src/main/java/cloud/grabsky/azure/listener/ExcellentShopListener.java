package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import su.nightexpress.nexshop.api.shop.event.AuctionListingCreateEvent;

import java.net.URI;

import org.jetbrains.annotations.NotNull;

import lombok.SneakyThrows;

public final class ExcellentShopListener implements Listener {

    /* DISCORD INTEGRATIONS - FORWARDING AUCTION LISTINGS */

    @SneakyThrows
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onListingCreateEvent(final @NotNull AuctionListingCreateEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_FORMAT)
                .replace("<amount>", String.valueOf(event.getListing().getItemStack().getAmount())
                .replace("<price>", event.getListing().getCurrency().format(event.getListing().getPrice())))
                .replace("<item>", PlainTextComponentSerializer.plainText().serialize(event.getListing().getItemStack().effectiveName()));
        // Creating new instance of WebhookMessageBuilder.
        final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
        // Setting username if specified.
        if (PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
            builder.setDisplayName(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_USERNAME));
        // Setting avatar if specified.
        if (PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
            builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_AVATAR)).toURL());
        // Sending the message.
        builder.sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_URL);
    }

}
