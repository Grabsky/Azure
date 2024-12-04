/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure.api.user;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.bedrock.util.Interval;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link User} object stores some player information.
 */
public interface User {

    /**
     * Returns last known, cached name of this {@link User}.
     */
    @NotNull String getName();

    /**
     * Returns unique identifier {@link UUID} of this {@link User}.
     */
    @NotNull UUID getUniqueId();

    /**
     * Returns Discord identifier of this {@link User}.
     */
    @Nullable String getDiscordId();

    /**
     * Sets Discord identifier of this {@link User}.
     */
    void setDiscordId(final @Nullable String identifier);

    /**
     * Returns display name of this {@link User}.
     */
    @Nullable String getDisplayName();

    /**
     * Sets display name of this {@link User}.
     */
    void setDisplayName(final @Nullable String displayName, final @Nullable Component displayNameComponent);

    /**
     * Returns skin textures of this {@link User} encoded with Base64.
     */
    @NotNull String getTextures();

    /**
     * Returns last known, cached ip address this {@link User} used to connect to the server.
     */
    @NotNull String getLastAddress();

    /**
     * Returns last known, cached country code of this {@link User}.
     */
    @NotNull String getLastCountryCode();

    /**
     * Returns maximum level this {@link User} had.
     */
    int getMaxLevel();

    /**
     * Returns {@code true} if this {@link User} is currently vanished.
     */
    boolean isVanished();

    /**
     * Returns {@code true} if this {@link User} is currently spying.
     */
    boolean isSpying();

    /**
     * Changes vanish state of this {@link User}. This also hides the player, changes gamemode, shows {@link net.kyori.adventure.bossbar.BossBar} etc.
     */
    default void setVanished(final boolean state) {
        setVanished(state, true);
    }

    /**
     * Changes vanish state of this {@link User}. This also hides the player, shows {@link net.kyori.adventure.bossbar.BossBar} etc.
     */
    void setVanished(final boolean state, final boolean updateGamemodeWhenDisabling);

    /**
     * Returns most recent ban {@link Punishment} of this {@link User}.
     */
    @Nullable Punishment getMostRecentBan();

    /**
     * Returns {@code true} if this {@link User} is currently banned.
     */
    default boolean isBanned() {
        return this.getMostRecentBan() != null && (this.getMostRecentBan().isPermanent() == true || this.getMostRecentBan().isActive() == true);
    }

    /**
     * Returns most recent mute {@link Punishment} of this {@link User}.
     */
    @Nullable Punishment getMostRecentMute();

    /**
     * Returns {@code true} if this {@link User} is currently muted.
     */
    default boolean isMuted() {
        return this.getMostRecentMute() != null && (this.getMostRecentMute().isPermanent() == true || this.getMostRecentMute().isActive() == true);
    }

    /**
     * Bans this {@link User} for specified duration of time or permanently in case {@code null} duration is provided.
     */
    @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @NotNull CommandSender issuer);

    /**
     * Removes current ban {@link Punishment} of this {@link User}.
     */
    void unban(final @NotNull CommandSender issuer);

    /**
     * Mutes this {@link User} for specified duration of time or permanently in case {@code null} duration is provided.
     */
    @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @NotNull CommandSender issuer);

    /**
     * Removes current mute {@link Punishment} of this {@link User}.
     */
    void unmute(final @NotNull CommandSender issuer);

    /**
     * Returns {@link Player} object obtained using unique identifier of this {@link User}.
     */
    default @Nullable Player toPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

    /**
     * Returns {@link OfflinePlayer} object obtained using unique identifier of this {@link User}.
     */
    default @NotNull OfflinePlayer toOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    /**
     * Returns {@link CompoundBinaryTag} object from contents of [_PRIMARY_WORLD_]/playerdata/[_UUID_].dat file.
     */
    default CompoundBinaryTag getPlayerData() throws IOException {
        final File file = new File(new File(AzureProvider.getAPI().getWorldManager().getPrimaryWorld().getWorldFolder(), "playerdata"), getUniqueId() + ".dat");
        // ...
        return BinaryTagIO.reader().read(file.toPath(), BinaryTagIO.Compression.GZIP); // Should be automatically closed.
    }

}
