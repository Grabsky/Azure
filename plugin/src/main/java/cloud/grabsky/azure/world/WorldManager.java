package cloud.grabsky.azure.world;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.Moshi;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okio.BufferedSink;
import okio.BufferedSource;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class WorldManager {

    @Getter(AccessLevel.PUBLIC)
    private final Azure plugin;

    private final Moshi moshi = new Moshi.Builder()
            .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
            .build();

    public @Nullable World createWorld(
            final @NotNull NamespacedKey key,
            final @NotNull World.Environment environment,
            final @NotNull WorldType type,
            final @Nullable String generator,
            final @Nullable Long seed,
            final boolean autoLoad
    ) throws IOException, IllegalStateException {
        final File worldDir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        final File configurationFile = new File(worldDir, "azure-world.json");
        // ...
        if (configurationFile.exists() == true)
            throw new IllegalStateException("World " + key.asString() + " already exists and is configured.");
        // ...
        final WorldCreator creator = new WorldCreator(key);
        // ...
        creator.environment(environment);
        creator.type(type);
        creator.generator(generator);
        if (seed != null)
            creator.seed(seed);
        // ...
        final World world = Bukkit.createWorld(creator);
        // ...
        final BufferedSink buffer = buffer(sink(configurationFile));
        // ...
        final WorldConfiguration configuration = new WorldConfiguration(key, environment, type, generator, creator.seed(), autoLoad);
        // ...
        moshi.adapter(WorldConfiguration.class).indent("  ").toJson(buffer, configuration);
        // ...
        buffer.close();
        // ...
        return world;
    }

    public @Nullable World loadWorld(final @NotNull NamespacedKey key, final boolean force) throws IOException, IllegalStateException {
        final File dir = new File(plugin.getServer().getWorldContainer(), key.getKey());
        // ...
        if (dir.exists() == false)
            throw new IllegalStateException("World directory does not exist.");
        // ...
        final File file = new File(dir, "azure-world.json");
        // ...
        if (file.exists() == false)
            throw new IllegalStateException("No configuration found for this world.");
        // ...
        final BufferedSource buffer = buffer(source(file));
        // ...
        final WorldConfiguration configuration = moshi.adapter(WorldConfiguration.class).fromJson(buffer);
        // ...
        if (configuration == null)
            throw new IllegalStateException("Parsing of " + file.getPath() + " failed: " + null);
        // ...
        if (configuration.isAutoLoad() == false && force == false)
            return null;
        // ...
        final WorldCreator creator = new WorldCreator(key);
        // ...
        creator.environment(configuration.getEnvironment());
        creator.type(configuration.getType());
        creator.generator(configuration.getGenerator());
        creator.seed(configuration.getSeed());
        // ...
        return creator.createWorld();
    }

    public void loadWorlds() throws IOException, IllegalStateException {
        final File[] dirs = plugin.getServer().getWorldContainer().listFiles();
        // ...
        if (dirs == null)
            throw new IllegalStateException("Directories list is null.");
        // ...
        final List<File> files = Stream.of(dirs).filter(File::isDirectory).filter(dir -> new File(dir, "azure-world.json").exists() == true).toList();
        // ...
        for (final File worldDir : files) {
            final NamespacedKey key = NamespacedKey.minecraft(worldDir.getName());
            // ...
            this.loadWorld(key, false);
        }
    }

}
