package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class InvulnerableCommand extends BaseCommand {
    private final Azure instance;

    public InvulnerableCommand(Azure instance) {
        super("invulnerable", List.of("in"), "azure.command.invulnerable", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.onDefault(sender);
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        final Player player = (Player) sender;
        if (player.isInvulnerable()) {
            player.setInvulnerable(false);
            AzureLang.send(sender, AzureLang.INVULNERABLE_OFF);
            return;
        }
        player.setInvulnerable(true);
        AzureLang.send(sender, AzureLang.INVULNERABLE_ON);
    }
}
