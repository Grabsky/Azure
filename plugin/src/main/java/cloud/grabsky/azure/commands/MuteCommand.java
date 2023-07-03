package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.commands.arguments.IntervalArgument;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.BedrockPlugin;
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
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TO-DO: Support for "@forever" selector which defaults to 0s, making the punishment permantent. (low priority)
// TO-DO: Notify staff members about bans.
public class MuteCommand extends RootCommand {

    private final Azure plugin;

    public MuteCommand(final @NotNull Azure plugin) {
        super("mute", null, "azure.command.mute", "/mute (player) (duration) (reason)", null);
        this.plugin = plugin;
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
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> IntervalArgument.ofRange(0L, 12L, Unit.YEARS);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting Player argument.
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(MUTE_USAGE);
        // ...
        final User userTarget = plugin.getUserCache().getUser(target.getUniqueId());
        // Getting duration.
        final Interval duration = arguments.next(Interval.class, IntervalArgument.ofRange(0L, 12L, Unit.YEARS)).asRequired(MUTE_USAGE);
        // (optional) Getting the punishment reason. Default has to be explicitly set here, so no "implementation-default" reason is used.
        final @Nullable String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // Loading LuckPerms data of targetted player, then running more logic.
        LuckPermsProvider.get().getUserManager().loadUser(target.getUniqueId()).thenAccept(user -> {
            // Checking if specified player is immunote to punishments.
            if (user.getCachedData().getPermissionData().checkPermission("azure.bypass.mute_immunity").asBoolean() == false) {
                // Following has to be scheduled onto the main thread.
                ((BedrockPlugin) context.getManager().getPlugin()).getBedrockScheduler().run(1L, (task) -> {
                    // When duration is 0, punishment will be permantent - until manually removed.
                    if (duration.as(Unit.MILLISECONDS) == 0) {
                        // Muting the player.
                        userTarget.mute(null, reason, sender.getName());
                        // Sending success message to the sender.
                        Message.of(PluginLocale.COMMAND_MUTE_SUCCESS_PERMANENT)
                                .placeholder("player", userTarget.getName())
                                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                                .send(sender);
                        // Exiting the command block.
                        return;
                    }
                    // Muting the player temporarily.
                    userTarget.mute(duration, reason, sender.getName());
                    // Sending success message to the sender.
                    Message.of(PluginLocale.COMMAND_MUTE_SUCCESS)
                            .placeholder("player", userTarget.getName())
                            .placeholder("duration_left", duration.toString())
                            .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                            .send(sender);
                });
                // Exiting the command block.
                return;
            }
            // Sending failure message to the sender.
            Message.of(PluginLocale.COMMAND_MUTE_FAIULURE_PLAYER_CANNOT_BE_MUTED).send(sender);
        });
    }

}
