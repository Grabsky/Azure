package cloud.grabsky.azure.api.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface WorldManager {

    NamespacedKey OVERWORLD = NamespacedKey.minecraft("overworld");

    /**
     * Returns default / primary world specified in {@code server.properties} configuration file.
     */
    default @UnknownNullability World getPrimaryWorld() {
        return Bukkit.getWorld(OVERWORLD); // This should never be null.
    }

    /**
     * Returns spawn location of specified world.
     */
    default @NotNull Location getSpawnPoint(final World world) {
        return world.getSpawnLocation();
    }

    final class Type {

        public static PersistentDataType<PersistentDataContainer, Location> ofLocation(final NamespacedKey key) {
            return new PersistentDataType<>() {

                @Override
                public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
                    return PersistentDataContainer.class;
                }

                @Override
                public @NotNull Class<Location> getComplexType() {
                    return Location.class;
                }

                @Override
                public @NotNull PersistentDataContainer toPrimitive(final @NotNull Location complex, final @NotNull PersistentDataAdapterContext context) {
                    final PersistentDataContainer container = context.newPersistentDataContainer();
                    // ...
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/world"), PersistentDataType.STRING, complex.getWorld().getKey().toString());
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/x"), PersistentDataType.DOUBLE, complex.x());
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/y"), PersistentDataType.DOUBLE, complex.y());
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/z"), PersistentDataType.DOUBLE, complex.z());
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/yaw"), PersistentDataType.FLOAT, complex.getYaw());
                    container.set(new NamespacedKey(key.getNamespace(), key.getKey() + "/pitch"), PersistentDataType.FLOAT, complex.getPitch());
                    // ...
                    return container;
                }

                @Override // NOTE: This may, but should never throw NPE.
                public @NotNull Location fromPrimitive(final @NotNull PersistentDataContainer primitive, final @NotNull PersistentDataAdapterContext context) {
                    final NamespacedKey worldKey = NamespacedKey.fromString(primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/world"), STRING));
                    return new Location(
                            Bukkit.getWorld(worldKey),
                            primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/x"), DOUBLE),
                            primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/y"), DOUBLE),
                            primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/z"), DOUBLE),
                            primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/yaw"), FLOAT),
                            primitive.get(new NamespacedKey(key.getNamespace(), key.getKey() + "/pitch"), FLOAT)
                    );
                }

            };
        }
    }

}
