package cloud.grabsky.azure.api.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * {@link User} object stores some player information.
 */
public interface User {

    @NotNull String getName();

    @NotNull UUID getUniqueId();

    @NotNull String getTextures();

    default @Nullable Player toPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

}
