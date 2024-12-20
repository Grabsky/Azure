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
package cloud.grabsky.azure.resourcepack;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.util.Iterables;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ResourcePackManager implements Listener {

    private final @NotNull Azure plugin;

    // Internal web-server, always listens on all available interfaces.
    private @Nullable HttpServer server;

    // Holds information about resource-packs, such as their UUID and hash. URI is generated on-demand, when requested.
    private List<ResourcePackHolder> holders = new ArrayList<>();

    // Holds information about each tokens created by each player. Map is never cleared but it should not be an issue.
    private final Map<UUID, String> secrets = new HashMap<>();

    /**
     * Reloads resource-packs from configuration and starts internal web-server if necessary.
     */
    @SuppressWarnings("deprecation") // Providing SHA-1 is required by the client for comparing checksums. Warning can be safely ignored.
    public void reload() throws IOException {
        // Clearing previously cached information.
        holders.clear();
        // Converting files to ResourcePackHolder objects.
        for (final String filename : PluginConfig.RESOURCE_PACK_FILES) {
            final File file = Path.of(plugin.getDataFolder().getPath(), ".public", filename).toFile();
            // Checking whether file exists and is not a directory.
            if (file.exists() == true && file.isDirectory() == false) {
                // Generating resource-pack UUID from the file name.
                final UUID uniqueId = UUID.nameUUIDFromBytes(file.getName().getBytes(StandardCharsets.UTF_8));
                // Generating SHA-1 hash and adding new ResourcePackHolder object to the cache.
                holders.add(new ResourcePackHolder(uniqueId, file, Files.asByteSource(file).hash(Hashing.sha1()).toString()));
            }
        }
        // Reversing the list to keep the resource-pack priority how it is defined in the config.
        this.holders = Iterables.reversed(holders);
        // Setting-up internal web server throws BindException in case port is already in use.
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && PluginConfig.RESOURCE_PACK_PORT > 0 && this.server == null) {
            this.server = HttpServer.create(new InetSocketAddress(PluginConfig.RESOURCE_PACK_PORT), 0);
            // Configuring the server to automatically close connections at any other paths.
            server.createContext("/", HttpExchange::close);
            // Starting the server.
            server.start();
            // Logging...
            plugin.getLogger().info("Internal web server started and should be accessible at http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT);
        }
    }

    /**
     * Sends configured resource-packs to the specified {@link Player}.
     */
    public void sendResourcePacks(final @NotNull Player player) {
        final String secret = UUID.randomUUID().toString();
        // Adding secret to the map. This can be later used for context removal.
        secrets.put(player.getUniqueId(), secret);
        // Resetting number of loaded resource-packs. This is mainly for ease of use with other plugins.
        player.setMetadata("sent_resource-packs", new FixedMetadataValue(plugin, 0));
        player.setMetadata("loaded_resource-packs", new FixedMetadataValue(plugin, 0));
        // Measuring request time for debug purposes.
        final long requestStart = System.nanoTime();
        // Creating the ResourcePackRequest instance.
        final ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .replace(true)
                .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                .packs(holders.stream().map(holder -> {
                    // Creating on-demand URI with the generated secret.
                    final @Nullable URI uri = toURI("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
                    // Logging an error in case URI happened to be null, likely due to a syntax error.
                    if (uri == null) {
                        plugin.getLogger().severe("Could not create URI: " + "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
                        plugin.getLogger().severe("  Resource-pack " + holder.file.getName() + " will be excluded from the request.");
                        return null;
                    }
                    // Creating new context at path '/{SECRET}/{RESOURCE_PACK_UUID}' which points to a downloadable file.
                    server.createContext("/" + secret + "/" + holder.uniqueId, (exchange) -> {
                        // Opening FileInputStream for the resource-pack file.
                        final FileInputStream in = new FileInputStream(holder.file);
                        // Reading all bytes.
                        final byte[] bytes = in.readAllBytes();
                        // Closing the FileInputStream.
                        in.close();
                        // Responding with code 200 and bytes length.
                        exchange.sendResponseHeaders(200, bytes.length);
                        // Writing bytes (file) to the response.
                        exchange.getResponseBody().write(bytes);
                        // Closing the response.
                        exchange.getResponseBody().close();
                        // Closing the exchange.
                        exchange.close();
                    });
                    // Setting meta-data so other plugins can easily tell when player loaded all packs.
                    final int count = player.getMetadata("sent_resource-packs").getFirst().asInt();
                    player.setMetadata("sent_resource-packs", new FixedMetadataValue(plugin, count + 1));
                    // Wrapping and returning as ResourcePackInfo object.
                    return ResourcePackInfo.resourcePackInfo(holder.uniqueId, uri, holder.hash);
                }).filter(Objects::nonNull).toList()).build();

        // Sending resource-packs to the player.
        player.sendResourcePacks(request);
        // Measuring request time for debug purposes. Part 2.
        final double requestMeasuredTime = (System.nanoTime() - requestStart) / 1000000.0D;
        // Logging...
        plugin.getLogger().info("Resource-packs requested by '" + player.getUniqueId() + "' and total of " + request.packs().size() + " contexts has been created with secret '" + secret + "'... " + requestMeasuredTime + "ms");
    }

    // NOTE: This is likely to be moved into configuration event once available. (1.20.2)
    // NOTE: We're at 1.21 and API for that does not exist yet...
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        // Setting total number of resource-packs loaded by this player to 0. This is mainly for ease of use with other plugins.
        event.getPlayer().setMetadata("total_loaded_resource-packs", new FixedMetadataValue(plugin, 0));
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && this.server != null) {
            // Sending resource-packs to the player. (next tick)
            plugin.getBedrockScheduler().run(1L, (_) -> sendResourcePacks(event.getPlayer()));
        }
    }

    @EventHandler
    public void onResourcePackStatus(final @NotNull PlayerResourcePackStatusEvent event) {
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && this.server != null) {
            final Player player = event.getPlayer();
            // Removing http contexts when no longer needed. Condition might be confusing but is a bit shorter when handled that way.
            if (event.getStatus() != Status.ACCEPTED && event.getStatus() != Status.SUCCESSFULLY_LOADED)
                server.removeContext("/" + secrets.get(player.getUniqueId()) + "/" + event.getID());
            // When the first pack is accepted.
            if (event.getStatus() == Status.ACCEPTED && player.hasMetadata("is_loading_resource-packs") == false) {
                // Setting the metadata.
                player.setMetadata("is_loading_resource-packs", new FixedMetadataValue(plugin, true));
                // Sending title to the player if enabled.
                if  (PluginConfig.RESOURCE_PACK_LOADING_SCREEN_APPLY_TITLE_AND_SUBTITLE == true) {
                    // Showing the title.
                    event.getPlayer().showTitle(
                            Title.title(
                                    PluginConfig.RESOURCE_PACK_LOADING_SCREEN_TITLE,
                                    PluginConfig.RESOURCE_PACK_LOADING_SCREEN_SUBTITLE,
                                    Title.Times.times(Duration.ofMillis(500), Duration.ofDays(1), Duration.ofMillis(500))
                            )
                    );
                }
                // Applying blindness effect if enabled.
                if (PluginConfig.RESOURCE_PACK_LOADING_SCREEN_APPLY_BLINDNESS == true)
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false));
            }
            // Setting resource-pack related meta-data. This is mainly for ease of use with other plugins.
            if (event.getStatus() == Status.SUCCESSFULLY_LOADED) {
                // Increasing number of resource-packs loaded by this player since the last time they were sent.
                final int count = player.getMetadata("loaded_resource-packs").getFirst().asInt();
                event.getPlayer().setMetadata("loaded_resource-packs", new FixedMetadataValue(plugin, count + 1));
                // Increasing total number of resource-packs loaded by this player since they logged in.
                final int totalCount = player.getMetadata("total_loaded_resource-packs").getFirst().asInt();
                event.getPlayer().setMetadata("total_loaded_resource-packs", new FixedMetadataValue(plugin, totalCount + 1));
                // After last pack has been loaded.
                if (player.getMetadata("sent_resource-packs").getFirst().asInt() == count + 1) {
                    // Removing the metadata.
                    player.removeMetadata("is_loading_resource-packs", plugin);
                    // Clearing the title.
                    if (PluginConfig.RESOURCE_PACK_LOADING_SCREEN_APPLY_TITLE_AND_SUBTITLE == true)
                        player.clearTitle();
                    // Removing blindness effect from the player.
                    if (PluginConfig.RESOURCE_PACK_LOADING_SCREEN_APPLY_BLINDNESS == true)
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
        }
    }

    /**
     * Returns {@link URI} from specified {@link String}, or {@code null} in case syntax error has been caught.
     */
    public static @Nullable URI toURI(final @NotNull String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException ___) {
            return null;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ResourcePackHolder {

        @Getter(AccessLevel.PUBLIC)
        private final UUID uniqueId;

        @Getter(AccessLevel.PUBLIC)
        private final File file;

        @Getter(AccessLevel.PUBLIC)
        private final String hash;

    }

}
