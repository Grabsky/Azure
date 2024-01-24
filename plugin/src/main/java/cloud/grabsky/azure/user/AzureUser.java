/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.user;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.Color;
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
    private final @NotNull String textures;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull String lastAddress;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PACKAGE, onMethod = @__(@Internal))
    private @NotNull String lastCountryCode;

    @Getter(AccessLevel.PUBLIC)
    private boolean isVanished;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod = @__(@Internal))
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
    public @NotNull Punishment ban(final @Nullable Interval duration, final @Nullable String reason, final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a new one.
        this.mostRecentBan = new AzurePunishment(
                (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON,
                issuer.getName(),
                Interval.now(),
                (duration != null) ? duration : Interval.of(Long.MAX_VALUE, Unit.MILLISECONDS)
        );
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been BANNED (" + (mostRecentBan.isPermanent() == false ? mostRecentBan.getDuration() : "permanent") + ") by " + mostRecentBan.getIssuer() + " with a reason: " + mostRecentBan.getReason());
        // Forwarding to Discord...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing type-specific embed.
            final EmbedBuilder embed = DiscordLogger.constructBan(this, mostRecentBan);
            // Forwarding the message through configured webhook.
            new WebhookMessageBuilder().addEmbed(embed).sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL);
        }
        // Returning new (and now current) punishment.
        return mostRecentBan;
    }

    @Override
    public void unban(final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentBan = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNBANNED by " + issuer.getName());
        // Forwarding to Discord...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing type-specific embed.
            final EmbedBuilder embed = DiscordLogger.constructUnban(this, issuer);
            // Forwarding the message through configured webhook.
            new WebhookMessageBuilder().addEmbed(embed).sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL);
        }
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
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been MUTED (" + (mostRecentMute.isPermanent() == false ? mostRecentMute.getDuration() : "permanent") + ") by " + mostRecentMute.getIssuer() + " with a reason: " + mostRecentMute.getReason());
        // Forwarding to Discord...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing type-specific embed.
            final EmbedBuilder embed = DiscordLogger.constructMute(this, mostRecentMute);
            // Forwarding the message through configured webhook.
            new WebhookMessageBuilder().addEmbed(embed).sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL);
        }
        // Returning new (and now current) punishment.
        return mostRecentMute;
    }

    @Override
    public void unmute(final @NotNull CommandSender issuer) {
        // Overriding previous punishment with a null one.
        this.mostRecentMute = null;
        // Saving User data to the filesystem.
        ((AzureUserCache) Azure.getInstance().getUserCache()).saveUser(this).thenAccept(isSuccess -> {
            Azure.getInstance().getLogger().info("Saving data of " + this.name + " in the background... " + (isSuccess == true ? "OK" : "ERROR"));
        });
        // Logging...
        Azure.getInstance().getPunishmentsFileLogger().log("Player " + this.getName() + " (" + this.getUniqueId() + ") has been UNMUTED by " + issuer.getName());
        // Forwarding to Discord...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Constructing type-specific embed.
            final EmbedBuilder embed = DiscordLogger.constructUnmute(this, issuer);
            // Forwarding the message through configured webhook.
            new WebhookMessageBuilder().addEmbed(embed).sendSilently(Azure.getInstance().getDiscord(), PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL);
        }
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


    // Utility class to help constructing punishments integration embeds.
    public static final class DiscordLogger {

        // Common for all punishment types.
        private static EmbedBuilder constructCommon() {
            return new EmbedBuilder()
                    .setColor(new Color(255, 85, 85))
                    .setTimestampToNow();
        }

        private static EmbedBuilder constructBan(final @NotNull User user, final @NotNull Punishment punishment) {
            return constructCommon()
                    .setDescription("**" + user.getName() + "** has been banned by **" + punishment.getIssuer() + "**.\n** **\n** **")
                    .addField("Identifier", user.getUniqueId().toString())
                    .addField("Punishment Duration", punishment.isPermanent() == false ? punishment.getDuration().toString() : "Permanent")
                    .addField("Punishment Reason", punishment.getReason() + "\n** **\n** **");
        }

        private static EmbedBuilder constructUnban(final @NotNull User user, final @NotNull CommandSender issuer) {
            return constructCommon()
                    .setDescription("**" + user.getName() + "** has been unbanned by **" + issuer.getName() + "**.\n** **\n** **")
                    .addField("Identifier", user.getUniqueId() + "\n** **\n** **");
        }

        private static EmbedBuilder constructMute(final @NotNull User user, final @NotNull Punishment punishment) {
            return constructCommon()
                    .setDescription("**" + user.getName() + "** has been muted by **" + punishment.getIssuer() + "**.\n** **\n** **")
                    .addField("Identifier", user.getUniqueId().toString())
                    .addField("Punishment Duration", punishment.isPermanent() == false ? punishment.getDuration().toString() : "Permanent")
                    .addField("Punishment Reason", punishment.getReason() + "\n** **\n** **");
        }

        private static EmbedBuilder constructUnmute(final @NotNull User user, final @NotNull CommandSender issuer) {
            return constructCommon()
                    .setDescription("**" + user.getName() + "** has been unmuted by **" + issuer.getName() + "**.\n** **\n** **")
                    .addField("Identifier", user.getUniqueId() + "\n** **\n** **");
        }

        // Public as this is must be called from the command directly.
        public static EmbedBuilder constructKick(final @NotNull User user, final @NotNull CommandSender sender, final @NotNull String reason) {
            return constructCommon()
                    .setDescription("**" + user.getName() + "** has been kicked by **" + sender.getName() + "**.\n** **\n** **")
                    .addField("Identifier", user.getUniqueId().toString())
                    .addField("Punishment Reason", reason + "\n** **\n** **");
        }

    }

}
