package me.grabsky.azure.commands.teleport;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TeleportHereCommand extends BaseCommand {

    private final Azure instance;

    public TeleportHereCommand(Azure instance) {
        super("teleporthere", List.of("tphere", "s"), "skydistrict.command.teleporthere", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return UserCache.getNamesOfOnlineUsers();
        else return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            this.onTeleportPlayerHere(sender, args[0]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/tphere <player>");
    }

    @SubCommand
    public void onTeleportPlayerHere(CommandSender sender, String targetName) {
        if (sender instanceof Player executor) {
            final Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                if (executor != target) {
                    target.teleport(executor);
                    AzureLang.send(sender, AzureLang.PLAYER_TELEPORTED_TO_YOU.replace("{target}", target.getName()));
                    return;
                }
                AzureLang.send(sender, Global.CANT_USE_ON_YOURSELF);
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
