package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.Azure.Keys;
import cloud.grabsky.azure.configuration.PluginConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.empty;

public final class PlayerListener implements Listener {

    private final Azure plugin;

    public PlayerListener(final @NotNull Azure plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(final @NotNull PlayerRespawnEvent event) {
        // Setting respawn location to spawn point of the primary world. (if enabled)
        if (PluginConfig.GENERAL_RESPAWN_ON_PRIMARY_WORLD_SPAWN == true) {
            // Getting the primary world.
            final World primaryWorld = plugin.getWorldManager().getPrimaryWorld();
            // Setting the respawn location.
            event.setRespawnLocation(plugin.getWorldManager().getSpawnPoint(primaryWorld));
        }
    }

    @EventHandler // NOTE: Some resource pack sending changes might be necessary once 1.20.2 is live. (see 23w31a)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Clearing title. (if enabled)
        if (PluginConfig.GENERAL_CLEAR_TITLE_ON_JOIN == true)
            player.clearTitle();
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            plugin.getBedrockScheduler().run(1L, (task) -> player.setResourcePack(
                    PluginConfig.RESOURCE_PACK_URL,
                    PluginConfig.RESOURCE_PACK_HASH,
                    PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                    PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
            ));
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true)
            // Hiding quit message.
            event.quitMessage(empty());
    }

    @EventHandler(ignoreCancelled = true) // Enables void damge to invulnerable players.
    public void onVoidDamage(final @NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && player.isInvulnerable() == true && event.getCause() != EntityDamageEvent.DamageCause.VOID)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) // Disables hunger loss of invulnerable players.
    public void onHungerLoss(final @NotNull FoodLevelChangeEvent event) {
        if (event.getEntity().getFoodLevel() > event.getFoodLevel() && event.getEntity() instanceof Player player && player.isInvulnerable() == true)
            event.setCancelled(true);
    }

}
