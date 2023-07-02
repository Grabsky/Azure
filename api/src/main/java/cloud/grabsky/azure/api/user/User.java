package cloud.grabsky.azure.api.user;

import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.bedrock.util.Interval;
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

    @Nullable Punishment getMostRecentBan();

    default boolean isBanned() {
        return this.getMostRecentBan() != null && (this.getMostRecentBan().isPermantent() == true || this.getMostRecentBan().isActive() == true);
    }

    @Nullable Punishment getMostRecentMute();

    default boolean isMuted() {
        return this.getMostRecentMute() != null && (this.getMostRecentMute().isPermantent() == true || this.getMostRecentMute().isActive() == true);
    }

    default @Nullable Player toPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

    @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer);

    void unban(final @Nullable String issuer);

    @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer);

    void unmute(final @Nullable String issuer);

}
