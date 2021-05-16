package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import io.papermc.lib.PaperLib;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportCommandBundle {

    public TeleportCommandBundle() {
        // Unregistering vanilla commands
        CommandAPI.unregister("teleport", true);
        CommandAPI.unregister("tp", true);
    }

    public void register() {
        this.onTeleportToPlayer().register();
        this.onTeleportToLocation().register();
        this.onTeleportPlayerToPlayer().register();
        this.onTeleportPlayerToLocation().register();
        this.onPlayerTeleportHere().register();
    }

    public CommandAPICommand onTeleportToPlayer() {
        return new CommandAPICommand("teleport")
                .withAliases("tp")
                .withPermission("skydistrict.command.teleport")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executesPlayer((sender, args) -> {
                    final Player target = (Player) args[0];
                    sender.teleport(target);
                    Lang.send(sender, Lang.TELEPORTED_TO_PLAYER
                            .replace("{target}", target.getName())
                    );
                });
    }

    public CommandAPICommand onTeleportToLocation() {
        return new CommandAPICommand("teleport")
                .withAliases("tp", "tploc", "tppos")
                .withPermission("skydistrict.command.teleport.location")
                .withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
                .executesPlayer((sender, args) -> {
                    final Location location = (Location) args[0];
                    PaperLib.teleportAsync(sender, location).complete(true);
                    Lang.send(sender, Lang.TELEPORTED_TO_LOCATION
                            .replace("{x}", String.valueOf(location.getX()))
                            .replace("{y}", String.valueOf(location.getY()))
                            .replace("{z}", String.valueOf(location.getZ()))
                    );
                });
    }

    public CommandAPICommand onTeleportPlayerToPlayer() {
        return new CommandAPICommand("teleport")
                .withAliases("tp")
                .withPermission("skydistrict.command.teleport.others")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executes((sender, args) -> {
                    final Player player = (Player) args[0];
                    final Player target = (Player) args[1];
                    player.teleport(target);
                    Lang.send(sender, Lang.TELEPORTED_PLAYER_TO_PLAYER
                            .replace("{player}", player.getName())
                            .replace("{target}", target.getName())
                    );
                });
    }

    public CommandAPICommand onTeleportPlayerToLocation() {
        return new CommandAPICommand("teleport")
                .withAliases("tp", "tploc", "tppos")
                .withPermission("skydistrict.command.teleport.location.others")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
                .executes((sender, args) -> {
                    final Player player = (Player) args[0];
                    final Location location = (Location) args[1];
                    PaperLib.teleportAsync(player, location).complete(true);
                    Lang.send(sender, Lang.TELEPORTED_PLAYER_TO_LOCATION
                            .replace("{player}", player.getName())
                            .replace("{x}", String.valueOf(location.getX()))
                            .replace("{y}", String.valueOf(location.getY()))
                            .replace("{z}", String.valueOf(location.getZ()))
                    );
                });
    }

    public CommandAPICommand onPlayerTeleportHere() {
        return new CommandAPICommand("teleporthere")
                .withAliases("tphere", "s")
                .withPermission("skydistrict.command.teleport.others")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executesPlayer((sender, args) -> {
                    final Player player = (Player) args[0];
                    player.teleport(sender);
                    Lang.send(sender, Lang.PLAYER_TELEPORTED_TO_YOU.replace("{player}", player.getName()));
                });
    }
}
