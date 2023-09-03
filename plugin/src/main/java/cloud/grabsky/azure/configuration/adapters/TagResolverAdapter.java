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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagResolverAdapter extends JsonAdapter<TagResolver> {

    public static final TagResolverAdapter INSTANCE = new TagResolverAdapter();

    @Override
    public TagResolver fromJson(final @NotNull JsonReader in) throws IOException {
        final TagResolver.Builder builder = TagResolver.builder();
        // ...
        in.beginArray();
        while (in.hasNext() != false) {
            final String nextString = in.nextString().toLowerCase();
            switch (nextString) {
                case "click" -> builder.resolver(StandardTags.clickEvent());
                case "color" -> builder.resolver(StandardTags.color());
                case "decorations" -> builder.resolver(StandardTags.decorations());
                case "font" -> builder.resolver(StandardTags.font());
                case "gradient" -> builder.resolver(StandardTags.gradient());
                case "hover" -> builder.resolver(StandardTags.hoverEvent());
                case "insertion" -> builder.resolver(StandardTags.insertion());
                case "keybind" -> builder.resolver(StandardTags.keybind());
                case "newline" -> builder.resolver(StandardTags.newline());
                case "rainbow" -> builder.resolver(StandardTags.rainbow());
                case "reset" -> builder.resolver(StandardTags.reset());
                case "selector" -> builder.resolver(StandardTags.selector());
                case "transition" -> builder.resolver(StandardTags.transition());
                case "translatable" -> builder.resolver(StandardTags.translatable());
                // Custom tags.
                case "item" -> builder.resolver(Placeholder.component("item", Component.empty())); // NOTE: Dummy placeholder.
                default -> throw new JsonDataException("Expected " + TagResolver.class.getName() + " at " + in.getPath() + " but found: " + nextString);
            }
        }
        in.endArray();
        // ...
        return builder.build();
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable TagResolver value) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

}
