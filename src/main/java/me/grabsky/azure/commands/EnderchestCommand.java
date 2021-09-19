package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class EnderchestCommand extends BaseCommand {
    private final Azure instance;

    public EnderchestCommand(Azure instance) {
        super("enderchest", List.of("ec"), "azure.command.enderchest", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return null;
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            this.onEnderchestOthers(sender, args[0]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        final Player executor = (Player) sender;
        executor.openInventory(executor.getEnderChest());
    }

    @SubCommand
    public void onEnderchestOthers(CommandSender sender, String targetName) {
        if (sender.hasPermission("azure.command.enderchest.others")) {
            final Player executor = (Player) sender;
            final Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                executor.openInventory(target.getEnderChest());
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }
}
