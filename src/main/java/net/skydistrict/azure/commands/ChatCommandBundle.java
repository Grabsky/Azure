package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ChatCommandBundle {

    private final String clearString;

    public ChatCommandBundle() {
        this.clearString = ("\n ").repeat(100);
    }

    public void register() {
        this.onChatClear().register();
    }

    public CommandAPICommand onChatClear() {
        return new CommandAPICommand("clearchat")
                .withAliases("cc")
                .withArguments(new EntitySelectorArgument("scope", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
                .withPermission("skydistrict.command.clearchat")
                .executes((sender, args) -> {
                    for (final Player player : (Collection<Player>) args[0]) {
                        player.sendMessage(clearString);
                    }
                });
    }
}
