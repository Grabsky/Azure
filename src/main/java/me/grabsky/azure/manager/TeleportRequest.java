package me.grabsky.azure.manager;

import java.util.UUID;

public class TeleportRequest {
    private final UUID target;
    private final long expirationDate;

    public TeleportRequest(UUID target) {
        this.target = target;
        this.expirationDate = System.currentTimeMillis() + 0; // TO-DO: Replace 0 with config option
    }

    public UUID getTarget() {
        return target;
    }

    public long getExpirationDate() {
        return expirationDate;
    }
}
