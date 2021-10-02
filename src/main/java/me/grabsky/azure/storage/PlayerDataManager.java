package me.grabsky.azure.storage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.grabsky.azure.Azure;
import me.grabsky.azure.api.PlayerDataAPI;
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

// TO-DO: Make sure to properly INVALIDATE/UNLOAD expired data in all relevant places.
// Future TO-DO: Rewrite save task logic to be less hacky. Current approach does NOT scale properly.
public class PlayerDataManager implements PlayerDataAPI {
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

    @Override
    public CompletableFuture<JsonPlayer> createOrLoad(final Player player) {
        final UUID uuid = player.getUniqueId();
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> (this.hasDataOf(uuid)) ? this.loadDataOf(uuid) : this.create(uuid));
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
            final JsonPlayer jsonPlayer = new JsonPlayer("N/A", "N/A", new JsonLocation(Bukkit.getWorlds().get(0).getSpawnLocation()), new HashMap<>());
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

    @Override
    public boolean isCached(@NotNull final UUID uuid) {
        return players.containsKey(uuid);
    }

    @Override
    public boolean hasDataOf(final UUID uuid) {
        return new File(dataDirectory + File.separator + uuid + ".json").exists();
    }

    @Override
    public JsonPlayer getOnlineData(@NotNull final Player player) {
        return players.get(player.getUniqueId());
    }

    @Override
    public JsonPlayer getOnlineData(@NotNull final UUID uuid) {
        return players.get(uuid);
    }

    @Override
    public CompletableFuture<JsonPlayer> getOfflineData(final UUID uuid, boolean scheduleUnload) {
        return new CompletableFuture<JsonPlayer>().completeAsync(() -> {
            // Loading and then returning data of player if
            final JsonPlayer jsonPlayer = this.loadDataOf(uuid);
            if (jsonPlayer != null && scheduleUnload) {
                jsonPlayer.setExpiresAt(System.currentTimeMillis() + AzureConfig.PLAYER_DATA_EXPIRES_AFTER);
            }
            return jsonPlayer;
        });
    }

    // Synchronously loads data attached to specified UUID
    @Nullable("You should first check if data exists using DataManager#hasDataOf(UUID uuid) method.")
    private JsonPlayer loadDataOf(final UUID uuid) {
        if (players.containsKey(uuid)) return players.get(uuid);
        if (this.hasDataOf(uuid)) {
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
                // CORRUPTED DATA FOUND: Renaming existing file to _invalid.<uuid>.json and creating a new one
                consoleLogger.error("Error occurred while trying to load data of player with uuid '" + uuid + "'. Malformed JSON?");
                e.printStackTrace();
                // Renaming invalid data file to _invalid.<uuid>.json and creating a new one
                file.renameTo(new File(dataDirectory + File.separator + "_invalid." + uuid + ".json"));
                return this.create(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    // Loads data of all online players; Deprecated as it SHOULD NOT be used on production servers
    @Deprecated
    public void loadDataOfOnlinePlayers() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.loadDataOf(player.getUniqueId());
        }
    }

    // Synchronously saves data of all cached players
    public void saveAll() {
        final long s = System.nanoTime();
        long saveCount = 0;
        // Iterating over cached data objects
        for (Map.Entry<UUID, JsonPlayer> entry : players.entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());
            // Updating player's last location in case server crash or w/e
            if (player != null && player.isOnline()) {
                entry.getValue().setLastLocation(player.getLocation());
            }
            // Saving data
            this.saveDataOf(entry.getKey());
            saveCount++;
        }
        if (saveCount > 0) {
            consoleLogger.success("Successfully saved data of " + saveCount + " player(s) in " + (System.nanoTime() - s) / 1000000D + "ms");
        }
    }

    // Creates task for saving (and removing expired) data of all cached players
    public int runSaveTask() {
        final long taskIntervalMs = 60000; // Should NOT be modified, it's here just for sake of accessibility
        final long s = System.nanoTime(); // Value required to calculate logic operation time
        return Bukkit.getScheduler().runTaskTimer(instance, () -> {
            // Creating copy of player's data map to prevent concurrency issues
            final HashMap<UUID, JsonPlayer> playersCopy = new HashMap<>(players);
            // Running the rest of stuff off the main thread
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                long saveCount = 0;
                // Checking data is ready to save
                final boolean readyToSave = (millisecondsSinceLastSave >= AzureConfig.PLAYER_DATA_SAVE_INTERVAL);
                millisecondsSinceLastSave = (readyToSave) ? 0 : millisecondsSinceLastSave + taskIntervalMs;
                // Unloading expired data
                for (final Map.Entry<UUID, JsonPlayer> entry : playersCopy.entrySet()) {
                    final UUID uuid = entry.getKey();
                    final JsonPlayer jsonPlayer= entry.getValue();
                    // Checking if data has expired
                    if (jsonPlayer.getExpiresAt() != -1 && jsonPlayer.getExpiresAt() >= System.currentTimeMillis()) {
                        // Saving, and then removing data
                        this.saveDataOf(uuid);
                        players.remove(uuid);
                        continue;
                    }
                    // Saving data if ready (every fifth task cycle)
                    if (readyToSave) {
                        final Player player = Bukkit.getPlayer(entry.getKey());
                        // Updating player's last location in case server crash or w/e
                        if (player != null && player.isOnline()) {
                            entry.getValue().setLastLocation(player.getLocation());
                        }
                        // Saving data
                        this.saveDataOf(uuid);
                        saveCount++;
                    }
                }
                if (saveCount > 0) {
                    consoleLogger.success("Successfully saved data of " + saveCount + " player(s) in " + (System.nanoTime() - s) / 1000000D + "ms");
                }
            });
        }, taskIntervalMs / 50, taskIntervalMs / 50).getTaskId();
    }
}