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
package cloud.grabsky.azure.configuration.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UUIDAdapter extends JsonAdapter<UUID> {
    public static final UUIDAdapter INSTANCE = new UUIDAdapter();

    @Override
    public UUID fromJson(final @NotNull JsonReader in) throws IOException {
        try {
            return UUID.fromString(in.nextString());
        } catch (final IllegalArgumentException e) {
            throw new JsonDataException(e);
        }
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable UUID uuid) throws IOException {
        out.value((uuid != null) ? uuid.toString() : null);
    }

}
