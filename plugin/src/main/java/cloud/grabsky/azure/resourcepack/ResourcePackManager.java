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
import io.javalin.Javalin;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ResourcePackManager implements Listener {

    private final @NotNull Azure plugin;


    // Holds information about resource-packs, such as their UUID and hash.
    private final List<ResourcePackHolder> holders = new ArrayList<>();

    // Internal web-server, always listens on all available interfaces.
    private @Nullable Javalin server;

    // Secret is an additional identifier used in resource-pack request URLs.
    // New secret is generated every time the internal web server is restarted.
    private @Nullable String secret;

    /**
     * Reloads resource-packs from configuration and starts internal web-server if necessary.
     */
    @SuppressWarnings("deprecation") // Providing SHA-1 is required by the client for comparing checksums. Warning can be safely ignored.
    public void reload() throws IOException {
        // Clearing previously cached information.
        holders.clear();
        // Restarting the HTTP server if necessary.
        if (this.server != null) {
            // Stopping the server.
            this.server.stop();
            // Cleaning-up anything HttpServer could've potentially left in the memory.
            this.server = null;
            // Cleaning-up the secret.
            this.secret = null;
        }
        // Logging...
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && PluginConfig.RESOURCE_PACK_PORT > 0) {
            this.server = Javalin.create(config -> {
                config.useVirtualThreads = true;
                config.showJavalinBanner = false;
            });
            // Generating new secret.
            this.secret = UUID.randomUUID().toString();
            // Configuring the server to automatically close connections at any non-desired paths.
            server.get("/*", (context) -> {
                try {
                    // Responding with error code 403.
                    context.status(403);
                    // Logging...
                    plugin.debug("[ResourcePacks] [" + context.req().getRemoteAddr().replace("/", "") + "] [" + context.statusCode() + "] " + (context.req().getRequestURI().length() < 96 ? context.req().getRequestURI() : context.req().getRequestURI().substring(0, 96) + "...") + " (Forbidden)");
                } catch (final Throwable thr) {
                    plugin.debug("[ResourcePacks] [" + context.req().getRemoteAddr().replace("/", "") + "] [" + context.statusCode() + "] " + (context.req().getRequestURI().length() < 96 ? context.req().getRequestURI() : context.req().getRequestURI().substring(0, 96) + "...") + " (Error)");
                    thr.printStackTrace();
                }
            });
            // Converting files to ResourcePackHolder objects.
            for (final String filename : Iterables.reversed(PluginConfig.RESOURCE_PACK_FILES)) {
                final File file = Path.of(plugin.getDataFolder().getPath(), ".public", filename).toFile();
                // Checking whether file exists and is not a directory.
                if (filename.endsWith(".zip") && file.exists() == true && file.isDirectory() == false) {
                    // Generating resource-pack UUID from the file name.
                    final UUID uniqueId = UUID.nameUUIDFromBytes(file.getName().getBytes(StandardCharsets.UTF_8));
                    // Generating SHA-1 hash and adding new ResourcePackHolder object to the cache.
                    holders.add(new ResourcePackHolder(uniqueId, file, Files.asByteSource(file).hash(Hashing.sha1()).toString()));
                    // Creating context at path '/{SECRET}/{RESOURCE_PACK_UUID}' which points to a downloadable file.
                    server.get("/" + secret + "/" + uniqueId, (context) -> {
                        try {
                            final long start = System.nanoTime();
                            // Responding with code 200 and bytes length.
                            context.status(200);
                            // Opening BufferedInputStream for the resource-pack file.
                            // It can't be done with try-with-resources because Javalin handles that on it's own and closes the stream after the response is sent.
                            final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), 8192);
                            // Transferring the file to the response body.
                            context.result(in);
                            // Logging...
                            plugin.debug("[ResourcePacks] [" + context.req().getRemoteAddr().replace("/", "") + "] [" + context.statusCode() + "] " + file.getName() + String.format(" (%.2fms)", (System.nanoTime() - start) / 1000000.0));
                        } catch (final Throwable thr) {
                            plugin.getLogger().severe("[ResourcePacks] [" + context.req().getRemoteAddr().replace("/", "") + "] [" + context.statusCode() + "] " + file.getName() + " (Error)");
                            thr.printStackTrace();
                        }
                    });
                }
            }
            // Starting the server.
            server.start(PluginConfig.RESOURCE_PACK_PORT);
        }
    }

    /**
     * Sends configured resource-packs to the specified {@link Player}.
     */
    public CompletableFuture<Void> sendResourcePacks(final @NotNull UUID uniqueId, final @NotNull Audience audience) {
        // Logging error if internal web server is null.
        if (this.server == null) {
            plugin.debug("[ResourcePacks] Requested resource-packs for '" + uniqueId + "' but the internal web server is null.");
            return CompletableFuture.completedFuture(null);
        }
        // Converting pack holders to ResourcePackInfo objects.
        final List<ResourcePackInfo> packs = holders.stream().map(holder -> {
            // Creating URI this resource-pack will be accessible at.
            final URI uri = URI.create("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
            // Wrapping and returning as ResourcePackInfo object.
            return ResourcePackInfo.resourcePackInfo(holder.uniqueId, uri, holder.hash);
        }).toList();
        // Preparing CompletableFuture that will be completed when all packs are loaded.
        final CompletableFuture<Void> future = new CompletableFuture<>();
        // Creating the ResourcePackRequest instance.
        final ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(packs)
                .replace(true)
                .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                .callback((packId, status, _) -> {
                    if (status.intermediate() == false) {
                        // Completing the future (and releasing player from configuration phase) after processing the last resource-pack.
                        if (packs.getLast().id().equals(packId) == true) {
                            future.complete(null);
                            plugin.debug("[ResourcePacks] Resource-packs requested by '" + uniqueId + "' were successfully processed.");
                        }
                    }
                })
                .build();
        // Sending resource-packs to the player.
        audience.sendResourcePacks(request);
        // Returning the future. This will remain incomplete until all packs are processed.
        return future;
    }

    @EventHandler @SuppressWarnings("UnstableApiUsage")
    public void onPlayerConnectionConfiguration(final @NotNull AsyncPlayerConnectionConfigureEvent event) {
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && event.getConnection().getProfile().getId() != null) {
            // Logging...
            plugin.debug("[ResourcePacks] Player '" + event.getConnection().getProfile().getName() + "' identified with '" + event.getConnection().getProfile().getId() + "' requested resource-packs...");
            // Sending resource-packs to the player.
            try {
                plugin.getResourcePackManager().sendResourcePacks(event.getConnection().getProfile().getId(), event.getConnection().getAudience()).get(180, TimeUnit.SECONDS);
            } catch (final TimeoutException exception) {
                // Logging cancellation reason to the console.
                plugin.getLogger().severe("Request invoked by " + event.getConnection().getProfile().getName() + " (" + event.getConnection().getProfile().getId() + ")" + " automatically cancelled after 180 seconds.");
                // Disconnecting player from the server.
                event.getConnection().disconnect(Component.translatable("disconnect.timeout"));
            } catch (final InterruptedException | ExecutionException exception) {
                // Logging cancellation reason to the console.
                plugin.getLogger().severe("Request invoked by " + event.getConnection().getProfile().getName() + " (" + event.getConnection().getProfile().getId() + ")" + " has been cancelled due to following error(s):");
                plugin.getLogger().severe(" (1) " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
                if (exception.getCause() != null)
                    plugin.getLogger().severe(" (2) " + exception.getCause().getClass().getSimpleName() + ": " + exception.getCause().getMessage());
                // Disconnecting player from the server.
                event.getConnection().disconnect(Component.translatable("disconnect.timeout"));
            }
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
