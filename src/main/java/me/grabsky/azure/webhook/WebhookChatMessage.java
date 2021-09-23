package me.grabsky.azure.webhook;

import com.google.gson.JsonObject;
import me.grabsky.azure.configuration.AzureConfig;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

public class WebhookChatMessage {
    private final String username;
    private final String avatarUrl;
    private final String content;

    public WebhookChatMessage(@NotNull String username, @NotNull UUID uuid, @NotNull String content) {
        this.username = username;
        this.avatarUrl = "https://crafatar.com/avatars/" + uuid;
        this.content = content;
    }

    public void send() throws IOException {
        // Creating JsonObject and adding fields
        final JsonObject json = new JsonObject();
        json.addProperty("content", this.content);
        json.addProperty("username", this.username);
        json.addProperty("avatar_url", this.avatarUrl);
        // Opening connection
        final URL url = new URL(AzureConfig.CHAT_WEBHOOK_URL);
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // Adding json to request
        final OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes());
        stream.flush();
        stream.close();
        // Closing connection
        connection.getInputStream().close();
        connection.disconnect();
    }
}
