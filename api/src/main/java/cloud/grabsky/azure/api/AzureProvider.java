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
