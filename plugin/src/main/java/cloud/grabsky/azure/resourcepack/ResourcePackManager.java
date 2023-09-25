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
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ResourcePackManager implements Listener, HttpHandler {

    private final Azure plugin;

    @Getter(AccessLevel.PUBLIC)
    private final String token = UUID.randomUUID().toString();

    @Getter(AccessLevel.PUBLIC)
    private @Nullable File file;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable String hash;

    private @Nullable HttpServer server;

    public void reload() throws IOException {
        this.file = Path.of(plugin.getDataFolder().getPath(), ".public_resourcepack", PluginConfig.RESOURCE_PACK_FILE).toFile();
        // Hashing file.
        if (file.exists() == true && file.isDirectory() == false)
            this.hash = Files.asByteSource(file).hash(Hashing.sha1()).toString();
        // Setting-up internal web server throws BindException in case port is already in use.
        if (PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS.isBlank() == false && PluginConfig.RESOURCE_PACK_PORT > 0 && server == null) {
            this.server = HttpServer.create(new InetSocketAddress(PluginConfig.RESOURCE_PACK_PORT), 0);
            // Configuring the server to be accessible only at unique-per-runtime path.
            server.createContext("/" + token, this);
            // Configuring the server to automatically close connections at any other paths.
            server.createContext("/", HttpExchange::close);
            // Starting the server.
            server.start();
            // ...
            plugin.getLogger().info("Internal web server started and should be accessible at http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + token);
        }
    }

    @Override
    public void handle(final @NotNull HttpExchange exchange) throws IOException {
        final InetAddress host = exchange.getRemoteAddress().getAddress();
        // Refusing unknown connections.
        if (plugin.getServer().getOnlinePlayers().stream().anyMatch(player -> player.getAddress().getAddress().equals(host) == true) == false)
            exchange.close();
        // Opening FileInputStream for the resourcepack file.
        final FileInputStream in = new FileInputStream(file);
        // Reading all bytes.
        final byte[] bytes = in.readAllBytes();
        // Closing the FileInputStream.
        in.close();
        // Repsonding with code 200 and bytes length.
        exchange.sendResponseHeaders(200, bytes.length);
        // Writing bytes (file) to the response.
        exchange.getResponseBody().write(bytes);
        // Closing the response.
        exchange.getResponseBody().close();
    }

    // NOTE: This is likely to be moved onto configuration event once available. (1.20.2)
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            if (file == null || hash == null) {
                plugin.getLogger().severe("Could not send resourcepack as it seems to be either non-existent or defined improperly.");
                return;
            }
            plugin.getBedrockScheduler().run(1L, (task) -> event.getPlayer().setResourcePack(
                    "http://" + PluginConfig.RESOURCE_PACK_PUBLIC_ACCESS_ADDRESS + ":" + PluginConfig.RESOURCE_PACK_PORT + "/" + token,
                    hash,
                    PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                    PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
            ));
        }
    }

}
