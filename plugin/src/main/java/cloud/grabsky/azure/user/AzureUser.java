package cloud.grabsky.azure.user;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonClass;
import com.squareup.moshi.JsonQualifier;
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

    // Defined as implementation rather than interface because we want Moshi to know what adapter to use.
    private @Nullable AzurePunishment currentBan;

    // Defined as implementation rather than interface because we want Moshi to know what adapter to use.
    private @Nullable AzurePunishment currentMute;

    @Override
    public @Nullable Punishment getCurrentBan() {
        return currentBan;
    }

    @Override
    public @Nullable Punishment getCurrentMute() {
        return currentMute;
    }

    @Override
    public @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer) {
        this.currentBan = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                (issuer != null) ? issuer : "System",
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // ...
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println(this.name + "Saving data of " + this.name + " in the background... SUCCESS = " + isSuccess);
        });
        // ...
        return currentBan;
    }

    @Override
    public @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer) {
        this.currentMute = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                (issuer != null) ? issuer : "System",
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // ...
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println(this.name + "Saving data of " + this.name + " in the background... SUCCESS = " + isSuccess);
        });
        // ...
        return currentMute;
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof AzureUser otherUser
                && name.equals(otherUser.name)
                && uniqueId.equals(otherUser.uniqueId)
                && textures.equals(otherUser.textures);
    }

}
