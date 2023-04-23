package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AzureUser implements User {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull UUID uniqueId;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String textures;

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof AzureUser otherUser
                && name.equals(otherUser.name)
                && uniqueId.equals(otherUser.uniqueId)
                && textures.equals(otherUser.textures);
    }

}
