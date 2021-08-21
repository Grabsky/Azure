package me.grabsky.azure.storage.data;

import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.objects.JsonPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TO-DO: Actual implementation
public class DataManager {
    private final Map<UUID, JsonPlayer> players;
    private final Map<UUID, Long> expirations;

    public DataManager(Azure instance) {
        this.players = new HashMap<>();
        this.expirations = new HashMap<>();
    }

    public CompletableFuture<JsonPlayer> loadAndGet(UUID uuid) {
        return null;
    }

    public JsonPlayer get(UUID uuid) {
        return null;
    }
}
