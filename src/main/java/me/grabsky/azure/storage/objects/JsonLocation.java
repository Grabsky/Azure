package me.grabsky.azure.storage.objects;

import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class JsonLocation {
    @Expose private final String world;
    @Expose private final float x;
    @Expose private final float y;
    @Expose private final float z;
    @Expose private final float yaw;
    @Expose private final float pitch;
    private final Location location;

    public JsonLocation(final Location location) {
        this.world = location.getWorld().getName();
        this.x = (float) location.getX();
        this.y = (float) location.getY();
        this.z = (float) location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.location = location;
    }

    public JsonLocation(String world, float x, float y, float z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.location = (Bukkit.getWorld(world) != null) ? new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch) : null;
    }

    public Location toLocation() {
        return location;
    }
}
