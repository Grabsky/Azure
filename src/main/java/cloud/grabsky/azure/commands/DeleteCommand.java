package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class DeleteCommand extends RootCommand {

    private final ChatManager chat;

    public DeleteCommand(final ChatManager chat) {
        super("delete", null, "azure.command.delete", "/delete", null);
        this.chat = chat;
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return (index == 0)
                ? CompletionsProvider.of(Player.class)
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        final Player player = queue.next(Player.class).asRequired();
        final UUID uuid = queue.next(UUID.class).asRequired();
        // ...
        if (chat.deleteMessage(player, uuid) == false)
            sender.sendMessage("Cannot delete requested message.");
    }

}
