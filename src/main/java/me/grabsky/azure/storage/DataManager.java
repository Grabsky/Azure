package me.grabsky.azure.storage;

import com.google.gson.Gson;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File dataDirectory;
    private final Gson gson;
    private final Map<UUID, JsonPlayer> players;
    private final Map<UUID, Long> expirations;

    public DataManager(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.dataDirectory = new File(instance.getDataFolder() + File.separator + "playerdata");
        this.gson = instance.getGson();
        this.players = new HashMap<>();
        this.expirations = new HashMap<>();
    }

    public boolean isCached(@NotNull final UUID uuid) {
        return players.containsKey(uuid);
    }

    public JsonPlayer getData(@NotNull final UUID uuid) {
        return players.get(uuid);
    }

    // Creates a new player data file or loads existing
    public CompletableFuture<JsonPlayer> create(final Player player) {
        // This can be run asynchronously
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            // Creating Azure/playerdata directory if not existent
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }
            final UUID uuid = player.getUniqueId();
            final File file = new File(dataDirectory + File.separator + uuid + ".json");
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    // Creating JsonPlayer object
                    final JsonPlayer jsonPlayer = new JsonPlayer(uuid, null, player.getAddress().getHostName(), "N/A", new JsonLocation(player.getLocation()));
                    // Creating BufferedWriter to save player data to the .json file
                    final BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
                    // Saving values into to .json file
                    final String json = gson.toJson(jsonPlayer, JsonPlayer.class);
                    writer.write(json);
                    // Closing BufferedWriter as everything have been done
                    writer.flush();
                    writer.close();
                    // Saving JsonPlayer object to the HashMap
                    players.put(uuid, jsonPlayer);
                    return jsonPlayer;
                }
                this.load(uuid, false);
            } catch (IOException e) {
                consoleLogger.error("Error occurred while trying to create data of player with uuid '" + uuid + "'.");
                e.printStackTrace();
            }
            return this.getData(uuid);
        });
    }

    public void load(final UUID uuid, boolean queueUnload) {
        // Creating Azure/playerdata directory if not existent
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        final File file = new File(dataDirectory + File.separator + uuid + ".json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            // Creating BufferedReader to read the <uuid>.json file
            final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            // Parsing file content to a JsonLocation object
            final JsonPlayer jsonPlayer = gson.fromJson(reader, JsonPlayer.class);
            // Making sure file content was parsed successfully
            if (jsonPlayer != null) {
                // Saving JsonPlayer object to the HashMap
                players.put(uuid, jsonPlayer);
                // Queue data unload
                if (queueUnload) {
                    expirations.put(uuid, System.currentTimeMillis());
                }
            } else {
                consoleLogger.error("Error occurred while trying to load data of player with uuid '" + uuid + "'.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(final UUID uuid) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        final File file = new File(dataDirectory + File.separator + uuid + ".json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final JsonPlayer jsonPlayer = players.get(uuid);
            // Creating BufferedWriter to save player data to the .json file
            final BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            // Saving values into to .json file
            final String json = gson.toJson(jsonPlayer, JsonPlayer.class);
            writer.write(json);
            // Closing BufferedWriter as everything have been done
            writer.flush();
            writer.close();
            // Saving JsonPlayer object to the HashMap
            players.put(uuid, jsonPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        // Iterating over cached data objects
        for (Map.Entry<UUID, JsonPlayer> en : players.entrySet()) {
            final Player player = Bukkit.getPlayer(en.getKey());
            // Updating player's last location in case server crash or w/e
            if (player != null && player.isOnline()) {
                en.getValue().setLastLocation(new JsonLocation(player.getLocation()));
            }
            // Saving data
            this.save(en.getKey());
        }
    }

    public int runUnloadTask() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            for (final Map.Entry<UUID, Long> en : expirations.entrySet()) {
                if (System.currentTimeMillis() - en.getValue() > AzureConfig.PLAYER_DATA_EXPIRES_AFTER) {
                    players.remove(en.getKey());
                    expirations.remove(en.getKey());
                }
            }
        }, 1200L, 1200L).getTaskId();
    }

    public int runSaveTask() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this::saveAll, 1200L, 1200L).getTaskId();
    }

}
