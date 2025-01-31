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
package cloud.grabsky.azure.user;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class AzureUser implements User {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull UUID uniqueId;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable String displayName;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC)
    private @Nullable String discordId;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String textures;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull String lastAddress;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PACKAGE, onMethod_ = @Internal)
    private @NotNull String lastCountryCode;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Internal)
    private int maxLevel;

    @Getter(AccessLevel.PUBLIC)
    private boolean isVanished;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Internal)
    private boolean isSpying;

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
    public void setDisplayName(final @Nullable String displayName, final @Nullable Component displayNameComponent) {
        final @Nullable Player thisPlayer = this.toPlayer();
        // Setting the display name.
        this.displayName = displayName;
        // Saving User data to the filesystem.
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(isSuccess -> {
            logSaveInformation(isSuccess);
            // Updating display name of an online player.
            if (thisPlayer != null && thisPlayer.isOnline() == true) Azure.getInstance().getBedrockScheduler().run(1L, (_) -> {
                thisPlayer.displayName(displayNameComponent);
            });
        });
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
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(isSuccess -> {
            logSaveInformation(isSuccess);
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
    public @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a new one.
        this.mostRecentBan = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                issuer.getName(),
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // Saving User data to the filesystem.
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(this::logSaveInformation);
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been BANNED (" + (mostRecentBan.isPermanent() == false ? mostRecentBan.getDuration() : "permanent") + ") by " + mostRecentBan.getIssuer() + " with a reason: " + mostRecentBan.getReason());
        // Returning new (and now current) punishment.
        return mostRecentBan;
    }

    @Override
    public void unban(final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentBan = null;
        // Saving User data to the filesystem.
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(this::logSaveInformation);
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNBANNED by " + issuer.getName());
    }

    @Override
    public @NotNull Punishment mute(final @Nullable Interval duration, final @Nullable String reason, final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a new one.
        this.mostRecentMute = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                issuer.getName(),
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // Saving User data to the filesystem.
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(this::logSaveInformation);
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been MUTED (" + (mostRecentMute.isPermanent() == false ? mostRecentMute.getDuration() : "permanent") + ") by " + mostRecentMute.getIssuer() + " with a reason: " + mostRecentMute.getReason());
        // Returning new (and now current) punishment.
        return mostRecentMute;
    }

    @Override
    public void unmute(final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentMute = null;
        // Saving User data to the filesystem.
        Azure.getInstance().getUserCache().as(AzureUserCache.class).saveUser(this).thenAccept(this::logSaveInformation);
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNMUTED by " + issuer.getName());
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        // Comparing instances.
        if (this == other)
            return true;
        // Comparing fields.
        return other instanceof AzureUser otherUser
                && name.equals(otherUser.name)
                && uniqueId.equals(otherUser.uniqueId)
                && textures.equals(otherUser.textures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uniqueId, textures);
    }


    // Helper method to log save information to the console.
    private void logSaveInformation(final Boolean isSuccess) {
        Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
    }

}
