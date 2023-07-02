package cloud.grabsky.azure.user;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class AzureUser implements User {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull UUID uniqueId;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String textures;

    // Defined as implementation rather than interface because we want Moshi to know what adapter to use.
    private @Nullable AzurePunishment mostRecentBan;

    // Defined as implementation rather than interface because we want Moshi to know what adapter to use.
    private @Nullable AzurePunishment mostRecentMute;

    @Override
    public @Nullable Punishment getMostRecentBan() {
        return mostRecentBan;
    }

    @Override
    public @Nullable Punishment getMostRecentMute() {
        return mostRecentMute;
    }

    @Override
    public @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer) {
        // Overriding previous punishment with a new one.
        this.mostRecentBan = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                (issuer != null) ? issuer : "SYSTEM",
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println("Saving data of " + this.name + " in the background... SUCCESS = " + ((isSuccess == true) ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("BAN | Target: " + this.getName() + " (" + this.getUniqueId() + ") | Duration: " + ((mostRecentBan.isPermantent() == false) ? duration : "PERMANENT") + " | Issuer: " + issuer);
        // Returning new (and now current) punishment.
        return mostRecentBan;
    }

    @Override
    public void unban(final @Nullable String issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentBan = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println("Saving data of " + this.name + " in the background... SUCCESS = " + ((isSuccess == true) ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("UNBAN | Target: " + this.getName() + " (" + this.getUniqueId() + ") | Issuer: " + issuer);
    }

    @Override
    public @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @Nullable String issuer) {
        // Overriding previous punishment with a new one.
        this.mostRecentMute = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                (issuer != null) ? issuer : "SYSTEM",
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println("Saving data of " + this.name + " in the background... SUCCESS = " + ((isSuccess == true) ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("MUTE | Target: " + this.getName() + " (" + this.getUniqueId() + ") | Duration: " + ((mostRecentMute.isPermantent() == false) ? duration : "PERMANENT") + " | Issuer: " + issuer);
        // Returning new (and now current) punishment.
        return mostRecentMute;
    }

    @Override
    public void unmute(final @Nullable String issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentMute = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            System.out.println("Saving data of " + this.name + " in the background... SUCCESS = " + ((isSuccess == true) ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("UNMUTE | Target: " + this.getName() + " (" + this.getUniqueId() + ") | Issuer: " + issuer);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof AzureUser otherUser
                && name.equals(otherUser.name)
                && uniqueId.equals(otherUser.uniqueId)
                && textures.equals(otherUser.textures);
    }

}
