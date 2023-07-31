package cloud.grabsky.azure.api.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface WorldManager {

    NamespacedKey OVERWORLD = NamespacedKey.minecraft("overworld");

    /**
     * Returns default / primary {@link World} specified in {@code server.properties} configuration file.
     */
    default @UnknownNullability World getPrimaryWorld() {
        return Bukkit.getWorld(OVERWORLD); // This should never be null.
    }

    /**
     * Returns spawn {@link Location} of specified world.
     */
    default @NotNull Location getSpawnPoint(final World world) {
        return world.getSpawnLocation();
    }

}
