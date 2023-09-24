package cloud.grabsky.azure.resourcepack;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import lombok.SneakyThrows;

public final class ResourcePackManager implements Listener, HttpHandler {

    private final Azure plugin;
    private final Optional<String> hash;

    public static @Nullable File PUBLIC_FILE;

    public ResourcePackManager(final @NotNull Azure plugin) {
        this.plugin = plugin;
        this.hash = calculateHash();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true && PluginConfig.RESOURCE_PACK_FILE) {
            plugin.getBedrockScheduler().run(1L, (task) -> player.setResourcePack(
                    PluginConfig.RESOURCE_PACK_URL,
                    PluginConfig.RESOURCE_PACK_HASH,
                    PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                    PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
            ));
        }
    }

    private static @Nullable String calculateHash() throws NoSuchAlgorithmException, IOException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final InputStream stream = new FileInputStream(PluginConfig.RESOURCE_PACK_FILE);

        Files.asByteSource(PUBLIC_FILE).hash()
        Hashing.sha1().hashBytes(stream.readAllBytes()).toString();

        final byte[] buffer = new byte[8192];

        int bytesRead = stream.read(buffer);

        while (bytesRead != -1) {
            digest.update(buffer, 0, bytesRead);
            // ...
            bytesRead = stream.read(buffer);
        }

        stream.close();

        final StringBuilder hexString = new StringBuilder();
        for (final byte b : digest.digest()) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    public void listen(final int port) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(PluginConfig.RESOURCE_PACK_PORT), 0);
        server.createContext("/download", this);
    }

    @Override
    public void handle(final @NotNull HttpExchange exchange) throws IOException {
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

}
