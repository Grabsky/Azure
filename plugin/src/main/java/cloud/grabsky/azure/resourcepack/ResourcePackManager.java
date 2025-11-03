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
import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.jetty.JettyPrecompressingResourceHandler;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ResourcePackManager implements Listener {

    private final @NotNull Azure plugin;

    // Holds information about resource-packs, such as their UUID and hash.
    private final List<ResourcePackInfo> holders = new ArrayList<>();

    // Holds all pending resource-pack requests.
    private final Map<UUID, RequestInfo> pendingRequests = new HashMap<>();

    // Internal web-server, always listens on all available interfaces.
    private @Nullable Javalin server;

    // Secret is an additional identifier used in resource-pack request URLs.
    // New secret is generated every time the internal web server is restarted.
    private @Nullable String secret;

    static {
        // Increasing maximum file size for precompressing to 20 MB.
        JettyPrecompressingResourceHandler.resourceMaxSize = 20971520;
    }

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
        }
        // Generating new secret.
        this.secret = UUID.randomUUID().toString();
        // Converting files to ResourcePackHolder objects.
        for (final String filename : Iterables.reversed(PluginConfig.RESOURCE_PACK_FILES)) {
            final File file = Path.of(plugin.getDataFolder().getPath(), ".public", filename).toFile();
            // Checking whether file exists and is not a directory.
            if (filename.endsWith(".zip") && file.exists() == true && file.isDirectory() == false) {
                // Generating resource-pack UUID from the file name.
                final UUID uniqueId = UUID.nameUUIDFromBytes(file.getName().getBytes(StandardCharsets.UTF_8));
                // Generating SHA-1 hash and adding new ResourcePackHolder object to the cache.
                holders.add(ResourcePackInfo.resourcePackInfo(
                        uniqueId,
                        URI.create("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)),
                        Files.asByteSource(file).hash(Hashing.sha1()).toString()
                ));
            }
        }
        // Creating and configuring Javalin (web-server) instance, and starting it afterwards.
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && PluginConfig.RESOURCE_PACK_PORT > 0) {
            this.server = Javalin.create(config -> {
                config.showJavalinBanner = false;
                config.startupWatcherEnabled = false;
                // NOTE: Virtual Threads can cause the internal web-server to hang indefinitely. Something yet to figure out.
                config.useVirtualThreads = false;
                // Creating static files configuration as a way to serve resource-packs.
                config.staticFiles.add(staticFiles -> {
                    staticFiles.hostedPath = "/" + secret;
                    staticFiles.directory = plugin.getDataFolder().getAbsolutePath() + File.separator + ".public";
                    staticFiles.location = Location.EXTERNAL;
                    // Since resource-packs are generally lightweight, they can be cached in the memory.
                    staticFiles.precompress = true;
                });
            });
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
        // Preparing CompletableFuture that will be completed when all packs are loaded.
        final CompletableFuture<Void> future = new CompletableFuture<>();
        // Creating AtomicInteger which will store amount of processed resource-packs.
        final AtomicInteger amountProcessed = new AtomicInteger(0);
        // Creating the ResourcePackRequest instance.
        final ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(holders)
                .replace(true)
                .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                // These callbacks may remain incomplete when player disconnects from the server.
                // It is yet to be confirmed whether they expire or not, but we're "cleaning" them after player disconnects anyway.
                .callback((_, status, _) -> {
                    if (status.intermediate() == false) {
                        // Completing the future (and releasing player from configuration phase) after processing the last resource-pack.
                        if (amountProcessed.incrementAndGet() == holders.size() - 1) {
                            // Completing the future and releasing player from configuration phase.
                            future.complete(null);
                            // Removing the future from pending requests.
                            pendingRequests.remove(uniqueId);
                            // Logging...
                            plugin.debug("[ResourcePacks] Resource-packs requested by '" + uniqueId + "' were successfully processed.");
                        }
                    }
                })
                .build();
        // Adding this future to pending requests of that player.
        pendingRequests.put(uniqueId, new RequestInfo(request, future));
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
                 this.sendResourcePacks(event.getConnection().getProfile().getId(), event.getConnection().getAudience()).get(180, TimeUnit.SECONDS);
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

    @EventHandler
    public void onPlayerConnectionClose(final @NotNull PlayerConnectionCloseEvent event) {
        // Getting the UUID of the player.
        final UUID uniqueId = event.getPlayerUniqueId();
        // Cleaning up / removing pending request if it exists.
        if (pendingRequests.containsKey(uniqueId) == true) {
            // Completing the future.
            if (pendingRequests.get(uniqueId).future().isDone() == false)
                pendingRequests.get(uniqueId).future().complete(null);
            // Cleaning-up the request callback.
            pendingRequests.get(uniqueId).request().callback((_, _, _) -> {});
            // Removing the future from pending requests.
            pendingRequests.remove(uniqueId);
        }
    }

    // Holds information about pending request.
    record RequestInfo(@NotNull ResourcePackRequest request, @NotNull CompletableFuture<Void> future) { /* DATA CLASS */ }

}
