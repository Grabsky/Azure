package cloud.grabsky.azure.world;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.world.WorldManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Stream;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static org.bukkit.NamespacedKey.minecraft;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class AzureWorldManager implements WorldManager {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Azure plugin;

    private static final String AUTO_LOAD_FILE_NAME = ".setAutoLoadEnabled";

    public @Nullable World createWorld(
            final @NotNull NamespacedKey key,
            final @NotNull World.Environment environment,
            final @NotNull WorldType type,
            final @Nullable Long seed
    ) throws IOException, IllegalStateException {
        final File worldDir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        // Throwing an exception in case world already exists.
        if (worldDir.exists() == true)
            throw new IllegalStateException("WORLD_ALREADY_EXISTS");
        // Creating new WorldCreator instance with initial values.
        final WorldCreator creator = new WorldCreator(key)
                .environment(environment)
                .type(type);
        // Specifying seed, if provided.
        if (seed != null)
            creator.seed(seed);
        // Creating the world.
        final World world = Bukkit.createWorld(creator);
        // Making world load automatically on server startup.
        if (new File(worldDir, AUTO_LOAD_FILE_NAME).createNewFile() == false)
            plugin.getLogger().warning("World " + key + " is already enabled.");
        // Returning newly created World object.
        return world;
    }

    public @Nullable World loadWorld(final @NotNull NamespacedKey key, final boolean remember) throws IOException, IllegalStateException {
        final File dir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        // Throwing an exception in case world with such name does not exist.
        if (dir.exists() == false)
            throw new IllegalStateException("WORLD_DOES_NOT_EXIST");
        // Making world load automatically on server startup, if enabled.
        if (remember == true)
            new File(dir, AUTO_LOAD_FILE_NAME).createNewFile();
        // Loading and returning the World object.
        return new WorldCreator(key).createWorld();
    }

    public boolean unloadWorld(final @NotNull World world, final boolean remember) {
        // Making world NOT load automatically on server startup, if enabled.
        if (remember == true)
            new File(world.getWorldFolder(), AUTO_LOAD_FILE_NAME).delete();
        // Returning 'true' if unloading was successful.
        return plugin.getServer().unloadWorld(world, true);
    }

    public void loadWorlds() throws IOException, IllegalStateException {
        final File[] dirs = requirePresent(plugin.getServer().getWorldContainer().listFiles(), new File[0]);
        // Streaming over all world directories and loading corresponding worlds, if AUTO_LOAD_FILE_NAME file is present.
        Stream.of(dirs).filter(File::isDirectory).filter(dir -> new File(dir, AUTO_LOAD_FILE_NAME).exists() == true).forEach(worldDir -> {
            final NamespacedKey key = minecraft(worldDir.getName());
            // Creating an existing world simply loads it from the disk.
            new WorldCreator(key).createWorld();
        });
    }

    private static final NamespacedKey SPAWN_POINT = new NamespacedKey("azure", "spawn_point");

    public boolean getAutoLoad(final @NotNull World world) {
        return new File(world.getWorldFolder(), AUTO_LOAD_FILE_NAME).exists();
    }

    public boolean setAutoLoad(final @NotNull World world, final boolean state) throws IOException {
        final File file = new File(world.getWorldFolder(), AUTO_LOAD_FILE_NAME);
        // ...
        if (state == true && file.exists() == false)
            return file.createNewFile();
        else if (file.exists() == true)
            return file.delete();
        // ...
        return false;
    }

    public void setSpawnPoint(final @NotNull World world, final @NotNull Location location) {
        world.getPersistentDataContainer().set(SPAWN_POINT, WorldManager.Type.ofLocation(SPAWN_POINT), location);
    }

    @Override
    public @NotNull Location getSpawnPoint(final @NotNull World world) {
        return world.getPersistentDataContainer().getOrDefault(SPAWN_POINT, WorldManager.Type.ofLocation(SPAWN_POINT), world.getSpawnLocation());
    }

}
