package cloud.grabsky.azure.api.user;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.bedrock.util.Interval;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * {@link User} object stores some player information.
 */
public interface User {

    @NotNull String getName();

    @NotNull UUID getUniqueId();

    @NotNull String getTextures();

    @NotNull String getLastAddress();

    @NotNull String getLastCountryCode();

    @Nullable Punishment getMostRecentBan();

    default boolean isBanned() {
        return this.getMostRecentBan() != null && (this.getMostRecentBan().isPermanent() == true || this.getMostRecentBan().isActive() == true);
    }

    @Nullable Punishment getMostRecentMute();

    default boolean isMuted() {
        return this.getMostRecentMute() != null && (this.getMostRecentMute().isPermanent() == true || this.getMostRecentMute().isActive() == true);
    }

    default @Nullable Player toPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

    default @Nullable OfflinePlayer toOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer);

    void unban(final @Nullable String issuer);

    @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer);

    void unmute(final @Nullable String issuer);

    default CompoundBinaryTag getPlayerData() throws IOException {
        final File file = new File(new File(AzureProvider.getAPI().getWorldManager().getPrimaryWorld().getWorldFolder(), "playerdata"), getUniqueId() + ".dat");
        // ...
        return BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP); // Should be automatically closed.
    }

}
