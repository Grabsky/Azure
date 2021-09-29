package me.grabsky.azure.storage.objects;

import com.google.gson.annotations.Expose;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class JsonPlayer extends ExpirableData {
    @Expose private String country;
    @Expose private String lastAddress;
    @Expose private JsonLocation lastLocation;
    @Expose private final Map<String, JsonLocation> homes;
    private UUID lastRecipient;
    private boolean socialSpy;

    // This one is used for creating data for the first time
    public JsonPlayer(final String address, final String country, final JsonLocation lastLocation, final Map<String, JsonLocation> homes) {
        this.country = country;
        this.lastAddress = address;
        this.lastLocation = lastLocation;
        this.homes = homes;
        this.lastLocation = null;
        this.socialSpy = false;
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
    public boolean hasHome(final String id) {
        return this.homes.containsKey(id);
    }

    // Returns home with specified ID
    public Location getHome(final String id) {
        return this.homes.get(id).toLocation();
    }

    public UUID getLastRecipient() {
        return this.lastRecipient;
    }

    public boolean getSocialSpy() {
        return this.socialSpy;
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
        } else {
            homes.remove(name);
        }
    }

    // Updates player's last recipient
    public void setLastRecipient(final UUID uuid) {
        this.lastRecipient = uuid;
    }

    // Updates player's social spy mode
    public void setSocialSpy(final boolean mode) {
        this.socialSpy = mode;
    }
}
