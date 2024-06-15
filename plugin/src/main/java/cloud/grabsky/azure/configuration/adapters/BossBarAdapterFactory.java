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
