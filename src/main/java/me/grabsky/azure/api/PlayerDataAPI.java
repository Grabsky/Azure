package me.grabsky.azure.api;

import me.grabsky.azure.storage.objects.JsonPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataAPI {

    /**
     * Asynchronously creates a new data file or loads existing.
     * @return CompletableFuture containing JsonPlayer object which can be null.
     */
    CompletableFuture<@Nullable("This is null only if either file fails to be created or existing file is malformed.") JsonPlayer> createOrLoad(@NotNull final Player player);

    /**
     * Returns true if data attached to specified UUID is currently in cache.
     * @return true if data attached to specified UUID is currently in cache.
     */
    boolean isCached(@NotNull final UUID uuid);

    /**
     * Returns true if data file attached to specified UUID exists
     * @return true if data file attached to specified UUID exists.
     */
    boolean hasDataOf(final UUID uuid);

    /**
     * Returns cached data attached to specified Player object.
     * @return JsonPlayer object from cache, otherwise - null.
     */
    JsonPlayer getOnlineData(@NotNull final Player player);

    /**
     * Returns cached data attached to specified UUID.
     * @return JsonPlayer object if data exists in cache, otherwise - null.
     */
    @Nullable("Can be null if trying to get data attached to INVALID, NON-EXISTENT or NON-ONLINE-PLAYER UUID.")
    JsonPlayer getOnlineData(@NotNull final UUID uuid);

    /**
     * Loads player data from a file and then returns CompletableFuture with JsonPlayer object.
     * @return CompletableFuture containing JsonPlayer object.
     */
    CompletableFuture<JsonPlayer> getOfflineData(final UUID uuid, boolean scheduleUnload);




}
