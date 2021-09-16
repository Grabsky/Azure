package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final Azure instance;
    private final PlayerDataManager data;

    public PlayerQuitListener(Azure instance) {
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final JsonPlayer jsonPlayer = data.getOnlineData(player);
        // Updating last known location and marking data as pending expiration
        jsonPlayer.setLastLocation(player.getLocation());
        jsonPlayer.setExpiresAt(System.currentTimeMillis() + AzureConfig.PLAYER_DATA_EXPIRES_AFTER);
    }
}
