package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

@Command(name = "player", permission = "azure.command.player", usage = "/player (player)")
public final class PlayerCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;

    @Dependency
    private @UnknownNullability LuckPerms luckperms;


    private final static DecimalFormat ONE_DECIMAL_PLACE = new DecimalFormat("#.#");
    private final static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static final ExceptionHandler.Factory PLAYER_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_PLAYER_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(OfflinePlayer.class) : CompletionsProvider.EMPTY;
    }

    @Override @SuppressWarnings("UnstableApiUsage")
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Parsing next argument as OfflinePlayer. Can be UUID or player name.
        final OfflinePlayer target = (sender instanceof Player senderPlayer)
                ? arguments.next(OfflinePlayer.class).asOptional(senderPlayer)
                : arguments.next(OfflinePlayer.class).asRequired(PLAYER_USAGE);
        // Getting UUID.
        final UUID targetUniqueId = target.getUniqueId();
        // Getting User object from UUID - can be null.
        final @Nullable User targetUser = plugin.getUserCache().getUser(targetUniqueId);
        // Leaving the command block in case that User object for provided player does not exist.
        if (targetUser == null) {
            Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER).placeholder("input", targetUniqueId).send(sender);
            return;
        }
        // Displaying information about player that is currently online.
        if (target instanceof Player targetOnline && targetOnline.isOnline() == true) {
            // Leaving the command block in case target is immune to this command check.
            if (sender instanceof Player senderOnline && sender != target) {
                final @Nullable Group senderGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(senderOnline).getPrimaryGroup());
                final @Nullable Group targetGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(targetOnline).getPrimaryGroup());
                // Comparing group weights.
                if (senderGroup == null || targetGroup == null || senderGroup.getWeight().orElse(0) <= targetGroup.getWeight().orElse(0)) {
                    Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                    return;
                }
            }
            // Continuing...
            // final WrappedPlayer guithiumTarget = Guithium.api().getPlayerManager().get(targetUniqueId);
            final Location location = targetOnline.getLocation();
            final String address = (targetUser.getLastAddress().equals("N/A") == false)
                    ? (sender.hasPermission("azure.command.player.can_see_address") == true)
                            ? targetUser.getLastAddress()
                            : "*****"
                    : "N/A";
            // Preparing and sending the message.
            Message.of(PluginLocale.COMMAND_PLAYER_SUCCESS_ONLINE)
                    .placeholder("name", targetUser.getName())
                    .placeholder("uuid", targetUniqueId.toString().substring(0, 13))
                    .placeholder("ip", address)
                    .placeholder("country", targetUser.getLastCountryCode())
                    .placeholder("ping", getColoredPing(targetOnline.getPing()))
                    .placeholder("first_join", DD_MM_YYYY.format(targetOnline.getFirstPlayed()))
                    .placeholder("time_played", (long) Math.floor(Interval.of(target.getStatistic(Statistic.PLAY_ONE_MINUTE), Unit.TICKS).as(Unit.HOURS)) + "h")
                    .placeholder("client", (targetOnline.getClientBrandName() != null) ? targetOnline.getClientBrandName() : "N/A")
                    .placeholder("version", getVersionFromProtocol(targetOnline.getProtocolVersion()))
                    .placeholder("online_since", Interval.between(System.currentTimeMillis(), targetOnline.getLastLogin(), Unit.MILLISECONDS))
                    .placeholder("x", location.blockX())
                    .placeholder("y", location.blockY())
                    .placeholder("z", location.blockZ())
                    .placeholder("world", location.getWorld())
                    .placeholder("gamemode", PluginLocale.getGameMode(targetOnline.getGameMode()))
                    .placeholder("is_flying", getColoredBooleanShort(targetOnline.isFlying() == true))
                    .placeholder("is_invulnerable", getColoredBooleanShort(targetOnline.isInvulnerable() == true))
                    .placeholder("health", ONE_DECIMAL_PLACE.format(targetOnline.getHealth() / 2.0D))
                    .placeholder("hunger", ONE_DECIMAL_PLACE.format(targetOnline.getFoodLevel() / 2.0D))
                    .placeholder("xp_level", targetOnline.getLevel())
                    .placeholder("xp_progress", Math.round(targetOnline.getExp() * 100))
                    .placeholder("is_banned", getColoredBooleanShort(targetUser.isBanned() == true))
                    .placeholder("is_muted", getColoredBooleanShort(targetUser.isMuted() == true))
                    .send(sender);
            return;
        }
        // Leaving the command block in case target is immune to this command check.
        if (sender instanceof Player senderOnline && sender != target) {
            // Getting group of the sender.
            final @Nullable Group senderGroup = luckperms.getGroupManager().getGroup(luckperms.getPlayerAdapter(Player.class).getUser(senderOnline).getPrimaryGroup());
            // Loading the target User.
            luckperms.getUserManager().loadUser(targetUniqueId).thenAccept(user -> {
                final @Nullable Group targetGroup = luckperms.getGroupManager().getGroup(user.getPrimaryGroup());
                // Comparing group weights.
                if (senderGroup == null || targetGroup == null || senderGroup.getWeight().orElse(0) <= targetGroup.getWeight().orElse(0)) {
                    Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                    return;
                }
                // Continuing... scheduling the rest of the logic onto the main thread.
                plugin.getBedrockScheduler().run(1L, (task) -> showOfflinePlayerInfo(sender, target, targetUser));
            });
            return;
        }
        // Displaying information to the console (or self) otherwise.
        showOfflinePlayerInfo(sender, target, targetUser);
    }

    // Displays information about player that is currently offline.
    @SuppressWarnings("UnstableApiUsage")
    private void showOfflinePlayerInfo(final @NotNull CommandSender sender, final @NotNull OfflinePlayer target, final @NotNull User targetUser) {
        try {
            // Getting the last ip address - may be redacted in case sender does not have permission to see it.
            final String address = (targetUser.getLastAddress().equals("N/A") == false)
                    ? (sender.hasPermission("azure.command.info.can_see_address") == true)
                            ? targetUser.getLastAddress()
                            : "*****"
                    : "N/A";
            // Reading 'PRIMARY_WORLD/playerdata/UUID.dat' file...
            final CompoundBinaryTag tag = targetUser.getPlayerData();
            // Getting last known location.
            final String lastWorld = tag.getString("Dimension"); // No need to convert it to NamespacedKey or World or anything - String should be fine for that.
            final long lastX = (long) Math.floor(tag.getList("Pos").getDouble(0)); // Math#floor is used to calculate block position.
            final long lastY = (long) Math.floor(tag.getList("Pos").getDouble(1)); // Math#floor is used to calculate block position.
            final long lastZ = (long) Math.floor(tag.getList("Pos").getDouble(2)); // Math#floor is used to calculate block position.
            // Getting last seen and first played timestamps.
            final long lastSeen = tag.getCompound("Paper").getLong("LastSeen");
            final long firstPlayed = tag.getCompound("bukkit").getLong("firstPlayed");
            // Getting last known gamemode, flying state and invulnerability state.
            final GameMode gamemode = GameMode.getByValue(tag.getInt("playerGameType")); // Should never be null.
            final boolean isFlying = tag.getCompound("abilities").getBoolean("flying");
            final boolean isInvulnerable = tag.getCompound("abilities").getBoolean("invulnerable");
            // Getting health and hunger.
            final float health = tag.getInt("Health") / 2.0F; // Divided by 2 to represent a human-friendly value.
            final float hunger = tag.getInt("foodLevel") / 2.0F; // Divided by 2 to represent a human-friendly value.
            // Getting experience level and progress towards the next level.
            final int level = tag.getInt("XpLevel");
            final int progress = (int) (tag.getFloat("XpP") * 100); // Multiplied by 100 to represent a human-friendly percent integer.
            // Preparing and sending the message.
            Message.of(PluginLocale.COMMAND_PLAYER_SUCCESS_OFFLINE)
                    .placeholder("name", targetUser.getName())
                    .placeholder("uuid", target.getUniqueId().toString().substring(0, 13))
                    .placeholder("ip", address)
                    .placeholder("country", targetUser.getLastCountryCode())
                    .placeholder("first_join", DD_MM_YYYY.format(firstPlayed))
                    .placeholder("time_played", (long) Math.floor(Interval.of(target.getStatistic(Statistic.PLAY_ONE_MINUTE), Unit.TICKS).as(Unit.HOURS)) + "h")
                    .placeholder("offline_since", Interval.between(System.currentTimeMillis(), lastSeen, Unit.MILLISECONDS))
                    .placeholder("x", lastX)
                    .placeholder("y", lastY)
                    .placeholder("z", lastZ)
                    .placeholder("world", (lastWorld.equals("") == false) ? lastWorld : "N/A")
                    .placeholder("gamemode", PluginLocale.getGameMode(gamemode)) // Should never be null.
                    .placeholder("is_flying", getColoredBooleanShort(isFlying == true))
                    .placeholder("is_invulnerable", getColoredBooleanShort(isInvulnerable == true))
                    .placeholder("health", ONE_DECIMAL_PLACE.format(health))
                    .placeholder("hunger", ONE_DECIMAL_PLACE.format(hunger))
                    .placeholder("xp_level", level)
                    .placeholder("xp_progress", progress)
                    .placeholder("is_banned", getColoredBooleanShort(targetUser.isBanned() == true))
                    .placeholder("is_muted", getColoredBooleanShort(targetUser.isMuted() == true))
                    .send(sender);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private Component getColoredBooleanShort(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanShort(true).color(GREEN) : PluginLocale.getBooleanShort(false).color(RED);
    }

    // TO-DO: Use pattern switch if feasible. (JDK 21)
    private Component getColoredPing(final int num) {
        if (num < 60)
            return Component.text(num + "ms", GREEN);
        if (num < 120)
            return Component.text(num + "ms", YELLOW);
        // ...
        return Component.text(num + "ms", RED);
    }

    private String getVersionFromProtocol(final int protocolVersion) {
        return switch (protocolVersion) {
            case 763 -> "1.20(.1)"; // 1.20 and 1.20.1 are both using the same protocol.
            default -> "N/A";
        };
    }

}
