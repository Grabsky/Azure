package me.grabsky.azure.storage.objects;

import com.google.gson.annotations.Expose;
import me.grabsky.indigo.user.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class JsonPlayer {
    @Expose private UUID uuid;
    @Expose private String customName;
    @Expose private String country;
    @Expose private String lastAddress;
    @Expose private JsonLocation lastLocation;

    // This one is used for creating data for the first time
    public JsonPlayer(@NotNull final UUID uuid, @Nullable final String customName, @NotNull final String address, @NotNull final String country, @NotNull final JsonLocation lastLocation) {
        this.uuid = uuid;
        this.customName = customName;
        this.country = country;
        this.lastAddress = address;
        this.lastLocation = lastLocation;
    }

    // Getters
    public UUID getUUID() {
        return uuid;
    }
    public String getName() {
        return UserCache.get(uuid).getName();
    }
    public String getCustomName() {
        return customName;
    }
    public String getCountry() {
        return country;
    }
    public String getLastAddress() {
        return lastAddress;
    }
    public JsonLocation getLastLocation() {
        return lastLocation;
    }

    // Setters
    public void setCustomName(@NotNull final String displayName) {
        this.customName = displayName;
    }
    public void setLastAddress(@NotNull final String address) {
        this.lastAddress = address;
    }
    public void setCountry(@NotNull final String country) {
        this.country = country;
    }
    public void setLastLocation(@NotNull final JsonLocation jsonLocation) {
        this.lastLocation = jsonLocation;
    }
}
