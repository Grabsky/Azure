package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.JsonPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final Azure instance;
    private final DataManager data;

    public PlayerQuitListener(Azure instance) {
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        // Updating last known location
        final JsonPlayer jsonPlayer = data.getOnlineData(player);
        jsonPlayer.setLastLocation(new JsonLocation(player.getLocation()));
        // Marking data as scheduled to unload (expiration)
        data.queueUnload(player.getUniqueId());
    }
}
