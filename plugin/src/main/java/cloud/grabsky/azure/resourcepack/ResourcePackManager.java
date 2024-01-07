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
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ResourcePackManager implements Listener {

    private final Azure plugin;

    private @Nullable HttpServer server;

    private final List<ResourcePackHolder> holders = new ArrayList<>();


    @SuppressWarnings("deprecation") // Providing SHA-1 is required by the client, despite it being insecure.
    public void reload() throws IOException, URISyntaxException {
        holders.clear();
        // Re-calculating hashes...
        for (final String filename : PluginConfig.RESOURCE_PACK_FILES) {
            final File file = Path.of(plugin.getDataFolder().getPath(), ".public", filename).toFile();
            // Hashing file.
            if (file.exists() == true && file.isDirectory() == false) {
                final UUID uniqueId = UUID.nameUUIDFromBytes(file.getName().getBytes(StandardCharsets.UTF_8));
                // ...
                holders.add(new ResourcePackHolder(uniqueId, file, Files.asByteSource(file).hash(Hashing.sha1()).toString()));
            }
        }
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

    // NOTE: This is likely to be moved onto configuration event once available. (1.20.2)
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) throws URISyntaxException {
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && this.server != null) {
            // ...
            plugin.getBedrockScheduler().run(1L, (task) -> event.getPlayer().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                    .required(PluginConfig.RESOURCE_PACK_IS_REQUIRED)
                    .prompt(PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE)
                    .packs(holders.stream().map(holder -> {
                        final String secret = UUID.randomUUID().toString();
                        // ...
                        server.createContext("/" + secret, (exchange) -> {
                            // Removing the exchange, preventing anyone else from using it.
                            exchange.getHttpContext().getServer().removeContext("/" + secret);
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
                        });
                        // ...
                        final @Nullable URI uri = toURI("http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret);
                        // ...
                        if (uri == null) {
                            plugin.getLogger().severe("Could not create URI: " + "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + secret);
                            plugin.getLogger().severe("  Resource-pack " + holder.file.getName() + " will not be excluded from the request.");
                            return null;
                        }
                        // ...
                        return ResourcePackInfo.resourcePackInfo(holder.uniqueId, uri, holder.hash);
                    }).filter(Objects::nonNull).toList()).build()
            ));
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

    public static @Nullable URI toURI(final @NotNull String uri) {
        try {
            return new URI(uri);
        } catch (final URISyntaxException ___) {
            return null;
        }
    }

}
