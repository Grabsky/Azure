/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure.user;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.configuration.adapters.UUIDAdapter;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.configuration.paper.adapter.ComponentAdapter;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class AzureUserCache implements UserCache, Listener {

    private final Azure plugin;
    private final File cacheDirectory;
    private final Map<UUID, AzureUser> internalUserMap;

    private final JsonAdapter<AzureUser> adapter;

    public AzureUserCache(final @NotNull Azure plugin) {
        this.plugin = plugin;
        this.cacheDirectory = new File(plugin.getDataFolder(), "usercache");
        this.internalUserMap = new HashMap<>();
        // ...
        this.adapter = new Moshi.Builder()
                .add(UUID.class, UUIDAdapter.INSTANCE)
                .add(Component.class, ComponentAdapter.INSTANCE)
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
        plugin.getLogger().info("Successfully loaded " + loadedUsers + " out of " + totalUsers + " user(s) total.");
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
        return internalUserMap.computeIfAbsent(uniqueId, (_) -> {
            final @Nullable URL skin = player.getPlayerProfile().getTextures().getSkin();
            // ...
            final @Nullable String address = (player.getAddress() != null) ? player.getAddress().getHostString() : null;
            // Creating instance of AzureUser containing player information.
            final AzureUser user = new AzureUser(
                    player.getName(),
                    uniqueId,
                    null,
                    null,
                    (skin != null) ? encodeTextures(skin) : "",
                    (address != null) ? address : "N/A",
                    "N/A", // Country code is fetched asynchronously in the next step.
                    0, // maxLevel
                    false,
                    false,
                    null,
                    null
            );
            // Saving to the file.
            CompletableFuture.runAsync(() -> {
                final String countryCode = fetchCountry(address, 1);
                // ...
                if (countryCode != null)
                    user.setLastCountryCode(countryCode);
            }).thenCompose(_ -> this.saveUser(user));
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
        final Player thisPlayer = event.getPlayer();
        // Updating cache with up-to-date data...
        final User thisUser = internalUserMap.compute(thisPlayer.getUniqueId(), (uniqueId, existingUser) -> {
            final @Nullable URL skin = thisPlayer.getPlayerProfile().getTextures().getSkin();
            // ...
            final @Nullable String address = (thisPlayer.getAddress() != null) ? thisPlayer.getAddress().getHostString() : null;
            // Creating instance of AzureUser containing player information.
            final AzureUser computeUser = new AzureUser(
                    thisPlayer.getName(),
                    uniqueId,
                    (existingUser != null) ? existingUser.getDisplayName() : null,
                    (existingUser != null) ? existingUser.getDiscordId() : null,
                    (skin != null) ? encodeTextures(skin) : "",
                    (thisPlayer.getAddress() != null) ? thisPlayer.getAddress().getHostString() : "N/A",
                    "N/A", // Country code is fetched asynchronously in the next step.
                    (existingUser != null) ? existingUser.getMaxLevel() : 0,
                    (existingUser != null) ? existingUser.isVanished() : false,
                    (existingUser != null) ? existingUser.isSpying() : false,
                    (existingUser != null) ? (AzurePunishment) existingUser.getMostRecentBan() : null,
                    (existingUser != null) ? (AzurePunishment) existingUser.getMostRecentMute() : null
            );
            // Completing...
            CompletableFuture.runAsync(() -> {
                final String countryCode = fetchCountry(address, 1);
                // ...
                if (countryCode != null)
                    computeUser.setLastCountryCode(countryCode);
            }).thenCompose(_ -> this.saveUser(computeUser));
            // Saving if modified.
            if (existingUser == null || computeUser.equals(existingUser) == false)
                this.saveUser(computeUser);
            // Returning "new" instance, replacing the previous one.
            return computeUser;
        });
        // Hiding vanished players. This was previously handled inside PlayerListener, until we migrated state storage from PDC to User JSON data.
        // NOTE: Handling that inside PlayerJoinEvent event may result in vanished player being exposed until he is hidden.
        // NOTE: Here and everywhere else (I believe) - vanished players of the same group weight can see each-other.
        if (thisUser.isVanished() == true) {
            // Showing BossBar.
            thisPlayer.showBossBar(PluginConfig.VANISH_BOSS_BAR);
        }
        // Getting instance of LuckPerms to compare group weights later on.
        final LuckPerms luckperms = plugin.getLuckPerms();
        // Iterating over list of online players to hide (this) player from them, and potentially (other) players from (this) player.
        Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
            if (thisPlayer != otherPlayer) {
                final @Nullable Group playerGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(thisPlayer).getPrimaryGroup());
                final @Nullable Group otherGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(otherPlayer).getPrimaryGroup());
                // Getting User object of the (other) player.
                final User otherUser = this.getUser(otherPlayer);
                // Hiding (other) player from (this) player, if feasible.
                if (otherUser.isVanished() == true) {
                    // Comparing group weights.
                    if (playerGroup != null && otherGroup != null && otherGroup.getWeight().orElse(0) > playerGroup.getWeight().orElse(0)) // Same check as below but inverted.
                        thisPlayer.hidePlayer(plugin, otherPlayer);
                }
                // Hiding (this) player from (other) player, if feasible.
                if (thisUser.isVanished() == true) {
                    // Comparing group weights.
                    if (playerGroup != null && otherGroup != null && playerGroup.getWeight().orElse(0) > otherGroup.getWeight().orElse(0))
                        otherPlayer.hidePlayer(plugin, thisPlayer);
                }
            }
        });
        // Setting display name.
        if (thisUser.getDisplayName() != null)
            thisPlayer.displayName(miniMessage().deserialize(thisUser.getDisplayName()));
        // Removing role and associated Discord ID if user is not on the server anymore.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ENABLED == true) {
            // Getting configured server.
            final @Nullable Server server = plugin.getDiscord().getServerById(PluginConfig.DISCORD_INTEGRATIONS_DISCORD_SERVER_ID).orElse(null);
            // Throwing IllegalStateException if configured server is inaccessible.
            if (server == null)
                throw new IllegalStateException("Server is inaccessible: " + PluginConfig.DISCORD_INTEGRATIONS_DISCORD_SERVER_ID);
            // Checking if user has left the server
            if (server.getMemberById(thisUser.getDiscordId()).isPresent() == false) {
                // Removing permission from the player, if configured.
                if ("".equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION) == false)
                    // Loading LuckPerms' User and removing permission from them.
                    plugin.getLuckPerms().getUserManager().modifyUser(thisUser.getUniqueId(), (it) -> {
                        it.data().remove(PermissionNode.builder(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION).build());
                    });
                // Getting verification role.
                final @Nullable Role role = server.getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID).orElse(null);
                // If the role exists, it is now being removed from the user.
                if (role != null)
                    plugin.getDiscord().getUserById(thisUser.getDiscordId()).thenAccept(it -> server.removeRoleFromUser(it, role));
                // Removing associated ID.
                thisUser.setDiscordId(null);
                // Saving... Hopefully this won't cause any CME or data loss due to the fact we're saving file earlier too.
                // I think in the worst case scenario either this or country info would be lost.
                this.saveUser(thisUser);
            }
        }
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
            // Calling (self) after deleting non-directory file. This should not lead to infinite recursion.
            ensureCacheDirectoryExists();
        }
    }

    /**
     * Returns Base64-encoded {@link String} representing textures from specified {@link URL}.
     */
    public static @NotNull String encodeTextures(final @NotNull URL url) {
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
            final URL uri = new URI(API_URL + address).toURL();
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
        } catch (final URISyntaxException e) {
            plugin.getLogger().severe("Malformed URI = " + API_URL + "[_REDACTED_ADDRESS_]");
        } catch (final IOException e) {
            plugin.getLogger().severe("An error occurred while trying to send request to '" + API_URL + "[_REDACTED_ADDRESS_]'... retrying...");
        }
        // ...
        return null;
    }

}
