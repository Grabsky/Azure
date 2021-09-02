package me.grabsky.azure.storage.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.JsonPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TO-DO: Actual implementation
public class DataManager {
    private final Azure instance;
    private final Gson gson;
    private final Map<UUID, JsonPlayer> players;
    private final Map<UUID, Long> expirations;

    public DataManager(Azure instance) {
        this.instance = instance;
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.players = new HashMap<>();
        this.expirations = new HashMap<>();
    }

    public void createDataFor(Player player) throws IOException {
        final File dataFile = new File(instance.getDataFolder() + File.separator + "storage" + File.separator + player.getUniqueId() + ".json");
        if (!dataFile.exists()) {
            dataFile.mkdirs();
            dataFile.createNewFile();
            final JsonPlayer jsonPlayer = new JsonPlayer(player.getUniqueId(),
                    player.getName(),
                    null,
                    player.getAddress().getHostName(),
                    "PL",
                    player.locale().getLanguage(),
                    System.currentTimeMillis(),
                    new JsonLocation(player.getWorld().getName(),
                            player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ(),
                            player.getLocation().getYaw(),
                            player.getLocation().getPitch()
                    )
            );
            final String json = gson.toJson(jsonPlayer, JsonPlayer.class);
            System.out.println(json);
        }
    }

    public CompletableFuture<JsonPlayer> loadAndGet(UUID uuid) throws IOException {
        return null;
    }

    public JsonPlayer get(UUID uuid) {
        return null;
    }

    public boolean isCached(UUID uuid) {
        return expirations.containsKey(uuid);
    }
}
