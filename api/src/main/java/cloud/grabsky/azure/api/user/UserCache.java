package cloud.grabsky.azure.api.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * {@link UserCache} is repsonsible for caching some information about players.
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
     * Returns {@code true} if user with provided {@link UUID} (uniqueId) is currently in cache.
     */
    boolean hasUser(final @NotNull UUID uniqueId);

    /**
     * Returns {@code true} if user with provided {@link String} (name) is currently in cache.
     */
    boolean hasUser(final @NotNull String name);

}
