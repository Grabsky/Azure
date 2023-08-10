package cloud.grabsky.azure.configuration.adapters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

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
