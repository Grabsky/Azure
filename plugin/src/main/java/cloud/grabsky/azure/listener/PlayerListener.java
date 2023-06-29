package cloud.grabsky.azure.listener;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.Azure.Keys;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.empty;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerListener implements Listener {

    private final Azure plugin;

    @EventHandler // Applying custom kick message when banned player tries to join the server.
    public void onPlayerConnect(final @NotNull PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            final BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getPlayer().getName());
            // Skipping if no ban entry was found.
            if (entry == null)
                return;
            // ...
            // Preparing the kick message.
            final Component message = (entry.getExpiration() != null)
                    ? Message.of(PluginLocale.BAN_DISCONNECT_MESSAGE)
                            .placeholder("until", Interval.between(entry.getExpiration().getTime(), System.currentTimeMillis(), Unit.MILLISECONDS).toString())
                            .placeholder("expiration_date", entry.getExpiration().toString())
                            .placeholder("reason", entry.getReason() != null ? entry.getReason() : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                            .parse()
                    : Message.of(PluginLocale.BAN_DISCONNECT_MESSAGE_PERMANENT)
                            .placeholder("reason", entry.getReason() != null ? entry.getReason() : PluginConfig.PUNISHMENT_SETTINGS_DEFAULT_REASON)
                            .parse();
            // Setting the kick message, unless null.
            if (message != null)
                event.kickMessage(message);
        }
    }

    @EventHandler
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
        // ...
        if (player.getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true) {
            // Hiding join message.
            event.joinMessage(empty());
            // Showing BossBar.
            player.showBossBar(PluginConfig.VANISH_BOSS_BAR);
        }
        // Hiding vanished players.
        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(other -> other.getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true)
                .forEach(other -> player.hidePlayer(plugin, other));
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true)
            // Hiding quit message.
            event.quitMessage(empty());
    }

    @EventHandler
    public void onGameModeChange(final @NotNull PlayerGameModeChangeEvent event) {
        if (event.getPlayer().getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true) {
            event.setCancelled(true);
            event.cancelMessage(empty());
        }
    }

}
