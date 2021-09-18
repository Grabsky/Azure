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

public class FeedCommand extends BaseCommand {
    private final Azure instance;

    public FeedCommand(Azure instance) {
        super("feed", null, "azure.command.feed", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        if (index == 0) return null;
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else {
            this.onHealPlayer(sender, args[0]);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        if (sender instanceof Player executor) {
            executor.setFoodLevel(20);
            AzureLang.send(sender, AzureLang.YOU_HAVE_BEEN_FED);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onHealPlayer(CommandSender sender, String playerName) {
        if (sender.hasPermission("azure.command.feed.others")) {
            final Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                player.setFoodLevel(20);
                AzureLang.send(sender, AzureLang.PLAYER_HAS_BEEN_FED.replace("{player}", player.getName()));
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }

}
