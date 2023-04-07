package cloud.grabsky.azure.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.ArrayList;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public enum CommandFilterListener implements Listener {
    /* SINGLETON */ INSTANCE;

    private static final ArrayList<String> EMPTY = new ArrayList<>();

    private static final Component UNKNOWN_COMMAND = text(Bukkit.spigot().getSpigotConfig().getString("messages.unknown-command", ""));

    public static Set<String> BLACKLISTED_COMMANDS;

    @EventHandler
    public void onCommandSend(final PlayerCommandSendEvent event) {
        event.getCommands().removeIf(it -> BLACKLISTED_COMMANDS.contains(it) == true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().replaceFirst("/", "").replaceFirst("/", "");
        if (BLACKLISTED_COMMANDS.stream().anyMatch(command::startsWith) == true) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(UNKNOWN_COMMAND);
        }
    }


}
