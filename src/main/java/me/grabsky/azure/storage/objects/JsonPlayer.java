package me.grabsky.azure.storage.objects;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;

public class JsonPlayer {
    private UUID uuid;
    private String name;
    private String customName;
    private String ipAddress;
    private String country;
    private String language;
    // Offline specific
    private long lastSeen;
    private JsonLocation lastLocation;
    // Homes
    private Map<String, Location> homes;

    public JsonPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    // This one is used for creating data for the first time
    public JsonPlayer(UUID uuid, String name, String displayName, String ipAddress, String country, String language, long lastSeen, JsonLocation lastLocation) {
        this.uuid = uuid;
        this.name = name;
        this.customName = displayName;
        this.ipAddress = ipAddress;
        this.country = country;
        this.language = language;
        this.lastSeen = lastSeen;
        this.lastLocation = lastLocation;
    }

    // Getters
    public UUID getUUID() {
        return uuid;
    }
    public String getName() {
        return name;
    }
    public String getCustomName() {
        return customName;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public String getCountry() {
        return country;
    }
    public String getLanguage() {
        return language;
    }
    public long getLastSeen() {
        return lastSeen;
    }
    public JsonLocation getLastLocation() {
        return lastLocation;
    }

    // Setters
    public String setName() {
        return name;
    }
    public void setCustomName(String displayName) {
        this.customName = displayName;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    public void setLastLocation(JsonLocation lastLocation) { this.lastLocation = lastLocation; }
}
