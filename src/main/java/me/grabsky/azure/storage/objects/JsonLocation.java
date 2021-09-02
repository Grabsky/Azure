package me.grabsky.azure.storage.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class JsonLocation {
    public String world;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    private Location location;

    public JsonLocation() {}

    public JsonLocation(String world, double x, double y, double z, float yaw, float pitch) {
        final World gameWorld = Bukkit.getWorld(world);
        if (gameWorld != null) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.location = new Location(gameWorld, x, y, z, yaw, pitch);
        }
    }

    public Location toLocation() {
        return location;
    }
}
