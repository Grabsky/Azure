package cloud.grabsky.azure.commands;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.ComponentArgument;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.exception.CommandLogicException;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;

public final class NickCommand extends RootCommand {

    public NickCommand() {
        super("nick", null, "azure.command.nick", "/nick [nick]", "Set or reset your in-game nickname.");
    }

    @Override
    public void onCommand(final RootCommandContext context, final ArgumentQueue queue) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();

        final Component nick = queue.next(Component.class, ComponentArgument.GREEDY).asOptional();

        sender.displayName(nick);

        if (nick != null) {
            sender.sendMessage(text("Your name has been changed to: ").append(nick));
        }

        // flags...
        final String[] flags = queue.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");

    }

}
