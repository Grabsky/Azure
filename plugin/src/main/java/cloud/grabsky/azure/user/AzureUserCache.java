package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.configuration.adapters.UUIDAdapter;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class AzureUserCache implements UserCache, Listener {

    private final BedrockPlugin plugin;
    private final Moshi moshi;
    private final File cacheDirectory;
    private final Map<UUID, AzureUser> internalUserMap;

    public AzureUserCache(final @NotNull BedrockPlugin plugin) {
        this.plugin = plugin;
        this.moshi = new Moshi.Builder().add(UUID.class, UUIDAdapter.INSTANCE).build();
        this.cacheDirectory = new File(plugin.getDataFolder(), "usercache");
        this.internalUserMap = new HashMap<>();
        // ...
        this.loadCache();
    }

    public void loadCache() throws IllegalStateException {
        // Creating cache directory if does not exist.
        ensureCacheDirectoryExists();
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
                try {
                    if (this.loadUser(file) != null)
                        count++;
                } catch (final IOException | IllegalStateException e) {
                    // TO-DO: Log to the console.
                }
        // ...
        plugin.getLogger().info(count + " users were loaded from cache.");
    }

    public @Nullable User loadUser(final @NotNull File file) throws IOException, IllegalStateException {
        final JsonReader reader = JsonReader.of(buffer(source(file)));
        // ...
        final AzureUser user = moshi.adapter(AzureUser.class).fromJson(reader);
        // ...
        reader.close();
        // ...
        if (user == null)
            throw new IllegalArgumentException("Deserialization of " + file.getPath() + " failed." );
        // ...
        internalUserMap.put(user.getUniqueId(), user);
        // ...
        return user;
}

    public @NotNull CompletableFuture<Boolean> save(final @NotNull User user) throws IllegalStateException {
        // Creating directory in case it does not exist.
        ensureCacheDirectoryExists();
        // ...
        final File file = new File(cacheDirectory, user.getUniqueId() + ".json");
        // ...
        final JsonAdapter<AzureUser> adapter = moshi.adapter(AzureUser.class).indent("  ");
        // ...
        return CompletableFuture.supplyAsync(() -> {
            try (final JsonWriter writer = JsonWriter.of(buffer(sink(file)))) {
                // Writing data to the file
                adapter.toJson(writer, (AzureUser) user);
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
    public void onUserJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // ...
        internalUserMap.compute(player.getUniqueId(), (uuid, existingUser) -> {
            final @Nullable URL skin = player.getPlayerProfile().getTextures().getSkin();
            // ...
            final AzureUser user = new AzureUser(player.getName(), uuid, (skin != null) ? encodeTextures(skin) : "");
            // ...
            if (existingUser == null || user.equals(existingUser) == false)
                this.save(user).thenAccept((isSuccess) -> {
                    player.getInventory().addItem(new ItemBuilder(Material.PLAYER_HEAD, 1).setSkullTexture(user.getTextures()).build());
                });
            // ...
            return user;
        });
    }

    private void ensureCacheDirectoryExists() throws IllegalStateException {
        // Creating directory in case it does not exist.
        if (cacheDirectory.exists() == false)
            cacheDirectory.mkdirs();
        // Throwing an exception in case file is not a directory.
        if (cacheDirectory.isDirectory() == false)
            throw new IllegalStateException(cacheDirectory.getPath() + " is not a directory.");
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

    /**
     * Returns Base64-encoded {@link String} representing textures from specified {@link URL}.
     */
    private static @NotNull String encodeTextures(final @NotNull URL url) {
        return Base64.getEncoder().encodeToString(
                               """
                               {
                                 "textures": {
                                   "SKIN": {
                                     "url": "%s"
                                   }
                                 }
                               }
                               """.trim().formatted(url).getBytes()
        );
    }

}
