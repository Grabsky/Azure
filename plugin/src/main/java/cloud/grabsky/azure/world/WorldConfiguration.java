package cloud.grabsky.azure.world;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class WorldConfiguration {

    @Getter(AccessLevel.PUBLIC)
    private final NamespacedKey key;

    @Getter(AccessLevel.PUBLIC)
    private final World.Environment environment;

    @Getter(AccessLevel.PUBLIC)
    private final WorldType type;

    @Getter(AccessLevel.PUBLIC)
    private final String generator;

    @Getter(AccessLevel.PUBLIC)
    private final boolean autoLoad;

}
