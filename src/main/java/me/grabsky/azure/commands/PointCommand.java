package me.grabsky.azure.commands;

import io.papermc.lib.PaperLib;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.manager.PointManager;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PointCommand extends BaseCommand {
    private final Azure instance;
    private final PointManager manager;

    public PointCommand(Azure instance) {
        super("point", List.of("p"), "firedot.command.point", ExecutorType.ALL);
        this.instance = instance;
        this.manager = instance.getLocationsManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return List.of("save", "del", "tp");
        if (index == 1) return switch (arg) {
            case "del", "tp" -> manager.getIds();
            default -> Collections.emptyList();
        };
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else switch (args[0]) {
            case "save" -> {
                if (args.length == 2) {
                    this.onPointAdd(sender, args[1].toLowerCase());
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/point save <id>");
            }
            case "del" -> {
                if (args.length == 2) {
                    this.onPointDelete(sender, args[1].toLowerCase());
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/point del <id>");
            }
            case "tp" -> {
                if (args.length == 2) {
                    this.onPointTeleport(sender, args[1].toLowerCase());
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/point tp <id>");
            }
            default -> this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, AzureLang.POINT_USAGE);
    }

    @SubCommand
    public void onPointAdd(CommandSender sender, String id) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.point.save")) {
                if (id.matches("[a-zA-Z0-9_-]+")) {
                    final boolean wasSet = manager.hasPoint(id);
                    manager.addPoint(id, executor.getLocation());
                    AzureLang.send(sender, ((wasSet) ? AzureLang.POINT_OVERWRITTEN : AzureLang.POINT_ADDED).replace("{id}", id));
                    return;
                }
                AzureLang.send(sender, AzureLang.INVALID_CHARACTERS);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onPointDelete(CommandSender sender, String id) {
        if (sender.hasPermission("firedot.command.point.delete")) {
            if (manager.hasPoint(id)) {
                manager.deletePoint(id);
                AzureLang.send(sender, AzureLang.POINT_DELETED.replace("{id}", id));
                return;
            }
            AzureLang.send(sender, AzureLang.POINT_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }

    @SubCommand
    public void onPointTeleport(CommandSender sender, String id) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.point.teleport")) {
                if (manager.hasPoint(id)) {
                    PaperLib.teleportAsync(executor, manager.getPoint(id));
                    AzureLang.send(sender, AzureLang.TELEPORTED_TO_POINT.replace("{id}", id));
                    return;
                }
                AzureLang.send(sender, AzureLang.POINT_NOT_FOUND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
