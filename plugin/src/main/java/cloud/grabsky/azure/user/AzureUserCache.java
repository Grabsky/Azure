package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.configuration.adapters.UUIDAdapter;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class AzureUserCache implements UserCache, Listener {

    private final BedrockPlugin plugin;
    private final File cacheDirectory;
    private final Map<UUID, AzureUser> internalUserMap;

    private final JsonAdapter<AzureUser> adapter;

    public AzureUserCache(final @NotNull BedrockPlugin plugin) {
        this.plugin = plugin;
        this.cacheDirectory = new File(plugin.getDataFolder(), "usercache");
        this.internalUserMap = new HashMap<>();
        // ...
        this.adapter = new Moshi.Builder()
                .add(UUID.class, UUIDAdapter.INSTANCE)
                .add(Interval.class, new JsonAdapter<Interval>() {

                    @Override
                    public @NotNull Interval fromJson(final @NotNull JsonReader reader) throws IOException {
                        return Interval.of(reader.nextLong(), Unit.MILLISECONDS);
                    }

                    @Override
                    public void toJson(final @NotNull JsonWriter writer, @Nullable final Interval value) throws IOException {
                        writer.value((long) value.as(Unit.MILLISECONDS));
                    }

                })
                .build().adapter(AzureUser.class).nullSafe().indent("  ");
        // Caching users.
        this.cacheUsers();
    }

    public void cacheUsers() throws IllegalStateException {
        // Creating cache directory if does not exist.
        ensureCacheDirectoryExists();
        // Getting list of the files within the cache directory. Non-recursive.
        final File[] files = cacheDirectory.listFiles();
        // ...
        int totalUsers = 0;
        int loadedUsers = 0;
        // Iterating over each file...
        for (final File file : (files != null) ? files : new File[0]) {
            // Skipping non-JSON files.
            if (file.getName().endsWith(".json") == true) {
                try {
                    // Increasing number of total users.
                    totalUsers++;
                    // Loading the user from the file.
                    final AzureUser user = this.readUser(file);
                    // Adding to the cache.
                    internalUserMap.put(user.getUniqueId(), user);
                    // Increasing number of loaded users.
                    loadedUsers++;
                } catch (final IOException | IllegalStateException e) {
                    plugin.getLogger().warning("User cannot be loaded. (FILE = " + file.getPath() + ")");
                    e.printStackTrace();
                }
            }
        }
        // Printing "summary" message to the console.
        plugin.getLogger().info("Succesfully loaded " + loadedUsers + " out of " + totalUsers + " user(s) total.");
    }

    public @NotNull AzureUser readUser(final @NotNull File file) throws IOException, IllegalStateException {
        // Creating a JsonReader from provided file.
        final JsonReader reader = JsonReader.of(buffer(source(file)));
        // Reading the JSON file.
        final AzureUser user = adapter.fromJson(reader);
        // Closing the reader.
        reader.close();
        // Throwing exception in case User ended up being null. Unlikely to happen, but possible.
        if (user == null)
            throw new IllegalArgumentException("Deserialization of " + file.getPath() + " failed: " + null);
        // Returning the value.
        return user;
    }

    public @NotNull CompletableFuture<Boolean> saveUser(final @NotNull User user) throws IllegalStateException {
        // Creating directory in case it does not exist.
        ensureCacheDirectoryExists();
        // ...
        final File file = new File(cacheDirectory, user.getUniqueId() + ".json");
        // Returning CompletableFuture which saves the file asynchronously.
        return CompletableFuture.supplyAsync(() -> {
            try (final JsonWriter writer = JsonWriter.of(buffer(sink(file)))) {
                // Writing data to the file.
                adapter.toJson(writer, (AzureUser) user);
                // Returning 'true' assuming data was written.
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
                return false;
            }
        }).exceptionally(thr -> {
            thr.printStackTrace();
            return false;
        });
    }

    @Override
    public @NotNull @Unmodifiable Collection<User> getUsers() {
        return Collections.unmodifiableCollection(internalUserMap.values());
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
        // No user found. Returning null.
        return null;
    }

    @Override
    public @NotNull User getUser(final @NotNull Player player) {
        final UUID uniqueId = player.getUniqueId();
        // Returning existing user from cache or computing new one, if absent.
        return internalUserMap.computeIfAbsent(uniqueId, (___) -> {
            final @Nullable URL skin = player.getPlayerProfile().getTextures().getSkin();
            // ...
            final @Nullable String address = (player.getAddress() != null) ? player.getAddress().getHostString() : null;
            // Creating instance of AzureUser containing player information.
            final AzureUser user = new AzureUser(
                    player.getName(),
                    uniqueId,
                    (skin != null) ? encodeTextures(skin) : "",
                    (address != null) ? address : "N/A",
                    "N/A", // Country code is fetched asynchronously, just before saving.
                    null,
                    null
            );
            // Saving to the file.
            CompletableFuture.runAsync(() -> {
                final String countryCode = fetchCountry(address, 1);
                // ...
                if (countryCode != null)
                    user.setLastCountryCode(countryCode);
            }).thenCompose((v) -> this.saveUser(user));
            // Returning the User instance.
            return user;
        });
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
        // No user found. Returning false.
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUserLogin(final @NotNull PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        // ...
        if (internalUserMap.containsKey(player.getUniqueId()) == true) {
            final User user = internalUserMap.get(player.getUniqueId());
            // ...
            if (user.getMostRecentBan() != null && user.getMostRecentBan().isActive() == true) {
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                // ...
                final Punishment punishment = user.getMostRecentBan();
                // Preparing the kick message.
                final Component message = (punishment.getDuration().as(Unit.MILLISECONDS) != Long.MAX_VALUE)
                        ? Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE)
                                .placeholder("duration_left", Interval.between((long) punishment.getEndDate().as(Unit.MILLISECONDS), System.currentTimeMillis(), Unit.MILLISECONDS).toString())
                                .placeholder("reason", punishment.getReason())
                                .parse()
                        : Message.of(PluginLocale.COMMAND_BAN_DISCONNECT_MESSAGE_PERMANENT)
                                .placeholder("reason", punishment.getReason())
                                .parse();
                // Setting the kick message, unless null.
                if (message != null)
                    event.kickMessage(message);
            }
        }
    }

    @EventHandler
    public void onUserJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Updating cache with up-to-date data...
        internalUserMap.compute(player.getUniqueId(), (uniqueId, existingUser) -> {
            final @Nullable URL skin = player.getPlayerProfile().getTextures().getSkin();
            // ...
            final @Nullable String address = (player.getAddress() != null) ? player.getAddress().getHostString() : null;
            // Getting punishment information.
            final @Nullable AzurePunishment mostRecentBan = (existingUser != null) ? (AzurePunishment) existingUser.getMostRecentBan() : null;
            final @Nullable AzurePunishment mostRecentMute = (existingUser != null) ? (AzurePunishment) existingUser.getMostRecentMute() : null;
            // Creating instance of AzureUser containing player information.
            final AzureUser user = new AzureUser(
                    player.getName(),
                    uniqueId,
                    (skin != null) ? encodeTextures(skin) : "",
                    (address != null) ? address : "N/A",
                    "N/A", // Country code is fetched asynchronously, just before saving.
                    mostRecentBan,
                    mostRecentMute
            );
            // Saving to the file.
            CompletableFuture.runAsync(() -> {
                final String countryCode = fetchCountry(address, 1);
                // ...
                if (countryCode != null)
                    user.setLastCountryCode(countryCode);
            }).thenCompose((v) -> this.saveUser(user));
            if (existingUser == null || user.equals(existingUser) == false)
                this.saveUser(user);
            // Returning "new" instance, replacing the previous one.
            return user;
        });
    }

    /**
     * Ensures that cache directory exists. In case file is not a directory - it gets deleted and a directory is created in its place.
     */
    private void ensureCacheDirectoryExists() throws IllegalStateException {
        // Creating directory in case it does not exist.
        if (cacheDirectory.exists() == false)
            cacheDirectory.mkdirs();
        // Deleting and re-creating in case file is not a directory.
        if (cacheDirectory.isDirectory() == false) {
            if (cacheDirectory.delete() == false)
                throw new IllegalStateException("File " + cacheDirectory.getPath() + " is not a directory and could not be deleted. Please delete or rename it manually.");
            // Calling (self) after deleting non-directory file. This should not lead to inifnite recursion.
            ensureCacheDirectoryExists();
        }
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

    private static final String API_URL = "https://get.geojs.io/v1/ip/country/";

    private @Nullable String fetchCountry(final @Nullable String address, @Range(from = 0L, to = 5L) int maxRetries) {
        if (address == null)
            return null;
        // Adding one as to
        maxRetries = maxRetries + 1;
        // ...
        try {
            final URL uri = new URL(API_URL + address);
            // ...
            while (maxRetries != 0) {
                // Sending request to an API.
                final BufferedReader reader = new BufferedReader(new InputStreamReader(uri.openStream()));
                if (reader.ready()) {
                    // Getting country name.
                    final String country = reader.readLine();
                    // Closing reader.
                    reader.close();
                    // Returning fetched country name or null.
                    return country.equals("nil") == false ? country : null;
                }
                maxRetries--;
            }
        } catch (final MalformedURLException e) {
            plugin.getLogger().severe("Malformed URI = " + API_URL + "[_REDACTED_ADDRESS_]");
        } catch (final IOException e) {
            plugin.getLogger().severe("An error occurred while trying to send request to '" + API_URL + "[_REDACTED_ADDRESS_]'... retrying...");
        }
        // ...
        return null;
    }

}
