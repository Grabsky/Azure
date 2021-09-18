package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SkullCommand extends BaseCommand {
    private final Azure instance;

    public SkullCommand(Azure instance) {
        super("skull", null, "azure.command.skull", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return null;
        else return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else {
            this.onSkullFromName(sender, args[0]);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/skull <player>");
    }

    @SubCommand
    public void onSkullFromName(CommandSender sender, String playerName) {
        if (sender instanceof Player executor) {
            if (UserCache.contains(playerName)) {
                executor.getInventory().addItem(new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(UserCache.get(playerName).getTexture()).build());
                AzureLang.send(sender, AzureLang.SKULL_RECEIVED.replace("{player}", UserCache.get(playerName).getName()));
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
