package me.grabsky.azure.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.webhook.WebhookChatMessage;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// It's done in very inelegant way because either LegacyComponentSerializer or the whole Component API is broken.
// Would love to replace that with proper implementation when everything is fixed.
public class PlayerChatListener implements Listener {
    private final Azure instance;
    private final LuckPerms api;
    private final Map<UUID, Long> cooldowns;

    public PlayerChatListener(Azure instance) {
        this.instance = instance;
        this.api = instance.getLuckPerms();
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled() || event.viewers().isEmpty()) return;
        final Player player = event.getPlayer();
        // Checking for if player is not on cooldown
        if (!cooldowns.containsKey(player.getUniqueId()) || (System.currentTimeMillis() - cooldowns.get(player.getUniqueId())) >= AzureConfig.CHAT_COOLDOWN) {
            if (!player.hasPermission("azure.bypass.chatcooldown")) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
            // "Rendering" chat format
            event.renderer(((source, sourceDisplayName, message, viewer) -> {
                final String chatFormat = AzureConfig.CHAT_FORMATS.getOrDefault(api.getPlayerAdapter(Player.class).getUser(source).getPrimaryGroup(), AzureConfig.CHAT_FORMATS.get("default"));
                final String messageString = (source.hasPermission("azure.chat.format")) ? Components.restoreAmpersand(message).replace("&", "§") : ((TextComponent) message).content();
                return Components.parseSection(chatFormat.replace("{player}", Components.restoreSection(sourceDisplayName)).replace("{message}", messageString));
            }));
            // Sending webhook if enabled
            if (AzureConfig.CHAT_WEBHOOK_ENABLED) {
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    try {
                        new WebhookChatMessage(player.getName(), player.getUniqueId(), Components.restorePlain(event.message())).send();
                    } catch (IOException e) {
                        instance.getConsoleLogger().error("Error occured while trying to send webhook message.");
                        e.printStackTrace();
                    }
                });
            }
            return;
        }
        AzureLang.send(player, AzureLang.CHAT_COOLDOWN);
    }
}
