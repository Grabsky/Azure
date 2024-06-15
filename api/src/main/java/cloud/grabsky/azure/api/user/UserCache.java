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
package cloud.grabsky.azure.api.user;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

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

}
