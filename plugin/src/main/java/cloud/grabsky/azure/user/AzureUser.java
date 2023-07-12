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
import lombok.Setter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
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

    @Getter(AccessLevel.PUBLIC)
    private @NotNull String lastAddress;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PACKAGE, onMethod = @__(@ApiStatus.Internal))
    private @NotNull String lastCountryCode;

    @Getter(AccessLevel.PUBLIC)
    private boolean isVanished;

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
    public void setVanished(final boolean state, final boolean updateGamemodeWhenDisabling) throws UnsupportedOperationException {
        final @Nullable Player thisPlayer = this.toPlayer();
        // Throwing an exception if trying to change vanish state of an offline player. Not yet supported.
        if (thisPlayer == null || thisPlayer.isOnline() == false)
            throw new UnsupportedOperationException("Player must be online to have his vanish state changed.");
        // Changing vanish state.
        this.isVanished = state;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
            // Scheduling more logic onto the main thread.
            Azure.getInstance().getBedrockScheduler().run(1L, (task) -> {
                // Executing post-actions for the "enabled" state.
                if (state == true) {
                    // Showing BossBar.
                    thisPlayer.showBossBar(PluginConfig.VANISH_BOSS_BAR);
                    // Switching game mode to spectator.
                    thisPlayer.setGameMode(GameMode.SPECTATOR);
                    // Hiding target from other players.
                    final LuckPerms luckperms = Azure.getInstance().getLuckPerms();
                    Bukkit.getOnlinePlayers().forEach(otherPlayer -> {
                        if (thisPlayer != otherPlayer) {
                            final @Nullable Group playerGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(thisPlayer).getPrimaryGroup());
                            final @Nullable Group otherGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(otherPlayer).getPrimaryGroup());
                            // Comparing group weights.
                            if (playerGroup != null && otherGroup != null && playerGroup.getWeight().orElse(0) > otherGroup.getWeight().orElse(0))
                                otherPlayer.hidePlayer(Azure.getInstance(), thisPlayer);
                        }
                    });
                // Otherwise, executing post-actions for the "disabled" state.
                } else {
                    // Hiding BossBar.
                    thisPlayer.hideBossBar(PluginConfig.VANISH_BOSS_BAR);
                    // Switching game mode to previous game mode or default, or not doing anything if 'updateGamemodeWhenDisabling' is false.
                    if (updateGamemodeWhenDisabling == true) {
                        final GameMode nextGameMode = (thisPlayer.getPreviousGameMode() != null)
                                ? (thisPlayer.hasPermission("azure.plugin.vanish_switch_previous_gamemode") == true) // ???
                                        ? thisPlayer.getPreviousGameMode()
                                        : Bukkit.getDefaultGameMode()
                                : Bukkit.getDefaultGameMode();
                        // Switching to previous, or default game mode.
                        thisPlayer.setGameMode(nextGameMode);
                    }
                    // Showing target to other players.
                    Bukkit.getOnlinePlayers().forEach(otherPlayer -> otherPlayer.showPlayer(Azure.getInstance(), thisPlayer));
                }
            });
        });
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
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been BANNED (" + (mostRecentBan.isPermanent() == false ? mostRecentBan.getDuration() : "permanent") + ") by " + mostRecentBan.getIssuer() + " with a reason: " + mostRecentBan.getReason());
        // Returning new (and now current) punishment.
        return mostRecentBan;
    }

    @Override
    public void unban(final @Nullable String issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentBan = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNBANNED by " + (issuer != null ? issuer : "SYSTEM"));
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
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been MUTED (" + (mostRecentMute.isPermanent() == false ? mostRecentMute.getDuration() : "permanent") + ") by " + mostRecentMute.getIssuer() + " with a reason: " + mostRecentMute.getReason());
        // Returning new (and now current) punishment.
        return mostRecentMute;
    }

    @Override
    public void unmute(final @Nullable String issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentMute = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNMUTED by " + (issuer != null ? issuer : "SYSTEM"));
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof AzureUser otherUser
                && name.equals(otherUser.name)
                && uniqueId.equals(otherUser.uniqueId)
                && textures.equals(otherUser.textures);
    }

}
