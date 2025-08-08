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
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    @SuppressWarnings("UnstableApiUsage")
    public CompletableFuture<Void> sendResourcePacks(final @NotNull UUID uniqueId, final @NotNull Audience audience) {
        // Logging error if internal web server is null.
        if (this.server == null)
            plugin.getLogger().severe("Requested resource-packs for '" + uniqueId + "' but the internal web server is null.");
        // Generating secret.
        final String secret = UUID.randomUUID().toString();
        // Adding secret to the map. This can be later used for context removal.
        secrets.put(uniqueId, secret);
        // Measuring request time for debug purposes.
        final long requestStart = System.nanoTime();
        // Converting pack holders to ResourcePackInfo objects.
        final List<ResourcePackInfo> packs = holders.stream().map(holder -> {
            // Creating on-demand URI with the generated secret.
            final @Nullable URI uri = toURI("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
            // Logging an error in case URI happened to be null, likely due to a syntax error.
            if (uri == null) {
                plugin.getLogger().severe("Could not create URI: " + "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
                plugin.getLogger().severe("  Resource-pack " + holder.file.getName() + " will be excluded from the request.");
                return null;
            }
            plugin.getLogger().info("[DEBUG] " + "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
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
            // Wrapping and returning as ResourcePackInfo object.
            return ResourcePackInfo.resourcePackInfo(holder.uniqueId, uri, holder.hash);
        }).filter(Objects::nonNull).toList();
        // Preparing CompletableFuture that will be completed when all packs are loaded.
        final CompletableFuture<Void> future = new CompletableFuture<>();
        // Creating the ResourcePackRequest instance.
        final ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(packs)
                .replace(true)
                .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                .callback((packId, status, _) -> {
                    if (audience instanceof PlayerConfigurationConnection connection && status.intermediate() == false) {
                        // Removing HttpContext since it is no longer needed.
                        server.removeContext("/" + secrets.get(connection.getProfile().getId()) + "/" + packId);
                        // Completing the future (and releasing player from configuration phase) after processing the last resource-pack.
                        if (packs.getLast().id().equals(packId) == true)
                            future.complete(null);
                    }
                })
                .build();
        // Sending resource-packs to the player.
        audience.sendResourcePacks(request);
        // Logging...
        plugin.getLogger().info("Resource-packs requested by '" + uniqueId + "' and total of " + request.packs().size() + " contexts has been created with secret '" + secret + "'... " + ((System.nanoTime() - requestStart) / 1000000.0D) + "ms");
        // Returning the future. This will remain incompleted until all packs are processed.
        return future;
    }

    @EventHandler @SuppressWarnings("UnstableApiUsage")
    public void onPlayerConnectionConfiguration(final @NotNull AsyncPlayerConnectionConfigureEvent event) {
        plugin.getLogger().info("[DEBUG] AsyncPlayerConnectionConfigureEvent called for profile identified with '" + event.getConnection().getProfile().getId() + "'...");
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && event.getConnection().getProfile().getId() != null)
            plugin.getResourcePackManager().sendResourcePacks(event.getConnection().getProfile().getId(), event.getConnection().getAudience()).join();
    }

    /**
     * Returns {@link URI} from specified {@link String}, or {@code null} in case syntax error has been caught.
     */
    public static @Nullable URI toURI(final @NotNull String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException _) {
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
