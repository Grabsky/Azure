package cloud.grabsky.azure.world;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class WorldConfiguration {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull NamespacedKey key;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull World.Environment environment;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull WorldType type;

    @Getter(AccessLevel.PUBLIC)
    private final @Nullable String generator;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Long seed;

    @Getter(AccessLevel.PUBLIC)
    private final boolean autoLoad;

}
