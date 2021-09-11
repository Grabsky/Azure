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

public class DataManager {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File dataDirectory;
    private final Gson gson;
    private final Map<UUID, JsonPlayer> players;
    private final Map<UUID, Long> expirations;
    private long secondsSinceLastSave;

    public DataManager(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.dataDirectory = new File(instance.getDataFolder() + File.separator + "playerdata");
        this.gson = instance.getGson();
        this.players = new HashMap<>();
        this.expirations = new HashMap<>();
        this.secondsSinceLastSave = 0;
    }

    // Asynchronously creates a new data file or loads existing
    @Nullable("This is null only if either file fails to be created or existing file is malformed.")
    public CompletableFuture<JsonPlayer> createOrLoad(final Player player) {
        final UUID uuid = player.getUniqueId();
        expirations.remove(uuid);
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            if (hasDataOf(uuid)) return this.loadDataOf(uuid);
            return this.create(uuid);
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
    public CompletableFuture<JsonPlayer> getOfflineData(final UUID uuid, boolean queueUnload) {
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            // Loading and then returning data of player (offline)
            final JsonPlayer jsonPlayer = this.loadDataOf(uuid);
            if (queueUnload) {
                expirations.put(uuid, System.currentTimeMillis());
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

    public void queueUnload(final UUID uuid) {
        expirations.put(uuid, System.currentTimeMillis());
    }

    // Synchronously saves and unloads expired data
    private void unloadExpired() {
        long expiredCount = 0;
        // Iterating over objects scheduled to expire
        for (final Map.Entry<UUID, Long> en : expirations.entrySet()) {
            // Checking if their time has come
            if ((System.currentTimeMillis() - en.getValue()) > AzureConfig.PLAYER_DATA_EXPIRES_AFTER) {
                // Saving and then removing
                this.saveDataOf(en.getKey());
                players.remove(en.getKey());
                expirations.remove(en.getKey());
                expiredCount++;
            }
        }
        if (expiredCount > 0) {
            consoleLogger.success("Successfully unloaded (expired) data of " + expiredCount + " player(s).");
        }
    }

    // Synchronously saves data of all cached players
    public void saveAll() {
        long savedCount = 0;
        // Iterating over cached data objects
        for (Map.Entry<UUID, JsonPlayer> en : players.entrySet()) {
            final Player player = Bukkit.getPlayer(en.getKey());
            // Updating player's last location in case server crash or w/e
            if (player != null && player.isOnline()) {
                en.getValue().setLastLocation(new JsonLocation(player.getLocation()));
            }
            // Saving data
            this.saveDataOf(en.getKey());
            savedCount++;
        }
        if (savedCount > 0) {
            consoleLogger.success("Successfully saved data of " + savedCount + " player(s).");
        }
    }

    // Creates asynchronous task for saving (and removing expired) data of all cached players
    public int runSaveTask() {
        secondsSinceLastSave += 60; // Adding 60 seconds
        return Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            // Unloading expired data
            this.unloadExpired();
            // Saving all data every nth minute
            if (secondsSinceLastSave >= AzureConfig.PLAYER_SAVE_INTERVAL) {
                secondsSinceLastSave = 0;
                this.saveAll();
            }
        }, 1200L, 1200L).getTaskId();
    }
}
