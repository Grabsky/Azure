package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// TO-DO: Support for "@forever" selector which defaults to 0s, making the punishment permantent. (low priority)
public final class MuteCommand extends RootCommand {

    private final Azure plugin;
    private final LuckPerms luckperms;

    public MuteCommand(final @NotNull Azure plugin) {
        super("mute", null, "azure.command.mute", "/mute (player) (duration) (reason)", null);
        // ...
        this.plugin = plugin;
        this.luckperms = plugin.getLuckPerms();
    }

    private static final ExceptionHandler.Factory MUTE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_MUTE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(OfflinePlayer.class);
            case 1 -> IntervalArgument.ofRange(0L, 12L, Unit.YEARS);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting OfflinePlayer argument, this can be either a player name or their unique id.
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(MUTE_USAGE);
        // Getting UUID.
        final UUID targetUniqueId = target.getUniqueId();
        // Getting User object from UUID - can be null.
        final @Nullable User targetUser = plugin.getUserCache().getUser(targetUniqueId);
        // Leaving the command block in case that User object for provided player does not exist.
        if (targetUser == null) {
            Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER).placeholder("input", targetUniqueId).send(sender);
            return;
        }
        // Getting duration.
        final Interval duration = arguments.next(Interval.class, IntervalArgument.ofRange(0L, 12L, Unit.YEARS)).asRequired(MUTE_USAGE);
        // (optional) Getting the punishment reason.
        final @Nullable String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // ...
        if (sender instanceof Player senderOnline) {
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
                plugin.getBedrockScheduler().run(1L, (task) -> mute(sender, targetUser, reason, duration));
            });
            return;
        }
        // Otherwise, just muting.
        mute(sender, targetUser, reason, duration);
    }

    private static void mute(final @NotNull CommandSender sender, final @NotNull User targetUser, final @Nullable String reason, final @NotNull Interval duration) {
        // When duration is 0, punishment will be permantent - until manually removed.
        if (duration.as(Unit.MILLISECONDS) == 0) {
            // Muting the player.
            targetUser.mute(null, reason, sender.getName());
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_MUTE_SUCCESS_PERMANENT)
                    .placeholder("player", targetUser.getName())
                    .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                    // Sending to all players with specific permission.
                    .broadcast(receiver -> receiver.hasPermission("azure.command.mute") == true);
            // Exiting the command block.
            return;
        }
        // Muting the player temporarily.
        targetUser.mute(duration, reason, sender.getName());
        // Sending success message to the sender.
        Message.of(PluginLocale.COMMAND_MUTE_SUCCESS)
                .placeholder("player", targetUser.getName())
                .placeholder("duration_left", duration)
                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                // Sending to all players with specific permission.
                .broadcast(receiver -> receiver.hasPermission("azure.command.mute") == true);
    }

}
