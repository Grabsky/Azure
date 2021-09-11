package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PlayerJoinListener implements Listener {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final DataManager data;

    public PlayerJoinListener(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.data = instance.getDataManager();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String ip = player.getAddress().getHostName();
        data.createOrLoad(player).thenAcceptAsync((jsonPlayer) -> {
            // Updating IP address if changed
            if (!ip.equals(jsonPlayer.getLastAddress())) {
                jsonPlayer.setLastAddress(ip);
                // Sending API request to get country from player's IP address
                final long s1 = System.nanoTime(); // DEBUG: LATENCY
                jsonPlayer.setCountry(this.fetchCountry("https://get.geojs.io/v1/ip/country/full/" + ip));
                consoleLogger.log("Fetched " + (System.nanoTime() - s1) / 1000000D + "ms"); // DEBUG: LATENCY
            }
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
