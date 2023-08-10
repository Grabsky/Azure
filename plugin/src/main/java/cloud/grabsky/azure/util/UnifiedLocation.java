package cloud.grabsky.azure.util;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnifiedLocation {

    private static final NamespacedKey KEY_X = new NamespacedKey("location", "x");
    private static final NamespacedKey KEY_Y = new NamespacedKey("location", "y");
    private static final NamespacedKey KEY_Z = new NamespacedKey("location", "z");
    private static final NamespacedKey KEY_YAW = new NamespacedKey("location", "yaw");
    private static final NamespacedKey KEY_PITCH = new NamespacedKey("location", "pitch");

    @Getter(AccessLevel.PUBLIC)
    private final double x;

    @Getter(AccessLevel.PUBLIC)
    private final double y;

    @Getter(AccessLevel.PUBLIC)
    private final double z;

    @Getter(AccessLevel.PUBLIC)
    private final float yaw;

    @Getter(AccessLevel.PUBLIC)
    private final float pitch;

    public static @NotNull UnifiedLocation fromLocation(final @NotNull Location location) {
        return new UnifiedLocation(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
    }

    public static @NotNull UnifiedLocation fromContainer(final @NotNull PersistentDataContainer container) throws IllegalArgumentException {
        final @Nullable Double x = container.get(KEY_X, org.bukkit.persistence.PersistentDataType.DOUBLE);
        final @Nullable Double y = container.get(KEY_Y, org.bukkit.persistence.PersistentDataType.DOUBLE);
        final @Nullable Double z = container.get(KEY_Z, org.bukkit.persistence.PersistentDataType.DOUBLE);
        final @Nullable Float yaw = container.get(KEY_YAW, org.bukkit.persistence.PersistentDataType.FLOAT);
        final @Nullable Float pitch = container.get(KEY_PITCH, org.bukkit.persistence.PersistentDataType.FLOAT);

        if (x == null || y == null || z == null || yaw == null || pitch == null)
            throw new IllegalArgumentException("Cannot create UnifiedLocation because one or more values are null.");

        return new UnifiedLocation(x, y, z, yaw, pitch);
    }

    public static @NotNull UnifiedLocation fromCompound(final @NotNull CompoundBinaryTag compound) {
        final double x = compound.getDouble(KEY_X.getKey(), Double.MAX_VALUE);
        final double y = compound.getDouble(KEY_Y.getKey(), Double.MAX_VALUE);
        final double z = compound.getDouble(KEY_Z.getKey(), Double.MAX_VALUE);
        final float yaw = compound.getFloat(KEY_YAW.getKey(), Float.MAX_VALUE);
        final float pitch = compound.getFloat(KEY_PITCH.getKey(), Float.MAX_VALUE);

        if (x == Double.MAX_VALUE || y == Double.MAX_VALUE || z == Double.MAX_VALUE || yaw == Float.MAX_VALUE || pitch == Float.MAX_VALUE)
            throw new IllegalArgumentException("Cannot create UnifiedLocation because one or more values are null.");

        return new UnifiedLocation(x, y, z, yaw, pitch);
    }

    public @NotNull Location toBukkitLocation(final @NotNull World world) {
        return new Location(world,x, y, z, yaw, pitch);
    }

    public static PersistentDataType<PersistentDataContainer, UnifiedLocation> PERSISTENT_DATA_TYPE = new PersistentDataType<>() {

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<UnifiedLocation> getComplexType() {
            return UnifiedLocation.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(final @NotNull UnifiedLocation complex, final @NotNull PersistentDataAdapterContext context) {
            final PersistentDataContainer container = context.newPersistentDataContainer();

            container.set(KEY_X, PersistentDataType.DOUBLE, complex.x);
            container.set(KEY_Y, PersistentDataType.DOUBLE, complex.y);
            container.set(KEY_Z, PersistentDataType.DOUBLE, complex.z);
            container.set(KEY_YAW, PersistentDataType.FLOAT, complex.yaw);
            container.set(KEY_PITCH, PersistentDataType.FLOAT, complex.pitch);

            return container;
        }

        @Override
        public @NotNull UnifiedLocation fromPrimitive(final @NotNull PersistentDataContainer primitive, final @NotNull PersistentDataAdapterContext context) {
            return UnifiedLocation.fromContainer(primitive);
        }

    };

}
