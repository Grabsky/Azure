package cloud.grabsky.azure.configuration.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

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
        return new JsonAdapter<BossBar>() {

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
