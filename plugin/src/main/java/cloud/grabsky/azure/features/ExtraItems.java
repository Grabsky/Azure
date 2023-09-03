/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.features;

import cloud.grabsky.azure.Azure;
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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

// NOTE: This really should not be part of this plugin.
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
