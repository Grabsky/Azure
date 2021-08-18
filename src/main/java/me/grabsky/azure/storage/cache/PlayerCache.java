package me.grabsky.azure.storage.cache;

import me.grabsky.azure.Azure;
import me.grabsky.azure.config.AzureConfig;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.azure.storage.data.PlayerData;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TO-DO: I need to understand why it keeps working.
public class PlayerCache {
    private static final ConsoleLogger consoleLogger = Azure.getInstance().getConsoleLogger();
    private static final DataManager dataManager = Azure.getInstance().getDataManager();
    private static final Map<UUID, PlayerData> playerCache = new HashMap<UUID, PlayerData>();
    private static final Map<UUID, Long> scheduledUnloads = new HashMap<UUID, Long>();

    // Removes PlayerData from map
    public static void remove(UUID uuid) {
        playerCache.remove(uuid);
    }

    // Returns PlayerData for given Player object
    public static CompletableFuture<PlayerData> getData(Player player) {
        if(containsData(player)) {
            return CompletableFuture.completedFuture(playerCache.get(player.getUniqueId()));
        }
        // Return CompletableFuture<PlayerData> obtained from DataManager class (upon database request)
        return getData(player.getUniqueId(), true);
    }

    // Returns PlayerData for player with given name
    public static CompletableFuture<PlayerData> getData(String name) {
        if(containsData(name)) {
            return CompletableFuture.completedFuture(playerCache.get(UUIDCache.getUniqueId(name)));
        }
        // Return CompletableFuture<PlayerData> obtained from DataManager class (upon database request)
        return getData(UUIDCache.getUniqueId(name), true);
    }

    // Returns PlayerData for given uuid
    public static CompletableFuture<PlayerData> getData(UUID uuid, boolean redirected) {
        if(!redirected) {
            if (containsData(uuid)) {
                return CompletableFuture.completedFuture(playerCache.get(uuid));
            }
        }
        // Return CompletableFuture<PlayerData> obtained from DataManager class (upon database request)
        return CompletableFuture.supplyAsync(() -> {
            PlayerData data = dataManager.query(uuid);
            playerCache.put(uuid, data);
            return data;
        });
    }

    public static boolean containsData(Player player) {
        return playerCache.containsKey(player.getUniqueId());
    }

    public static boolean containsData(String name) {
        return playerCache.containsKey(UUIDCache.getUniqueId(name));
    }

    public static boolean containsData(UUID uuid) {
        return playerCache.containsKey(uuid);
    }

    // Schedules data unload for given uuid
    public static void scheduleUnload(UUID uuid) {
        // Calculating time for data removal
        long targetedTime = System.currentTimeMillis() + (AzureConfig.SOFT_CACHE_EXPIRE_AFTER * 30L * 1000L);
        scheduledUnloads.put(uuid, targetedTime);
    }

    // Runs 1-minute-task required to remove unused data.
    public static void runUnloadTask(Azure azure) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(azure, () -> {
            int i = 0;
            for(Map.Entry<UUID, Long> entry : scheduledUnloads.entrySet()) {
                long targetedTime = entry.getValue();
                if(System.currentTimeMillis() >= targetedTime) {
                    UUID uuid = entry.getKey();
                    if(Bukkit.getPlayer(uuid) == null) {
                        remove(uuid);
                        scheduledUnloads.remove(uuid);
                        i++;
                        if(i > 0) {
                            consoleLogger.warn("Unloaded data of " + i + " players.");
                        }
                    }
                }
            }
        }, 0L, 1200L);
    }

    // Removes scheduled earlier task for given uuid (eg. when player join the server again)
    public static void removeScheduledUnloadIfExists(UUID uuid) {
        scheduledUnloads.remove(uuid);
    }
}
