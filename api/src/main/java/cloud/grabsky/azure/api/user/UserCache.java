package cloud.grabsky.azure.api.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * {@link UserCache} is repsonsible for caching some information about players that have joined the server.
 */
public interface UserCache {

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @Nullable User getUser(final @NotNull UUID uniqueId);

    /**
     * Returns {@link User} if currently in cache, {@code null} otherwise.
     */
    @Nullable User getUser(final @NotNull String name);

    /**
     * Returns {@code true} if user with provided {@link UUID} (uniqueId) is currently in cache.
     */
    boolean hasUser(final UUID uniqueId);

    /**
     * Returns {@code true} if user with provided {@link String} (name) is currently in cache.
     */
    boolean hasUser(final String name);

}
