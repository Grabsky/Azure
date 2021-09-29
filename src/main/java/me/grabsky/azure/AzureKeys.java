package me.grabsky.azure;

import org.bukkit.NamespacedKey;

public class AzureKeys {
    public static NamespacedKey CUSTOM_NAME;
    public static NamespacedKey SOCIAL_SPY;

    public AzureKeys(Azure instance) {
        CUSTOM_NAME = new NamespacedKey(instance, "customName");
        SOCIAL_SPY = new NamespacedKey(instance, "socialSpy");
    }
}
