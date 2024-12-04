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
        // Returning null for null names.
        if (name == null)
            return null;
        // Iterating over all enum constants.
        for (final T en : clazz.getEnumConstants())
            // Returning enum if matches the name.
            if (en.toString().equalsIgnoreCase(name) == true)
                return en;
        // Returning null in case no enum was found.
        return null;
    }

}
