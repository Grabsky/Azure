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
package cloud.grabsky.azure.api;

import org.jetbrains.annotations.ApiStatus.Internal;

import lombok.AccessLevel;
import lombok.Getter;

public final class AzureProvider {

    @Getter(AccessLevel.PUBLIC)
    private static AzureAPI API = null;

    @Internal
    public static void finalize(final AzureAPI api) {
        if (API != null)
            throw new IllegalStateException("AzureAPI has already been finalized.");
        // ...
        API = api;
    }

}
