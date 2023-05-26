package cloud.grabsky.azure.configuration.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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
