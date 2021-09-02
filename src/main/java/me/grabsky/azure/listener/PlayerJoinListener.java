package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.data.DataManager;
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
        // try {
        //     data.createDataFor(event.getPlayer());
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

}
