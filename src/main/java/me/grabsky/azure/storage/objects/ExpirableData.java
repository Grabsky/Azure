package me.grabsky.azure.storage.objects;

public abstract class ExpirableData {
    private long expiresAt = -1;

    public void setExpiresAt(final long timestamp) {
        this.expiresAt = timestamp;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
