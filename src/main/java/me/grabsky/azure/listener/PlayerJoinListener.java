package me.grabsky.azure.listener;

import com.google.gson.JsonElement;
import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.azure.util.JsonUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Azure instance;
    private final DataManager data;

    public PlayerJoinListener(Azure instance) {
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        data.create(event.getPlayer()).thenAccept((jsonPlayer) -> {
            // Updating player's IP address
            final String ip = event.getPlayer().getAddress().getHostName();
            jsonPlayer.setLastAddress(ip);
            final long s1 = System.nanoTime(); // DEBUG: LATENCY
            // Sending API request to get country from player's IP address
            final JsonElement json = JsonUtils.sendJsonRequest("https://ipwhois.app/json/" + ip, 1);
            // Checking if API response is correct
            if (json != null && json.getAsJsonObject().get("success").getAsBoolean()) {
                // Updating player's country
                final String country = json.getAsJsonObject().get("country").getAsString();
                jsonPlayer.setCountry((country != null) ? country : "N/A");
                return;
            }
            // Setting player's location to NOT AVAILABLE because API has failed
            jsonPlayer.setCountry("N/A");
            System.out.println("API request took: " + (System.nanoTime() - s1) / 1000000D + "ms"); // DEBUG: LATENCY
        });
    }
}
