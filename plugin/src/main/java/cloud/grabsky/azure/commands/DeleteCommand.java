package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;

public final class DeleteCommand extends RootCommand {

    private final ChatManager chat;

    public DeleteCommand(final ChatManager chat) {
        super("delete", null, "azure.plugin.chat.can_delete_messages", "/delete <signature_uuid>", null);
        this.chat = chat;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final UUID uuid = queue.next(UUID.class).asRequired(SEND_USAGE_ON_MISSING_INPUT);
        // Requests message deletion and send error message on failure
        if (chat.deleteMessage(uuid) == false) {
            sendMessage(context.getExecutor().asCommandSender(), PluginLocale.COMMAND_DELETE_FAILURE);
        }
    }

    private static final ExceptionHandler.Factory SEND_USAGE_ON_MISSING_INPUT = (exception) -> {
        if (exception instanceof MissingInputException) return (_0, context) -> {
            sendMessage(context.getExecutor().asCommandSender(), PluginLocale.COMMAND_DELETE_USAGE);
        };
        return null;
    };

}
