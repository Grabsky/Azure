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
import com.squareup.moshi.Moshi;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import static com.squareup.moshi.Types.getRawType;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BossBarAdapterFactory implements JsonAdapter.Factory {
    public static final BossBarAdapterFactory INSTANCE = new BossBarAdapterFactory();

    @Override
    public @Nullable JsonAdapter<BossBar> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
        if (BossBar.class.isAssignableFrom(getRawType(type)) == false)
            return null;
        // ...
        final JsonAdapter<BossBarSurrogate> adapter = moshi.adapter(BossBarSurrogate.class);
        // ...
        return new JsonAdapter<>() {

            @Override
            public BossBar fromJson(final @NotNull JsonReader in) throws IOException {
                final BossBarSurrogate surrogate = adapter.fromJson(in);
                // ...
                if (surrogate != null)
                    return surrogate.toBossBar();
                // ...
                throw new JsonDataException("BossBar is null.");
            }

            @Override
            public void toJson(final @NotNull JsonWriter out, final @Nullable BossBar uuid) throws IOException {
                out.value((uuid != null) ? uuid.toString() : null);
            }

        };
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class BossBarSurrogate {

        private final @NotNull Component text;
        private final @NotNull BossBar.Color color;
        private final @NotNull BossBar.Overlay overlay;

        public @NotNull BossBar toBossBar() {
            return BossBar.bossBar(text, 1.0F, color, overlay);
        }

    }
}
