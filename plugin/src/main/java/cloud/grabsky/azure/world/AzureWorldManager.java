/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.world;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.world.WorldManager;
import cloud.grabsky.azure.util.Enums;
import cloud.grabsky.azure.util.UnifiedLocation;
import cloud.grabsky.azure.world.AzureWorldManager.WorldOperationException.Reason;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;

// NOTE: Looking forward to improve that once some better API is added to Paper.
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class AzureWorldManager implements WorldManager {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Azure plugin;

    private static final NamespacedKey KEY_SPAWN_POINT = new NamespacedKey("azure", "spawn_point");
    private static final NamespacedKey KEY_AUTO_LOAD = new NamespacedKey("azure", "auto_load");
    private static final NamespacedKey KEY_ENVIRONMENT = new NamespacedKey("azure", "environment");
    private static final NamespacedKey KEY_DESCRIPTION = new NamespacedKey("azure", "description");

    /**
     * Creates a world using specified parameters.
     */
    public @NotNull World createWorld(final @NotNull NamespacedKey key, final @NotNull World.Environment environment, final @NotNull WorldType type, final @Nullable Long seed) throws IOException, WorldOperationException {
        final File worldDir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        // Throwing an exception in case world already exists.
        if (worldDir.exists() == true)
            throw new WorldOperationException(Reason.ALREADY_EXISTS, "An error occurred while trying to load the world. Directory named " + key.value() + " already exists.");
        // Creating new WorldCreator instance with initial values.
        final WorldCreator creator = new WorldCreator(key)
                .environment(environment)
                .type(type);
        // Specifying seed, if provided.
        if (seed != null)
            creator.seed(seed);
        // Creating the world.
        final @Nullable World world = Bukkit.createWorld(creator);
        // Throwing an exception if created World object is null.
        if (world == null)
            throw new WorldOperationException(Reason.OTHER, "An error occurred while trying to import the world. Perhaps wrong timing?");
        // Setting PDC values.
        world.getPersistentDataContainer().set(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, true);
        world.getPersistentDataContainer().set(KEY_ENVIRONMENT, PersistentDataType.STRING, environment.name());
        // Saving the world. This should (hopefully) save level.dat as well.
        world.save();
        // Returning newly created World object.
        return world;
    }

    /**
     * Loads a world. This method uses world-specific properties defined inside {@code level.dat} file.
     */
    public @NotNull World loadWorld(final @NotNull NamespacedKey key) throws IOException, WorldOperationException {
        return this.loadWorld(key, true);
    }

    /**
     * Loads a world. This method uses world-specific properties defined inside {@code level.dat} file.
     *
     * @apiNote In case {@code force} is set to {@code false} and world is not marked to be automatically loaded, {@code null} is returned.
     */
    public @UnknownNullability World loadWorld(final @NotNull NamespacedKey key, final boolean force) throws IOException, WorldOperationException {
        final File dir = new File(plugin.getServer().getWorldContainer(), key.value());
        // Throwing an exception in case world with such name does not exist.
        if (dir.exists() == false)
            throw new WorldOperationException(Reason.DOES_NOT_EXIST, "An error occurred while trying to load the world. No directory named " + key.value() + " was found.");
        // Reading PDC.
        final CompoundBinaryTag compound = BinaryTagIO.reader(Long.MAX_VALUE).read(new File(dir, "level.dat").toPath(), BinaryTagIO.Compression.GZIP).getCompound("Data").getCompound("BukkitValues");
        // Skipping worlds that should not be loaded automatically.
        if (force == false && compound.getBoolean(KEY_AUTO_LOAD.asString(), false) == false)
            return null;
        // Reading environment of the World, stored in PDC.
        final @Nullable World.Environment environment = Enums.findMatching(World.Environment.class, compound.getString(KEY_ENVIRONMENT.asString()));
        // Throwing an exception if environment has not been found for that World.
        if (environment == null)
            throw new WorldOperationException(Reason.OTHER, "An error occurred while trying to load the world. Environment has not been specified. Try importing the world instead.");
        // Creating the World object.
        final @Nullable World world = new WorldCreator(key).environment(environment).createWorld();
        // Throwing an exception if created World object is null.
        if (world == null)
            throw new IllegalStateException("An error occurred while trying to import the world. Perhaps wrong timing?");
        // Setting PDC.
        world.getPersistentDataContainer().set(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, true); // Imported worlds should be automatically loaded by default.
        // Returning the World object.
        return world;
    }

    /**
     * Imports the world. When world is loaded for the first time, it's necessary to specify {@link World.Environment} it uses - Bukkit API can't guess that, apparently.
     */
    public @NotNull World importWorld(final @NotNull NamespacedKey key, final @NotNull World.Environment environment) throws WorldOperationException {
        final File dir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        // Throwing an exception in case world with such name does not exist.
        if (dir.exists() == false)
            throw new WorldOperationException(Reason.DOES_NOT_EXIST, "An error occurred while trying to load the world. No directory named " + key.value() + " was found.");
        // Returning the World object.
        final @Nullable World world = new WorldCreator(key).environment(environment).createWorld();
        // Throwing an exception if created World object is null.
        if (world == null)
            throw new WorldOperationException(Reason.OTHER, "An error occurred while trying to load the world. Environment has not been specified. Try importing the world instead.");
        // Setting PDC values.
        world.getPersistentDataContainer().set(KEY_ENVIRONMENT, PersistentDataType.STRING, environment.toString());
        world.getPersistentDataContainer().set(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, true); // Imported worlds should be automatically loaded by default.
        // Returning the World object.
        return world;
    }

    /**
     * Unloads the world. Can ocassionally fail, according to Bukkit API. This has the same effect as {@link Bukkit#unloadWorld}.
     */
    public boolean unloadWorld(final @NotNull World world) throws WorldOperationException {
        if (this.getPrimaryWorld().equals(world) == true)
            throw new WorldOperationException(Reason.PRIMARY_WORLD, "An error occurred while trying to unload a world. Default world cannot be unloaded.");
        // Moving all players to spawn of the main world.
        world.getPlayers().forEach(player -> player.teleport(this.getSpawnPoint(this.getPrimaryWorld()))); // Not async to prevent next call from failiing.
        // Setting PDC values.
        world.getPersistentDataContainer().set(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, false); // Unloaded worlds shouldn't be loaded automatically until requested.
        // Returning 'true' if unloading was successful.
        return plugin.getServer().unloadWorld(world, true);
    }

    /**
     * Deletes the world. Can ocassionally fail, according to Bukkit API.
     */
    public boolean deleteWorld(final @NotNull World world) throws WorldOperationException {
        if (this.getPrimaryWorld().equals(world) == true)
            throw new WorldOperationException(Reason.PRIMARY_WORLD, "An error occurred while trying to unload a world. Default world cannot be deleted.");
        // Unloading the world. This method also moves all players to spawn of the primary world.
        this.unloadWorld(world);
        // Deleting the directory and returning the result.
        return deleteDirectory(world.getWorldFolder());
    }

    /**
     * Loads all worlds that have auto-load enabled.
     */
    public void loadWorlds() throws IOException, WorldOperationException {
        final File[] dirs = requirePresent(plugin.getServer().getWorldContainer().listFiles(), new File[0]);
        // Streaming over all world directories and loading corresponding worlds, if AUTO_LOAD_FILE_NAME file is present.
        for (final File directory : dirs) {
            // Skipping non-worlds.
            if (new File(directory, "level.dat").exists() == false)
                continue;
            final String name = directory.getName();
            // Skipping "default" world. There may be a better way to do that but this one should work for now.
            if (name.equals(this.getPrimaryWorld().getName()) == true)
                continue;
            // Creating a key.
            final NamespacedKey key = NamespacedKey.minecraft(directory.getName());
            // Loading the world.
            this.loadWorld(key, false); // "Disabled" worlds are ignored.
        }
    }

    /**
     * Returns spawn point of specified {@link World}.
     */
    @Override
    public @NotNull Location getSpawnPoint(final @NotNull World world) {
        final @Nullable UnifiedLocation location = (world.getPersistentDataContainer().has(KEY_SPAWN_POINT) == true)
                ? world.getPersistentDataContainer().get(KEY_SPAWN_POINT, UnifiedLocation.PERSISTENT_DATA_TYPE)
                : null;
        // Returning...
        return (location != null) ? location.toBukkitLocation(world) : world.getSpawnLocation();
    }

    /**
     * Changes spawn point of specified {@link World} to provided {@link Location}.
     */
    public void setSpawnPoint(final @NotNull World world, final @NotNull Location location) {
        world.getPersistentDataContainer().set(KEY_SPAWN_POINT, UnifiedLocation.PERSISTENT_DATA_TYPE, UnifiedLocation.fromLocation(location));
    }

    /**
     * Changes auto load flag of specified {@link World}.
     */
    public void setAutoLoad(final @NotNull World world, final boolean state) {
        world.getPersistentDataContainer().set(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, state);
    }

    /**
     * Returns true if provided {@link World} should be auto loaded.
     */
    public boolean getAutoLoad(final @NotNull World world) {
        return world.getPersistentDataContainer().getOrDefault(KEY_AUTO_LOAD, PersistentDataType.BOOLEAN, false);
    }

    public void setDescription(final @NotNull World world, final @Nullable String description) {
        if (description == null || description.isBlank() == true) {
            world.getPersistentDataContainer().remove(KEY_DESCRIPTION);
            return;
        }
        // ...
        world.getPersistentDataContainer().set(KEY_DESCRIPTION, PersistentDataType.STRING, description);
    }

    public @Nullable String getDescription(final @NotNull World world) {
        return world.getPersistentDataContainer().get(KEY_DESCRIPTION, PersistentDataType.STRING);
    }

    private static boolean deleteDirectory(final File file) {
        try (final Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public static final class WorldOperationException extends IllegalStateException {

        @Getter(AccessLevel.PUBLIC)
        private final Reason reason;

        public WorldOperationException(final @NotNull Reason reason, final @NotNull String message) {
            super(message);
            // ...
            this.reason = reason;
        }

        public enum Reason {
            PRIMARY_WORLD, DOES_NOT_EXIST, ALREADY_EXISTS, OTHER
        }

    }

}
