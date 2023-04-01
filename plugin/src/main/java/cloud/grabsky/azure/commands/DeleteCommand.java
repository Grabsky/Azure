package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class DeleteCommand extends RootCommand {

    private final ChatManager chat;

    public DeleteCommand(final ChatManager chat) {
        super("delete", null, "azure.command.delete", "/delete <signature_uuid>", null);
        this.chat = chat;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final UUID uuid = queue.next(UUID.class).asRequired();
        // Requests message deletion and send error message on failure
        if (chat.deleteMessage(uuid) == false) {
            Message.of(PluginLocale.COMMAND_DELETE_FAILURE).send(context.getExecutor().asCommandSender());
        }
    }

}
