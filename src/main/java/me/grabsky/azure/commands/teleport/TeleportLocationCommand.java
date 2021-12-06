package me.grabsky.azure.commands.teleport;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Numbers;
import me.grabsky.libs.paperlib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportLocationCommand extends BaseCommand {
    private final Azure instance;

    public TeleportLocationCommand(Azure instance) {
        super("teleportlocation", List.of("tploc"), "azure.command.teleportlocation", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (sender instanceof Player) {
            final Location loc = ((Player) sender).getLocation();
            return switch (index) {
                case 0 -> List.of(String.format("%.3f", loc.getX()), new StringBuilder(String.format("%.3f", loc.getX())).append(" ").append(String.format("%.3f", loc.getY())).append(" ").append(String.format("%.3f", loc.getZ())).toString());
                case 1 -> List.of(String.format("%.3f", loc.getY()), new StringBuilder(String.format("%.3f", loc.getY())).append(" ").append(String.format("%.3f", loc.getZ())).toString());
                case 2 -> List.of(String.format("%.3f", loc.getZ()));
                case 3 -> Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        switch (args.length) {
            case 3 -> this.onTeleportToLocation(sender, args[0], args[1], args[2], null);
            case 4 -> this.onTeleportToLocation(sender, args[0], args[1], args[2], args[3]);
            default -> this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/tploc <x> <y> <z> [world]");
    }

    @SubCommand
    public void onTeleportToLocation(CommandSender sender, String stringX, String stringY, String stringZ, String worldName) {
        if (sender instanceof Player executor) {
            final Double x = Numbers.parseDouble(stringX), y = Numbers.parseDouble(stringY), z = Numbers.parseDouble(stringZ);
            if (x != null && y != null && z != null) {
                final World world = (worldName != null) ? Bukkit.getWorld(worldName) : executor.getWorld();
                if (world != null) {
                    final Location loc = new Location(world, x, y, z, executor.getLocation().getYaw(), executor.getLocation().getPitch());
                    if (world.getWorldBorder().isInside(loc)) {
                        PaperLib.teleportAsync(executor, loc).thenAccept((t) -> AzureLang.send(sender, AzureLang.TELEPORTED_TO_LOCATION.replace("{x}", stringX).replace("{y}", stringY).replace("{z}", stringZ)));
                        return;
                    }
                    AzureLang.send(sender, AzureLang.OUTSIDE_WORLD_BORDER);
                    return;
                }
                AzureLang.send(sender, Global.WORLD_NOT_FOUND);
                return;
            }
            AzureLang.send(sender, Global.INVALID_NUMBER);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
