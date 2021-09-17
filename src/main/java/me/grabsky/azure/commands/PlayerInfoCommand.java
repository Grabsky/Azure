package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import me.grabsky.indigo.utils.Beautifier;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerInfoCommand extends BaseCommand {
    private final Azure instance;
    private final PlayerDataManager data;

    public PlayerInfoCommand(Azure instance) {
        super("playerinfo", List.of("pi"), "azure.command.playerinfo", ExecutorType.ALL);
        this.instance = instance;
        this.data = instance.getDataManager();
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        if (index == 0) return UserCache.getNamesOfOnlineUsers();
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else {
            this.onPlayerInfo(sender, args[0]);
        }
    }

    @DefaultCommand
    public void onDefault(final CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/playerinfo <player>");
    }

    // TO-DO: Add IP filter for players without proper permissions
    @SubCommand
    public void onPlayerInfo(final CommandSender sender, final String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            final Location loc = player.getLocation();
            final String address = (sender.hasPermission("azure.command.playerinfo.view.address")) ? player.getAddress().getHostName() : "§o*****§r";
            final JsonPlayer jsonPlayer = instance.getDataManager().getOnlineData(player.getUniqueId());
            // TO-DO: Ban plugin support
            AzureLang.send(sender, AzureLang.PLAYERINFO_ONLINE
                    .replace("{name}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString().substring(0, 13))
                    .replace("{ip}", address)
                    .replace("{country}", jsonPlayer.getCountry())
                    .replace("{ping}", this.coloredPing(player.getPing()))
                    .replace("{first_join}", Beautifier.DD_MM_YYYY.format(player.getFirstPlayed()))
                    .replace("{time_played}", Beautifier.ONE_DECIMAL_PLACE.format(player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20D / 60D / 60D))
                    .replace("{client}", (player.getClientBrandName() != null) ? player.getClientBrandName() : "N/A")
                    .replace("{version}", this.version(player.getProtocolVersion()))
                    .replace("{online_since}", Beautifier.formatTimestamp(System.currentTimeMillis() - player.getLastLogin()))
                    .replace("{x}", Beautifier.THREE_DECIMAL_PLACES.format(loc.getX()))
                    .replace("{y}", Beautifier.THREE_DECIMAL_PLACES.format(loc.getY()))
                    .replace("{z}", Beautifier.THREE_DECIMAL_PLACES.format(loc.getZ()))
                    .replace("{world}", loc.getWorld().getName())
                    .replace("{gamemode}", this.gamemode(player.getGameMode()))
                    .replace("{is_flying}", (player.isFlying()) ? Global.YES : Global.NO)
                    .replace("{is_invulnerable}", (player.isInvulnerable()) ? Global.YES : Global.NO)
                    .replace("{health}", Beautifier.ONE_DECIMAL_PLACE.format(player.getHealth() / 2D))
                    .replace("{hunger}", Beautifier.ONE_DECIMAL_PLACE.format(player.getFoodLevel() / 2D))
                    .replace("{level}", String.valueOf(player.getLevel()))
                    .replace("{progress}", String.valueOf(Math.round(player.getExp() * 100)))
                    // TO-DO: Actual implementation
                    .replace("{is_banned}", Global.NO)
                    .replace("{is_muted}", Global.NO)
                    .replace("{warns}", String.valueOf(0))
            );
            return;
        } else if (UserCache.contains(playerName)) {
            final User user = UserCache.get(playerName);
            final UUID uuid = user.getUniqueId();
            if (data.hasDataOf(uuid)) {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                data.getOfflineData(uuid, true).thenAccept((jsonPlayer) -> {
                    if (jsonPlayer != null) {
                        final Location lastLocation = jsonPlayer.getLastLocation().toLocation();
                        AzureLang.send(sender, AzureLang.PLAYERINFO_OFFLINE
                                .replace("{name}", user.getName())
                                .replace("{uuid}", user.getUniqueId().toString().substring(0, 13))
                                .replace("{ip}", jsonPlayer.getLastAddress())
                                .replace("{country}", jsonPlayer.getCountry())
                                .replace("{time_played}", Beautifier.ONE_DECIMAL_PLACE.format(offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20D / 60D / 60D))
                                .replace("{offline_since}", Beautifier.formatTimestamp(System.currentTimeMillis() - offlinePlayer.getLastSeen()))
                                .replace("{x}", Beautifier.THREE_DECIMAL_PLACES.format(lastLocation.getX()))
                                .replace("{y}", Beautifier.THREE_DECIMAL_PLACES.format(lastLocation.getY()))
                                .replace("{z}", Beautifier.THREE_DECIMAL_PLACES.format(lastLocation.getZ()))
                                .replace("{world}", lastLocation.getWorld().getName())
                                // TO-DO: Actual implementation
                                .replace("{is_banned}", Global.NO)
                                .replace("{is_muted}", Global.NO)
                                .replace("{warns}", String.valueOf(0))
                        );
                    }
                });
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
    }

    private String gamemode(final GameMode gamemode) {
        return switch (gamemode) {
            case ADVENTURE -> Global.GAMEMODE_ADVENTURE;
            case CREATIVE -> Global.GAMEMODE_CREATIVE;
            case SPECTATOR -> Global.GAMEMODE_SPECTATOR;
            case SURVIVAL -> Global.GAMEMODE_SURVIVAL;
        };
    }

    private String coloredPing(final int num) {
        if (num < 60) return "§a" + num;
        if (num < 120) return "§e" + num;
        return "§c" + num;
    }

    private String version(final int protocolVersion) {
        return switch (protocolVersion) {
            case 755 -> "1.17";
            case 756 -> "1.17.1";
            case 757 -> "1.18 (?)";
            default -> "Unknown";
        };
    }
}
