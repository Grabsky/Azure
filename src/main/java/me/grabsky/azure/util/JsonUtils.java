package me.grabsky.azure.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.grabsky.azure.Azure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

// TO-DO: Two-sided ItemStack parser.
public class JsonUtils {

    // Sends JSON request to given URL
    public static JsonElement sendJsonRequest(String url, int maxRetries) {
        for (int retries = 1; retries <= maxRetries; retries++) {
            try {
                // Sending request to an API
                final URL u = new URL(url);
                final URLConnection req = u.openConnection();
                // Getting request from an API and store it
                final BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
                final JsonParser jp = new JsonParser();
                // Convert the input stream to a json element
                final JsonElement el = jp.parse(new InputStreamReader((InputStream) req.getContent()));
                // Close connection
                in.close();
                // Return root JsonObject
                return el;
            } catch (IOException e) {
                Azure.getInstance().getConsoleLogger().error(ChatColor.RED + "An error occurred while trying to send request to '" + url + "'... retrying...");
            }
        }
        return null;
    }

    // Parses Location to a JsonObject
    public static JsonObject parseLocation(Location location) {
        JsonObject object = new JsonObject();
        object.addProperty("world", location.getWorld().toString());
        object.addProperty("x", location.getX());
        object.addProperty("y", location.getY());
        object.addProperty("z", location.getZ());
        object.addProperty("yaw", location.getYaw());
        object.addProperty("pitch", location.getPitch());
        return object;
    }

    // Converts json string to a Location
    public static Location location(String json) {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        return new Location(
                Bukkit.getWorld(object.get("world").getAsString()),
                object.get("x").getAsDouble(),
                object.get("y").getAsDouble(),
                object.get("z").getAsDouble(),
                object.get("yaw").getAsFloat(),
                object.get("pitch").getAsFloat()
        );
    }

    public static JsonObject parseItemStack(ItemStack itemStack) {
        return new JsonObject(); // because IntelliJ keeps screaming at me
    }

    public static ItemStack itemstack(String json) {
        return new ItemStack(Material.AIR, 0); // because IntelliJ keeps screaming at me
    }

}
