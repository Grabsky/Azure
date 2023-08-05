package cloud.grabsky.azure.features;

import cloud.grabsky.azure.Azure;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// NOTE: The reason why this is hard-coded is because there is pretty much nothing to change.
// NOTE: Implementation does not use PlayerArmorChangeEvent because of following reasons:
//         - Current implementation shouldn't have much impact on performance.
//         - Current implementation is simple and cannot be exploited.
//         - Mentioned event does not cover all possible cases of the armor change. (though it may in the future)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ExtraItems implements Listener {

    private final @NotNull Azure plugin;

    private static @Nullable BukkitTask TASK = null;

    private static final NamespacedKey KEY_HAS_JUMP_BOOST = new NamespacedKey("azure", "item/has_jump_boost");
    private static final NamespacedKey KEY_HAS_SPEED_BOOST = new NamespacedKey("azure", "item/has_speed_boost");

    public void initialize() {
        // Unregistering events to make sure no logic is duplicated
        HandlerList.unregisterAll(this);
        // Registering event(s).
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Cancelling existing task to make sure no logic is duplicated.
        if (TASK != null)
            TASK.cancel();
        // Scheduling a new repeating task.
        TASK = plugin.getBedrockScheduler().repeat(20L, 20L, Long.MAX_VALUE, (___) -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getInventory().getBoots() != null && player.getInventory().getBoots().hasItemMeta() == true) {
                    final PersistentDataContainer container = player.getInventory().getBoots().getItemMeta().getPersistentDataContainer();
                    if (container.has(KEY_HAS_SPEED_BOOST) == true)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 21, 0, false, false, false));
                    if (container.has(KEY_HAS_JUMP_BOOST) == true) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 21, 3, false, false, false));
                    }
                }
            });
            return true;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(final EntityDamageEvent event) {
        if (event.getCause() != DamageCause.FALL)
            return;
        // ...
        if (event.getEntity() instanceof Player player)
            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().hasItemMeta() == true) {
                final PersistentDataContainer container = player.getInventory().getBoots().getItemMeta().getPersistentDataContainer();
                if (container.has(KEY_HAS_JUMP_BOOST) == true)
                    event.setCancelled(true);
            }
    }

}
