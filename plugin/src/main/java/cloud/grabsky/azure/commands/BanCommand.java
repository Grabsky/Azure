package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
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

import java.time.Instant;
import java.util.Date;

public class BanCommand extends RootCommand {

    public BanCommand() {
        super("delete", null, "azure.command.delete", "/ban (player) (duration) (reason)", null);
    }

    private static final ExceptionHandler.Factory BAN_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.BAN_USAGE);
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
        final Long durationMin = arguments.next(Long.class, LongArgument.ofRange(0, Long.MAX_VALUE)).asRequired();
        final String reason = arguments.next(String.class, StringArgument.GREEDY).asOptional(PluginLocale.BAN_DEFAULT_REASON);
        // PERMANTENT
        if (durationMin == 0) {
            target.banPlayer(reason, sender.getName());
            Message.of(PluginLocale.BAN_SUCCESS)
                    .placeholder("player", target.getName())
                    .placeholder("duration", "forever")
                    .placeholder("reason", reason);
            return;
        }
        // TEMPORARY
        final Interval interval = Interval.of(durationMin, Unit.MINUTES);
        final Date expirationDate = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + (long) interval.as(Unit.SECONDS)));
        // ...
        target.banPlayer(reason, expirationDate, sender.getName());
        Message.of(PluginLocale.BAN_SUCCESS)
                .placeholder("player", target.getName())
                .placeholder("duration", interval.toString())
                .placeholder("reason", reason);
    }

}
