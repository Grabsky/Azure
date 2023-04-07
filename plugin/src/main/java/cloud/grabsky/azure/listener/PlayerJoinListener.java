package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerJoinListener implements Listener {

    private final Azure plugin;

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Sending resource pack 1 tick after event is fired. (if enabled)
        if (PluginConfig.RESOURCE_PACK_SEND_ON_JOIN == true) {
            plugin.getBedrockScheduler().run(1L, (task) -> event.getPlayer().setResourcePack(
                    PluginConfig.RESOURCE_PACK_URL,
                    PluginConfig.RESOURCE_PACK_HASH,
                    PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                    PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
            ));
        }
    }

}
