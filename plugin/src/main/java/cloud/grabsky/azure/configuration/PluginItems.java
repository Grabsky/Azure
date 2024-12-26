package cloud.grabsky.azure.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public final class PluginItems implements JsonConfiguration {

    // Items

    @JsonPath("items")
    public static Map<String, @Nullable ItemStack> ITEMS = new HashMap<>();

}
