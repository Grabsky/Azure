package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class DeleteCommand extends RootCommand {

    private final ChatManager chat;

    public DeleteCommand(final @NotNull Azure plugin) {
        super("delete", null, "azure.command.delete", "/delete <signature_uuid>", null);
        // ...
        this.chat = plugin.getChatManager();
    }

    private static final ExceptionHandler.Factory DELETE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_DELETE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final UUID uuid = queue.next(UUID.class).asRequired(DELETE_USAGE);
        // Requests message deletion and send error message on failure
        if (chat.deleteMessage(uuid) == false)
            Message.of(PluginLocale.COMMAND_DELETE_FAILURE).send(context.getExecutor().asCommandSender());
    }

}
