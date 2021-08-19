package me.grabsky.azure.commands.teleport;

import io.papermc.lib.PaperLib;
import me.grabsky.azure.Azure;
import me.grabsky.azure.config.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Numbers;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TeleportLocationCommand extends BaseCommand {

    private final Azure instance;

    public TeleportLocationCommand(Azure instance) {
        super("teleportlocation", List.of("tploc"), "skydistrict.command.teleportlocation", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        if (sender instanceof Player) {
            final Location loc = ((Player) sender).getLocation();
            return switch (index) {
                case 0 -> List.of(String.format("%.3f", loc.getX()), new StringBuilder(String.format("%.3f", loc.getX())).append(" ").append(String.format("%.3f", loc.getY())).append(" ").append(String.format("%.3f", loc.getZ())).toString());
                case 1 -> List.of(String.format("%.3f", loc.getY()), new StringBuilder(String.format("%.3f", loc.getY())).append(" ").append(String.format("%.3f", loc.getZ())).toString());
                case 2 -> List.of(String.format("%.3f", loc.getZ()));
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 3) {
            this.onTeleportToLocation(sender, args[0], args[1], args[2]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/tploc <x> <y> <z>");
    }

    @SubCommand
    public void onTeleportToLocation(CommandSender sender, String stringX, String stringY, String stringZ) {
        if (sender instanceof Player executor) {
            final Double x = Numbers.parseDouble(stringX), y = Numbers.parseDouble(stringY), z = Numbers.parseDouble(stringZ);
            if (x != null && y != null && z != null) {
                final Location loc = new Location(executor.getWorld(), x, y, z);
                if (executor.getWorld().getWorldBorder().isInside(loc)) {
                    PaperLib.teleportAsync(executor, loc).thenAccept((t) -> AzureLang.send(sender, AzureLang.TELEPORTED_TO_LOCATION.replace("{x}", stringX).replace("{y}", stringY).replace("{z}", stringZ)));
                    return;
                }
                AzureLang.send(sender, AzureLang.OUTSIDE_WORLD_BORDER);
                return;
            }
            AzureLang.send(sender, Global.INVALID_NUMBER);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
