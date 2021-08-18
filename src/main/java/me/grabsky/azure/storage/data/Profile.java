package me.grabsky.azure.storage.data;

import java.util.UUID;

public class Profile {
    private UUID uuid;
    private String skinValue;
    private String[] nameHistory;

    public Profile(UUID uuid, String skinValue, String[] nameHistory) {
        this.uuid = uuid;
        this.skinValue = skinValue;
        this.nameHistory = nameHistory;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String[] getNameHistory() {
        return nameHistory;
    }

    public String getSkinValue() {
        return skinValue;
    }
}
