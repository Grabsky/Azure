package me.grabsky.azure.storage.objects;

import com.google.gson.annotations.Expose;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class JsonPlayer extends ExpirableData {
    // Persistent
    @Expose private String country;
    @Expose private String lastAddress;
    @Expose private JsonLocation lastLocation;
    @Expose private final Map<String, JsonLocation> homes;
    // Temporary
    private UUID lastRecipient;
    private boolean socialSpy;
    private int homesLimit;

    public JsonPlayer(final String address, final String country, final JsonLocation lastLocation, final Map<String, JsonLocation> homes) {
        this.lastAddress = address;
        this.country = country;
        this.lastLocation = lastLocation;
        this.homes = homes;
        this.lastRecipient = null;
        this.socialSpy = false;
        this.homesLimit = 0;
    }

    // Returns player's country
    public String getCountry() {
        return country;
    }

    // Returns player's last IP address
    public String getLastAddress() {
        return lastAddress;
    }

    // Returns player's last location
    public JsonLocation getLastLocation() {
        return lastLocation;
    }

    // Returns list of homes
    public Set<String> getHomes() {
        return homes.keySet();
    }

    // Returns true if player has home with specified ID
    public boolean hasHome(@NotNull final String id) {
        return this.homes.containsKey(id);
    }

    // Returns home with specified ID
    public Location getHome(@NotNull final String id) {
        return this.homes.get(id).toLocation();
    }

    public UUID getLastRecipient() {
        return this.lastRecipient;
    }

    public boolean getSocialSpy() {
        return this.socialSpy;
    }

    public int getHomesLimit() {
        return homesLimit;
    }

    // Updates player's last IP address
    public void setLastAddress(@NotNull final String address) {
        this.lastAddress = address;
    }

    // Updates player's country
    public void setCountry(@NotNull final String country) {
        this.country = country;
    }

    // Updates player's last location
    public void setLastLocation(@NotNull final Location location) {
        this.lastLocation = new JsonLocation(location);
    }

    // Adds, updates or removes player's home with given name
    public void setHome(@NotNull final String name, @Nullable final Location location) {
        if (location != null) {
            homes.put(name, new JsonLocation(location));
            return;
        }
        homes.remove(name);
    }

    // Updates player's last recipient
    public void setLastRecipient(@NotNull final UUID uuid) {
        this.lastRecipient = uuid;
    }

    // Updates player's social spy mode
    public void setSocialSpy(final boolean mode) {
        this.socialSpy = mode;
    }

    // Updates player's home limit
    public void setHomesLimit(final int limit) {
        this.homesLimit = limit;
    }
}
