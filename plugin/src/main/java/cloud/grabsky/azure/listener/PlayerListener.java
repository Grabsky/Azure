package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.empty;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerListener implements Listener {

    private final Azure plugin;

    private static final NamespacedKey IS_VANISHED = new NamespacedKey("azure", "is_vanished");

    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            plugin.getBedrockScheduler().run(1L, (task) -> player.setResourcePack(
                    PluginConfig.RESOURCE_PACK_URL,
                    PluginConfig.RESOURCE_PACK_HASH,
                    PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                    PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
            ));
        }
        // ...
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(IS_VANISHED, PersistentDataType.BYTE, (byte) 0) == (byte) 1)
            event.joinMessage(empty());
        // TO-DO: Hide vanished players...
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(IS_VANISHED, PersistentDataType.BYTE, (byte) 0) == (byte) 1)
            event.quitMessage(empty());
    }

    @EventHandler
    public void onGameModeChange(final @NotNull PlayerGameModeChangeEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(IS_VANISHED, PersistentDataType.BYTE, (byte) 0) == (byte) 1) {
            event.setCancelled(true);
            event.cancelMessage(empty());
        }
    }

}
