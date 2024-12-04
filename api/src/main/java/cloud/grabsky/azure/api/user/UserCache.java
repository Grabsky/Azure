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
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @Nullable User getUser(final @NotNull UUID uniqueId);

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @Nullable User getUser(final @NotNull String name);

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @NotNull User getUser(final @NotNull Player player);

    /**
     * Returns {@code true} if user with provided {@link UUID} (uniqueId) is currently in cache.
     */
    boolean hasUser(final @NotNull UUID uniqueId);

    /**
     * Returns {@code true} if user with provided {@link String} (name) is currently in cache.
     */
    boolean hasUser(final @NotNull String name);

    @Internal
    default <T extends UserCache> T as(final Class<T> clazz) {
        return (T) this;
    }

}
