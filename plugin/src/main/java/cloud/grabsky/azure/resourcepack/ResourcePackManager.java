/*
 * MIT License
 *
 * Copyright (c) 2023
 *   ProtonDev220
 *   Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

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

    // Holds information about each tokens created by each player.
    private final Map<UUID, String> secrets = new HashMap<>();

    /**
     * Reloads resource-packs from configuration and starts internal web-server if necessary.
     */
    @SuppressWarnings("deprecation") // Providing SHA-1 is required by the client, despite it being insecure.
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
            // ...
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
        // ...
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .replace(true)
                .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                .packs(holders.stream().map(holder -> {
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
                    // Creating on-demand URI with the generated secret.
                    final @Nullable URI uri = toURI("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret + "/" + holder.uniqueId);
                    // Logging an error in case URI happened to be null, likely due to a syntax error.
                    if (uri == null) {
                        plugin.getLogger().severe("Could not create URI: " + "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret);
                        plugin.getLogger().severe("  Resource-pack " + holder.file.getName() + " will be excluded from the request.");
                        return null;
                    }
                    // Wrapping and returning as ResourcePackInfo object.
                    return ResourcePackInfo.resourcePackInfo(holder.uniqueId, uri, holder.hash);
                }).filter(Objects::nonNull).toList()).build()
        );
    }

    // NOTE: This is likely to be moved into configuration event once available. (1.20.2)
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && this.server != null) {
            // Sending resource-packs to the player. (next tick)
            plugin.getBedrockScheduler().run(1L, (task) -> sendResourcePacks(event.getPlayer()));
        }
    }

    @EventHandler
    public void onResourcePackStatus(final @NotNull PlayerResourcePackStatusEvent event) {
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && this.server != null)
            // Removing http contexts when no longer needed. Condition might be confusing but is a bit shorter when handled that way.
            if (event.getStatus() != Status.ACCEPTED && event.getStatus() != Status.SUCCESSFULLY_LOADED)
                server.removeContext("/" + secrets.get(event.getPlayer().getUniqueId()) + "/" + event.getID());
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
