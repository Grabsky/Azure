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

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * {@link UserCache} is responsible for caching some information about players.
 */
public interface UserCache {

    /**
     * Returns unmodifiable {@link Collection} of users currently held in cache.
     */
    @NotNull @Unmodifiable Collection<User> getUsers();

    /**
     * Returns unmodifiable {@link Map} of users currently held in cache.
     */
    @NotNull @Unmodifiable Map<UUID, User> getUsersMap();

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    default @Nullable User getUser(final @NotNull UUID uniqueId) {
        return this.getUsersMap().get(uniqueId);
    }

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    default @Nullable User getUser(final @NotNull String name) {
        return this.getUsers().stream().filter(user -> user.getName().equalsIgnoreCase(name) == true).findFirst().orElse(null);
    }

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @NotNull User getUser(final @NotNull Player player);

    /**
     * Returns {@code true} if user with provided {@link UUID} (uniqueId) is currently in cache.
     */
    default boolean hasUser(final @NotNull UUID uniqueId) {
        return this.getUsersMap().containsKey(uniqueId);
    }

    /**
     * Returns {@code true} if user with provided {@link String} (name) is currently in cache.
     */
    default boolean hasUser(final @NotNull String name) {
        return this.getUsers().stream().anyMatch(user -> user.getName().equalsIgnoreCase(name));
    }

    /**
     * Returns matching {@link User} for the specified discord identifier.
     */
    default @Nullable User fromDiscord(final String discordId) {
        return getUsers().stream().filter(user -> user.getDiscordId() != null && user.getDiscordId().equals(discordId) == true).findFirst().orElse(null);
    }

    @Internal
    default <T extends UserCache> T as(final Class<T> clazz) {
        return (T) this;
    }

}
