package net.skydistrict.azure.manager;

import net.skydistrict.azure.config.Config;

import java.util.UUID;

public class TeleportRequest {
    private final UUID target;
    private final long expirationDate;

    public TeleportRequest(UUID target) {
        this.target = target;
        this.expirationDate = System.currentTimeMillis() + Config.TELEPORT_REQUEST_LIFESPAN;
    }

    public UUID getTarget() {
        return target;
    }

    public long getExpirationDate() {
        return expirationDate;
    }
}
