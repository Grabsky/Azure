package cloud.grabsky.azure.integrations;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import su.nightexpress.nexshop.api.shop.event.AuctionListingCreateEvent;

import org.jetbrains.annotations.NotNull;

import lombok.SneakyThrows;

public enum ExcellentShopIntegration implements Listener {
    INSTANCE; // SINGLETON

    private static boolean IS_INITIALIZED = false;

    public static boolean initialize(final @NotNull Azure plugin) {
        if (IS_INITIALIZED == false && plugin.getServer().getPluginManager().getPlugin("ExcellentShop") != null) {
            plugin.getServer().getPluginManager().registerEvents(ExcellentShopIntegration.INSTANCE, plugin);
            // Marking the integration as initialized.
            IS_INITIALIZED = true;
            // Returning true if integration was successfully initialized.
            return true;
        }
        // Logging warning and returning false if integration could not be initialized.
        plugin.getLogger().warning("ExcellentShop integration could not be initialized. (DEPENDENCY_NOT_ENABLED)");
        return false;
    }

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
            builder.setUsername(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_USERNAME));
        // Setting avatar if specified.
        if (PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
            builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_AVATAR));
        // Sending the message.
        Azure.getInstance().getDiscordIntegration().getWebhookForwardingAuctionHouseListings().send(builder.build());
    }

}
