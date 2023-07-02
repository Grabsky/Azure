package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class UnbanCommand extends RootCommand {

    private final Azure plugin;

    public UnbanCommand(final @NotNull Azure plugin) {
        super("unban", null, "azure.command.unban", "/unban (player)", null);
        this.plugin = plugin;
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return (index == 0)
                ? (ctx) -> Bukkit.getBannedPlayers().stream().map(OfflinePlayer::getName).toList()
                : CompletionsProvider.EMPTY;
    }

    private static final ExceptionHandler.Factory UNBAN_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_UNBAN_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting OfflinePlayer argument, this can be either a player name or their unique id.
        final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(UNBAN_USAGE);
        // ...
        final User targetUser = plugin.getUserCache().getUser(target.getUniqueId());
        // ...
        if (targetUser != null) {
            // Checking if player is banned.
            if (targetUser.isBanned() == true) {
                // Unbanning the player.
                targetUser.unban(sender.getName());
                // Sending success message to the sender.
                Message.of(PluginLocale.COMMAND_UNBAN_SUCCESS).placeholder("player", targetUser.getName()).send(sender);
                return;
            }
            // Sending failure message to the sender.
            Message.of(PluginLocale.COMMAND_UNBAN_FAILURE_PLAYER_NOT_BANNED).send(sender);
            return;
        }
        Message.of(PluginLocale.Commands.INVALID_OFFLINE_PLAYER).send(sender);
    }

}
