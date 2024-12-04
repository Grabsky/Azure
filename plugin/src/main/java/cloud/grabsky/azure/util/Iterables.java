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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Iterables {

    // May be replaced with https://openjdk.org/jeps/431 (JDK 21)
    public static <T> List<T> reversed(final Collection<T> original) {
        // Creating a copy of provided collection.
        final List<T> reversed = new ArrayList<>(original);
        // Reversing using Collections#reverse utility.
        Collections.reverse(reversed);
        // Returning the reversed list.
        return reversed;
    }

}
