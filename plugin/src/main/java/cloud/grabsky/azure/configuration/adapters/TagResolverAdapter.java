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

import cloud.grabsky.bedrock.components.ComponentBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagResolverAdapter extends JsonAdapter<TagResolver> {

    public static final TagResolverAdapter INSTANCE = new TagResolverAdapter();

    @Override
    public TagResolver fromJson(final @NotNull JsonReader in) throws IOException {
        // Creating new (empty) instance of TagResolver.Builder which will then be populated with resolvers specified in the array.
        final TagResolver.Builder builder = TagResolver.builder();
        // Beginning JSON array.
        in.beginArray();
        // Iterating over elements inside the JSON array.
        while (in.hasNext() != false) {
            final String nextString = in.nextString().toLowerCase();
            // Getting TagResolver from specified value.
            final @Nullable TagResolver resolver = getResolverFromName(nextString);
            // Throwing exception if TagResolver ended up being null, meaning unexpected value has been provided.
            if (resolver == null)
                throw new JsonDataException("Expected " + TagResolver.class.getName() + " at " + in.getPath() + " but found: " + nextString);
            // Otherwise, adding TagResolver to the builder.
            builder.resolver(resolver);
        }
        // Ending JSON array.
        in.endArray();
        // Building and returning TagResolver object.
        return builder.build();
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable TagResolver value) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

    /**
     * Returns instance of {@link TagResolver} from provided {@code name}, or {@code null} if not found.
     */
    private static @Nullable TagResolver getResolverFromName(final @NotNull String name) {
        return switch (name) {
            case "click"        -> StandardTags.clickEvent();
            case "color"        -> StandardTags.color();
            case "decorations"  -> StandardTags.decorations();
            case "font"         -> StandardTags.font();
            case "gradient"     -> StandardTags.gradient();
            case "hover"        -> StandardTags.hoverEvent();
            case "insertion"    -> StandardTags.insertion();
            case "keybind"      -> StandardTags.keybind();
            case "newline"      -> StandardTags.newline();
            case "rainbow"      -> StandardTags.rainbow();
            case "reset"        -> StandardTags.reset();
            case "selector"     -> StandardTags.selector();
            case "transition"   -> StandardTags.transition();
            case "translatable" -> StandardTags.translatable();
            // CUSTOM
            case "item"         -> Placeholder.component("item", ComponentBuilder.EMPTY); // NOTE: Dummy placeholder.
            // Anything unspecified above is null.
            default -> null;
        };
    }

}
