package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import me.grabsky.azure.AzureKeys;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.indigo.utils.Components;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PlayerJoinListener implements Listener {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final PlayerDataManager data;

    public PlayerJoinListener(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.data = instance.getDataManager();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String ip = player.getAddress().getHostString();
        // Updating player's display name
        final PersistentDataContainer container = player.getPersistentDataContainer();
        if (container.has(AzureKeys.CUSTOM_NAME, PersistentDataType.STRING)) {
            player.displayName(Components.parseAmpersand(container.get(AzureKeys.CUSTOM_NAME, PersistentDataType.STRING))); // Won't ever be null
        }
        // Loading existing or creating new data for joined player
        data.createOrLoad(player).thenAcceptAsync((jsonPlayer) -> {
            // Updating IP address if changed
            if (!ip.equals(jsonPlayer.getLastAddress())) {
                jsonPlayer.setLastAddress(ip);
                // Sending API request to get country from player's IP address
                final long s1 = System.nanoTime(); // DEBUG: LATENCY
                jsonPlayer.setCountry(this.fetchCountry("https://get.geojs.io/v1/ip/country/full/" + ip));
                consoleLogger.log("Fetched " + (System.nanoTime() - s1) / 1000000D + "ms"); // DEBUG: LATENCY
            }
            // Updating player's social spy mode
            jsonPlayer.setSocialSpy(container.getOrDefault(AzureKeys.SOCIAL_SPY, PersistentDataType.BYTE, (byte) 0) == (byte) 1);
        });
    }

    private String fetchCountry(final String url) {
        for (int retries = 0; retries < 1; retries++) {
            try {
                // Sending request to an API
                final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                if (reader.ready()) {
                    // Getting country name
                    final String country = reader.readLine();
                    // Closing reader
                    reader.close();
                    // Returning fetched country name or default
                    return (!country.equals("nil")) ? country : "N/A";
                }
            } catch (IOException e) {
                Azure.getInstance().getConsoleLogger().error(ChatColor.RED + "An error occurred while trying to send request to '" + url + "'... retrying...");
            }
        }
        return "N/A";
    }
}
