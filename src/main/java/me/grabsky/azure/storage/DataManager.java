package me.grabsky.azure.storage;

import com.google.gson.Gson;
import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.JsonPlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TO-DO: Actual implementation
public class DataManager {
    private final Azure instance;
    private final File dataDirectory;
    private final Gson gson;
    private final Map<UUID, JsonPlayer> players;
    private final Map<UUID, Long> expirations;

    public DataManager(Azure instance) {
        this.instance = instance;
        this.dataDirectory = new File(instance.getDataFolder() + File.separator + "playerdata");
        this.gson = instance.getGson();
        this.players = new HashMap<>();
        this.expirations = new HashMap<>();
    }

    public void create(final Player player) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        final JsonLocation loc = new JsonLocation("world", 1, 2, 3, 4, 5);
        final JsonPlayer jsonPlayer = new JsonPlayer(player.getUniqueId(), null, player.getAddress().getHostName(), "PL", loc);
        final File file = new File(dataDirectory + File.separator + player.getUniqueId() + ".json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final String json = gson.toJson(jsonPlayer, JsonPlayer.class);
            final BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.load(player);
    }

    public void load(final Player player) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        final File file = new File(dataDirectory + File.separator + player.getUniqueId() + ".json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            final JsonPlayer jsonPlayer = gson.fromJson(reader, JsonPlayer.class);
            System.out.println(jsonPlayer.getCountry());
            System.out.println(jsonPlayer.getLastLocation().toLocation().getPitch());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
