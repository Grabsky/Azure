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

public class TeleportCommand extends BaseCommand {

    private final Azure instance;

    public TeleportCommand(Azure instance) {
        super("teleport", List.of("tp"), "skydistrict.command.teleport", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        return switch (index) {
            case 0, 1 -> UserCache.getNamesOfOnlineUsers();
            default -> Collections.emptyList();
        };
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        switch (args.length) {
            default -> this.onDefault(sender);
            case 1 -> this.onTeleportToPlayer(sender, args[0]);
            case 2 -> this.onTeleportPlayerToPlayer(sender, args[0], args[1]);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/tp <player> [player]");
    }

    @SubCommand
    public void onTeleportToPlayer(CommandSender sender, String targetName) {
        if (sender instanceof Player executor) {
            final Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                if (executor != target) {
                    executor.teleport(target);
                    AzureLang.send(sender, AzureLang.TELEPORTED_TO_PLAYER.replace("{target}", target.getName()));
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

    @SubCommand
    public void onTeleportPlayerToPlayer(CommandSender sender, String playerOneName, String playerTwoName) {
        if (sender.hasPermission("skydistrict.command.teleport.others")) {
            final Player playerOne = Bukkit.getPlayer(playerOneName);
            final Player playerTwo = Bukkit.getPlayer(playerTwoName);
            if (playerOne != null && playerOne.isOnline() && playerTwo != null && playerTwo.isOnline()) {
                if (playerOne != playerTwo) {
                    playerOne.teleport(playerTwo);
                    AzureLang.send(sender, AzureLang.TELEPORTED_PLAYER_TO_PLAYER.replace("{player_one}", playerOne.getName()).replace("{player_two}", playerTwo.getName()));
                    return;
                }
                AzureLang.send(sender, Global.TARGETS_ARE_THE_SAME);
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }
}

