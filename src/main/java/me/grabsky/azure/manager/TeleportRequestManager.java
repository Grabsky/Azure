package me.grabsky.azure.manager;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportRequestManager {
    private final Azure instance;
    private final Map<UUID, TeleportRequest> requests;

    public TeleportRequestManager(Azure instance) {
        this.instance = instance;
        this.requests = new HashMap<>();
        this.runCleanTask();
    }

    public boolean addRequest(UUID player, UUID target) {
        if (!requests.containsKey(player) || requests.get(player).getExpirationDate() <= System.currentTimeMillis()) {
            requests.put(player, new TeleportRequest(target));
            return true;
        }
        return false;
    }

    public boolean removeRequest(UUID player) {
        if (requests.containsKey(player)) {
            requests.remove(player);
            return true;
        }
        return false;
    }

    public boolean hasRequestFrom(UUID player, UUID target) {
        for (Map.Entry<UUID, TeleportRequest> en : requests.entrySet()) {
            if (en.getValue().getTarget() == player) {
                return true;
            }
        }
        return false;
    }

    private void clean() {
        for (Map.Entry<UUID, TeleportRequest> en : requests.entrySet()) {
            if (en.getValue().getExpirationDate() <= System.currentTimeMillis()) {
                requests.remove(en.getKey());
            }
        }
    }

    public void runCleanTask() {
        instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, this::clean, 6000, 6000);
    }

    public void teleport(Player player, Player target, int delay) {
        if (delay == 0 || player.hasPermission("skydistrict.bypass.azure.teleportdelay")) {
            if (player.teleport(target, PlayerTeleportEvent.TeleportCause.COMMAND)) {
                AzureLang.send(player, AzureLang.TELEPORTED_TO_PLAYER.replace("{player}", target.getName()));
                AzureLang.send(target, AzureLang.PLAYER_TELEPORTED_TO_YOU.replace("{player}", player.getName()));
                return;
            }
            AzureLang.send(player, AzureLang.TELEPORT_FAILED);
            return;
        }
        final Location initialLoc = player.getLocation();
        new BukkitRunnable() {
            int secondsLeft = delay;
            @Override
            public void run() {
                secondsLeft--;
                if (initialLoc.distance(player.getLocation()) > 1D) {
                    AzureLang.send(player, AzureLang.TELEPORT_CANCELLED);
                    this.cancel();
                } else if (secondsLeft == 0) {
                    if (player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                        AzureLang.send(player, AzureLang.TELEPORTED_TO_PLAYER.replace("{player}", target.getName()));
                        AzureLang.send(target, AzureLang.PLAYER_TELEPORTED_TO_YOU.replace("{player}", player.getName()));
                        return;
                    }
                    AzureLang.send(player, AzureLang.TELEPORT_FAILED);
                    this.cancel();
                }
            }
        }.runTaskTimer(instance, 0L, 20L);
    }
}
