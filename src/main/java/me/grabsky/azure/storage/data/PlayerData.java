package me.grabsky.azure.storage.data;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private String name;
    private String customName;
    private String ipAddress;
    private String country;
    private String language;
    // Offline specific
    private long lastSeen;
    private Location lastLocation;
    // Homes
    private Map<String, Location> homes;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    // This one is used for creating data for the first time
    public PlayerData(UUID uuid, String name, String displayName, String ipAddress, String country, String language, long lastSeen, Location lastLocation) {
        this.uuid = uuid;
        this.name = name;
        this.customName = displayName;
        this.ipAddress = ipAddress;
        this.country = country;
        this.language = language;
        this.lastSeen = lastSeen;
        this.lastLocation = lastLocation;
    }

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

    public Location getLastLocation() {
        return lastLocation;
    }

    public Location getHome(String name) {
        return homes.get(name);
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

    public void setLastLocation(Location lastLocation) { this.lastLocation = lastLocation; }


    public void addHome(String name, Location location) {
        homes.put(name, location);
    }

    public boolean removeHome(String name) {
        if(homes.containsKey(name)) {
            homes.remove(name);
            return true;
        }
        return false;
    }

    public Map<String, Location> getHomes() {
        return homes;
    }



}
