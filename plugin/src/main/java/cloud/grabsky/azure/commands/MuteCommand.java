package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.LongArgument;
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

// TO-DO: Better duration format, perhaps ["5s, 5min, 5h, 5d, @forever", ...]
// TO-DO: More permantent solution, store mutes inside a JSON file or something. PersistentDataContainer should be fine temporarily though.
// TO-DO: Store the source of mute.
public class MuteCommand extends RootCommand {

    private final Azure plugin;

    public MuteCommand(final @NotNull Azure plugin) {
        super("mute", null, "azure.command.mute", "/mute (player) (duration_min) (reason)", null);
        this.plugin = plugin;
    }

    private static final ExceptionHandler.Factory MUTE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.MUTE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> CompletionsProvider.of("0", "30", "60", "1440");
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
        // Getting duration in seconds.
        final long durationInMinutes = arguments.next(Long.class, LongArgument.ofRange(0, Long.MAX_VALUE)).asRequired(MUTE_USAGE);
        // (optional) Getting the punishment reason. Default has to be explicitly set here, so no "implementation-default" reason is used.
        final @Nullable String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // Loading LuckPerms data of targetted player, then running more logic.
        LuckPermsProvider.get().getUserManager().loadUser(target.getUniqueId()).thenAccept(user -> {
            // Checking if specified player is immunote to punishments.
            if (user.getCachedData().getPermissionData().checkPermission("azure.bypass.mute_immunity").asBoolean() == false) {
                // Following has to be scheduled onto the main thread.
                ((BedrockPlugin) context.getManager().getPlugin()).getBedrockScheduler().run(1L, (task) -> {
                    // When durationInMinutes is 0, punishment will be permantent - until manually removed.
                    if (durationInMinutes == 0) {
                        // Muting the player.
                        userTarget.mute(null, reason, sender.getName());
                        // Sending success message to the sender.
                        Message.of(PluginLocale.MUTE_SUCCESS_PERMANENT)
                                .placeholder("player", userTarget.getName())
                                .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                                .send(sender);
                        // Exiting the command block.
                        return;
                    }
                    // When durationInMinutes is not 0, punishment will be temporary.
                    final Interval duration = Interval.of(durationInMinutes, Unit.MINUTES);
                    // Muting the player.
                    userTarget.mute(duration, reason, sender.getName());
                    // Sending success message to the sender.
                    Message.of(PluginLocale.MUTE_SUCCESS)
                            .placeholder("player", userTarget.getName())
                            .placeholder("duration_left", duration.toString())
                            .placeholder("reason", (reason != null) ? reason : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                            .send(sender);
                });
                // Exiting the command block.
                return;
            }
            // Sending failure message to the sender.
            Message.of(PluginLocale.MUTE_FAIULURE_PLAYER_CANNOT_BE_MUTED).send(sender);
        });
    }

}
