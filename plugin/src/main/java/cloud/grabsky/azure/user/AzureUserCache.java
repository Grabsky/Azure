package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.configuration.adapters.UUIDAdapter;
import cloud.grabsky.bedrock.BedrockPlugin;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

@Internal
public final class AzureUserCache implements UserCache, Listener {

    private final BedrockPlugin plugin;
    private final Moshi moshi;
    private final File cacheDirectory;
    private final Map<UUID, User> internalUserMap;

    public AzureUserCache(final BedrockPlugin plugin) {
        this.plugin = plugin;
        this.moshi = new Moshi.Builder().add(UUID.class, UUIDAdapter.INSTANCE).build();
        this.cacheDirectory = new File(plugin.getDataFolder(), "usercache");
        this.internalUserMap = new HashMap<>();
        // ...
        this.loadCache();
    }

    public void loadCache() {
        // Creating cache directory if does not exist.
        if (cacheDirectory.exists() == false)
            cacheDirectory.mkdirs();
        // ...
        if (cacheDirectory.isDirectory() == false)
            throw new IllegalStateException(cacheDirectory.getPath() + " is not a directory.");
        // ...
        final File[] files = cacheDirectory.listFiles();
        // ...
        if (files == null) {
            plugin.getLogger().info("No users were loaded from cache: " + cacheDirectory.getPath() + " is empty.");
            return;
        }
        int count = 0;
        // ...
        for (final File file : files)
            if (file.getName().endsWith(".json") == true)
                if (this.loadUser(file) != null)
                    count++;
        // ...
        plugin.getLogger().info(count + " users were loaded from cache.");
    }

    public @Nullable User loadUser(final @NotNull File file) {
        try (final JsonReader reader = JsonReader.of(buffer(source(file)))) {
            // ...
            final User user = moshi.adapter(AzureUser.class).fromJson(reader);
            // ...
            if (user == null)
                throw new IllegalArgumentException("Deserialization of " + file.getPath() + " failed." );
            // ...
            return internalUserMap.putIfAbsent(user.getUniqueId(), user);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CompletableFuture<Boolean> save(final @NotNull User user) {
        // Creating directory in case it does not exist.
        if (cacheDirectory.exists() == false)
            cacheDirectory.mkdirs();
        // Throwing an exception in case file is not a directory.
        if (cacheDirectory.isDirectory() == false)
            throw new IllegalStateException(cacheDirectory.getPath() + " is not a directory.");
        // ...
        final File file = new File(cacheDirectory, user.getUniqueId() + ".json");
        // ...
        return CompletableFuture.supplyAsync(() -> {
            try (final JsonWriter writer = JsonWriter.of(buffer(sink(file)))) {
                // Writing data to the file
                moshi.adapter(AzureUser.class).toJson(writer, (AzureUser) user);
                // ...
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public @Nullable User getUser(final @NotNull UUID uniqueId) {
        return internalUserMap.get(uniqueId);
    }

    @Override
    public @Nullable User getUser(final @NotNull String name) {
        for (final User user : internalUserMap.values())
            if (user.getName().equals(name) == true)
                return user;
        // ...
        return null;
    }

    @Override
    public boolean hasUser(final @NotNull UUID uniqueId) {
        return internalUserMap.containsKey(uniqueId);
    }

    @Override
    public boolean hasUser(final @NotNull String name) {
        for (final User user : internalUserMap.values())
            if (user.getName().equals(name) == true)
                return true;
        // ...
        return false;
    }

    @EventHandler
    public void onUserJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // ...
        internalUserMap.compute(player.getUniqueId(), (uuid, existingUser) -> {
            final User user = new AzureUser(player.getName(), uuid, readProperty(player.getPlayerProfile(), "textures"));
            // ...
            if (existingUser == null || user.equals(existingUser) == false)
                this.save(user).thenAccept(isSuccess -> {
                   if (isSuccess == false)
                       plugin.getLogger().warning("Could not load " + uuid + ".json");
                });
            // ...
            return user;
        });
    }

    /**
     * Returns {@link String} value of specified property, or {@code null}.
     */
    private static @Nullable String readProperty(final @NotNull PlayerProfile profile, final @NotNull String propertyName) {
        for (final ProfileProperty property : profile.getProperties())
            if (property.getName().equals(propertyName) == true)
                return property.getValue();
        // Returning 'null' in case property was not found.
        return null;
    }

}
