package me.grabsky.azure.listener;

import me.grabsky.azure.Azure;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerInvulnerableListener implements Listener {
    private final Azure instance;

    public PlayerInvulnerableListener(Azure instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player player) {
            if (player.isInvulnerable() && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player player) {
            if (player.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }
}
