package me.grabsky.azure.storage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public class PlayerDataManager {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File dataDirectory;
    private final Gson gson;
    private final Map<UUID, JsonPlayer> players;
    private long millisecondsSinceLastSave;

    public PlayerDataManager(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.dataDirectory = new File(instance.getDataFolder() + File.separator + "playerdata");
        this.gson = instance.getGson();
        this.players = new HashMap<>();
        this.millisecondsSinceLastSave = 0;
    }

    // Asynchronously creates a new data file or loads existing
    @Nullable("This is null only if either file fails to be created or existing file is malformed.")
    public CompletableFuture<JsonPlayer> createOrLoad(final Player player) {
        final UUID uuid = player.getUniqueId();
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            final JsonPlayer jsonPlayer = (hasDataOf(uuid)) ? this.loadDataOf(uuid) : this.create(uuid);
            // Disable expiration
            jsonPlayer.setExpireTimestamp(-1);
            return jsonPlayer;
        });
    }

    // Synchronously creates a new data file with default values
    private JsonPlayer create(final UUID uuid) {
        // Creating 'Azure/playerdata/' directory if it doesn't exist
        dataDirectory.mkdirs();
        final File file = new File(dataDirectory + File.separator + uuid + ".json");
        try {
            // Creating new file if it doesn't exist
            file.createNewFile();
            // Creating JsonPlayer object
            final JsonPlayer jsonPlayer = new JsonPlayer(uuid, null, "N/A", "N/A", new JsonLocation(Bukkit.getWorlds().get(0).getSpawnLocation()));
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
        } catch (IOException e) {
            consoleLogger.error("Error occurred while trying to create data of player with uuid '" + uuid + "'.");
            e.printStackTrace();
        }
        return null;
    }

    // Returns true if data attached to specified UUID is currently in cache
    public boolean isCached(@NotNull final UUID uuid) {
        return players.containsKey(uuid);
    }

    // Returns cached data attached to specified uuid
    @Nullable("Direct call can be null if trying to get data attached to invalid/not-existent/non-online-player UUID.")
    public JsonPlayer getOnlineData(@NotNull final UUID uuid) {
        return players.get(uuid);
    }

    // Returns cached data attached to specified player; Technically could be null if data failed to load
    @NotNull
    public JsonPlayer getOnlineData(@NotNull final Player player) {
        return players.get(player.getUniqueId());
    }

    @Nullable("You should first check if data exists using DataManager#hasDataOf(UUID uuid) method.")
    public CompletableFuture<JsonPlayer> getOfflineData(final UUID uuid, boolean scheduleUnload) {
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            // Loading and then returning data of player (offline)
            final JsonPlayer jsonPlayer = this.loadDataOf(uuid);
            if (jsonPlayer != null && scheduleUnload) {
                jsonPlayer.setExpireTimestamp(System.currentTimeMillis() + AzureConfig.PLAYER_DATA_EXPIRES_AFTER);
            }
            return jsonPlayer;
        });
    }

    // Returns true if data file attached to specified UUID exists
    public boolean hasDataOf(final UUID uuid) {
        return new File(dataDirectory + File.separator + uuid + ".json").exists();
    }

    // Synchronously loads data attached to specified UUID
    @Nullable
    private JsonPlayer loadDataOf(final UUID uuid) {
        if (players.containsKey(uuid)) return players.get(uuid);
        // Creating 'Azure/playerdata/' directory if it doesn't exist
        dataDirectory.mkdirs();
        final File file = new File(dataDirectory + File.separator + uuid + ".json");
        try {
            // Creating BufferedReader to read the <uuid>.json file
            final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            // Parsing file content to a JsonLocation object
            final JsonPlayer jsonPlayer = gson.fromJson(reader, JsonPlayer.class);
            // Making sure file content was parsed successfully
            if (jsonPlayer != null) {
                // Saving JsonPlayer object to the HashMap
                players.put(uuid, jsonPlayer);
                return jsonPlayer;
            }
        } catch (JsonSyntaxException e) {
            consoleLogger.error("Error occurred while trying to load data of player with uuid '" + uuid + "'. Malformed JSON?");
            e.printStackTrace();
            // Renaming invalid data file to _invalid.<uuid>.json and creating a new one
            file.renameTo(new File(dataDirectory + File.separator + "_invalid." + uuid + ".json"));
            return this.create(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Synchronously saves data attached to specified UUID
    public void saveDataOf(final UUID uuid) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        final File file = new File(dataDirectory + File.separator + uuid + ".json");
        try {
            // Creating new file if it doesn't exist
            file.createNewFile();
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

    // Synchronously saves data of all cached players
    public void saveAll() {
        final long s1 = System.nanoTime();
        long saveCount = 0;
        // Iterating over cached data objects
        for (Map.Entry<UUID, JsonPlayer> en : players.entrySet()) {
            final Player player = Bukkit.getPlayer(en.getKey());
            // Updating player's last location in case server crash or w/e
            if (player != null && player.isOnline()) {
                en.getValue().setLastLocation(new JsonLocation(player.getLocation()));
            }
            // Saving data
            this.saveDataOf(en.getKey());
            saveCount++;
        }
        if (saveCount > 0) {
            consoleLogger.success("Successfully saved data of " + saveCount + " player(s) in " + (System.nanoTime() - s1) / 1000000D + "ms");
        }
    }

    final long taskIntervalMs = 10000;

    // Creates asynchronous task for saving (and removing expired) data of all cached players
    public int runSaveTask() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            final long s1 = System.nanoTime();
            long saveCount = 0;
            // Updating
            final boolean readyToSave = (millisecondsSinceLastSave >= AzureConfig.PLAYER_DATA_SAVE_INTERVAL);
            millisecondsSinceLastSave = (readyToSave) ? 0 : millisecondsSinceLastSave + taskIntervalMs;
            // Unloading expired data
            for (Map.Entry<UUID, JsonPlayer> en : players.entrySet()) {
                final UUID uuid = en.getKey();
                final JsonPlayer jsonPlayer= en.getValue();
                // Checking if data has expired
                if (jsonPlayer.getExpireTimestamp() != -1 && jsonPlayer.getExpireTimestamp() >= System.currentTimeMillis()) {
                    // Saving, and then removing data
                    this.saveDataOf(uuid);
                    players.remove(uuid);
                    continue;
                }
                // Saving data if ready (every fifth task cycle)
                if (readyToSave) {
                    final Player player = Bukkit.getPlayer(en.getKey());
                    // Updating player's last location in case server crash or w/e
                    if (player != null && player.isOnline()) {
                        en.getValue().setLastLocation(new JsonLocation(player.getLocation()));
                    }
                    // Saving data
                    this.saveDataOf(uuid);
                    saveCount++;
                }
            }
            if (saveCount > 0) {
                consoleLogger.success("Successfully saved data of " + saveCount + " player(s) in " + (System.nanoTime() - s1) / 1000000D + "ms");
            }
        }, taskIntervalMs / 50, taskIntervalMs / 50).getTaskId();
    }
}
