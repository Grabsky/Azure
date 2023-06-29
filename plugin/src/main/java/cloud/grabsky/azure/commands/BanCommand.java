package cloud.grabsky.azure.commands;

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
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

// TO-DO: Better duration format, perhaps ["5s, 5min, 5h, 5d, @forever", ...]
public class BanCommand extends RootCommand {

    public BanCommand() {
        super("ban", null, "azure.command.ban", "/ban (player) (duration) (reason)", null);
    }

    private static final ExceptionHandler.Factory BAN_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.BAN_USAGE).send(context.getExecutor().asCommandSender());
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

    @Override // TO-DO: Check for "immunity" permissions.
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(BAN_USAGE);
        // ...
        final long durationMin = arguments.next(Long.class, LongArgument.ofRange(0, Long.MAX_VALUE)).asRequired(BAN_USAGE);
        final String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON);
        // ...
        LuckPermsProvider.get().getUserManager().loadUser(target.getUniqueId()).thenAccept(user -> {
            // Checking if specified player is immunote to punishments.
            if (user.getCachedData().getPermissionData().checkPermission("azure.bypass.ban_immunity").asBoolean() == false) {
                // Following has to be scheduled onto the main thread.
                ((BedrockPlugin) context.getManager().getPlugin()).getBedrockScheduler().run(1L, (task) -> {
                    // PERMANTENT
                    if (durationMin == 0) {
                        target.banPlayer(reason, sender.getName());
                        Message.of(PluginLocale.BAN_SUCCESS_PERMANENT)
                                .placeholder("player", target.getName())
                                .placeholder("reason", reason)
                                .send(sender);
                        return;
                    }
                    // TEMPORARY
                    final Interval interval = Interval.of(durationMin, Unit.MINUTES);
                    final Date expirationDate = Date.from(Instant.ofEpochMilli(currentTimeMillis() + (long) interval.as(Unit.MILLISECONDS)));
                    // Banning the player. Player will be kicked manually for the sake of custimazble message.
                    final BanEntry entry = target.banPlayer(reason, expirationDate, sender.getName(), false);
                    // Kicking with custom message.
                    if (target.isOnline() && target instanceof Player onlineTarget) {
                        final Component message = (entry.getExpiration() != null)
                                ? Message.of(PluginLocale.BAN_DISCONNECT_MESSAGE)
                                .placeholder("until", interval.toString())
                                .placeholder("reason", entry.getReason() != null ? entry.getReason() : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                                .parse()
                                : Message.of(PluginLocale.BAN_DISCONNECT_MESSAGE_PERMANENT)
                                .placeholder("reason", entry.getReason() != null ? entry.getReason() : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                                .parse();
                        if (message != null)
                            onlineTarget.kick(message);
                    }
                    // ...
                    Message.of(PluginLocale.BAN_SUCCESS)
                            .placeholder("player", target.getName())
                            .placeholder("duration", interval.toString())
                            .placeholder("reason", reason)
                            .send(sender);
                });
                return;
            }
            Message.of(PluginLocale.BAN_FAIULURE_PLAYER_CANNOT_BE_BANNED).send(sender);
        });
    }

}
