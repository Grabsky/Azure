package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class InvseeCommand extends BaseCommand {
    private final Azure instance;

    public InvseeCommand(Azure instance) {
        super("invsee", null, "azure.command.invsee", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        if (index == 0) return null;
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            this.onInvsee(sender, args[0]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/invsee <player>");
    }

    @SubCommand
    public void onInvsee(CommandSender sender, String targetName) {
        final Player executor = (Player) sender;
        final Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            executor.openInventory(target.getInventory());
            return;
        }
        AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
    }
}
