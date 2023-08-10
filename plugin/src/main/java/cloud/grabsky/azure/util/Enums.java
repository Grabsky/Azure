package cloud.grabsky.azure.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Enums {

    public static <T extends Enum<?>> @Nullable T findMatching(final @NotNull Class<T> clazz, final @Nullable String name) throws IllegalArgumentException {
        if (clazz.isEnum() == false)
            throw new IllegalArgumentException(clazz.getName() + " is not an enum.");
        // Returning false if provided name is null.
        if (name == null)
            return null;
        // Iterating over all enum constants.
        for (final var en : clazz.getEnumConstants())
            // Returning true if enum name is equal to provided name. Case insensitive.
            if (en.toString().equalsIgnoreCase(name) == true)
                return en;
        // Returning false otherwise.
        return null;
    }

}
