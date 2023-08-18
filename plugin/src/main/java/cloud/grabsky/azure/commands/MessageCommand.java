package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

// TO-DO: Disallow spying on players with higher group weight.
@Command(name = "message", aliases = "msg", permission = "azure.command.message", usage = "/message (player) (message)")
public final class MessageCommand extends RootCommand {

    @Dependency
    private @UnknownNullability ChatManager chat;

    @Dependency
    private @UnknownNullability UserCache userCache;


    private static final ExceptionHandler.Factory MESSAGE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_MESSAGE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Getting target player.
        final Player target = arguments.next(Player.class).asRequired(MESSAGE_USAGE);
        // Sending error message when sender is the same as target.
        if (sender.equals(target) == true) {
            Message.of(PluginLocale.COMMAND_MESSAGE_FAILURE_RECIPIENT_MUST_NOT_BE_SENDER).send(sender);
            return;
        }
        // Don't expose hidden players. This will be (soon) moved to the PlayerArgument
        if (sender.canSee(target) == false) {
            Message.of(PluginLocale.Commands.INVALID_PLAYER).placeholder("input", target.getName()).send(sender);
            return;
        }
        // Getting the rest of user input as a message.
        final String message = arguments.next(String.class, StringArgument.GREEDY).asRequired(MESSAGE_USAGE);
        // Sending messages...
        Message.of(PluginLocale.COMMAND_MESSAGE_SUCCESS_TO).placeholder("target", target).placeholder("message", message).send(sender);
        Message.of(PluginLocale.COMMAND_MESSAGE_SUCCESS_FROM).placeholder("sender", sender).placeholder("message", message).send(target);
        // Sending message to spies...
        Bukkit.getOnlinePlayers().forEach(player -> {
            // Prevent duplicated messages.
            if (player.equals(sender) == true || player.equals(target) == true)
                return;
            // Getting User object...
            final User user = userCache.getUser(player);
            // Forwarding message to spying player. (IF CURRENTLY SPYING)
            if (user.isSpying() == true)
                Message.of(PluginLocale.COMMAND_SPY_MESSAGE_FORMAT).placeholder("sender", sender).placeholder("target", target).placeholder("message", message).send(player);
        });
        // Sending message to the console...
        Message.of(PluginLocale.COMMAND_SPY_MESSAGE_FORMAT_CONSOLE).placeholder("sender", sender).placeholder("target", target).placeholder("message", message).send(Bukkit.getConsoleSender());
        // Updating recipients...
        chat.setLastRecipients(sender.getUniqueId(), target.getUniqueId());
    }

}
