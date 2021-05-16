package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageCommandBundle {

    private final Map<UUID, UUID> lastRecipients;

    public MessageCommandBundle() {
        this.lastRecipients = new HashMap<>();
        // Unregistering vanilla commands
        CommandAPI.unregister("tell");
        CommandAPI.unregister("msg");
        CommandAPI.unregister("w");
    }

    public void register() {
        this.onPlayerMessagePlayer().register();
        this.onPlayerReply().register();
    }

    public CommandAPICommand onPlayerMessagePlayer() {
        return new CommandAPICommand("message")
                .withAliases("msg", "tell", "w", "dm", "pm")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .withArguments(new GreedyStringArgument("message"))
                .executesPlayer((sender, args) -> {
                    final Player recipient = (Player) args[0];
                    if (!sender.equals(recipient)) {
                        // Updating last recipients
                        lastRecipients.put(sender.getUniqueId(), recipient.getUniqueId());
                        lastRecipients.put(recipient.getUniqueId(), sender.getUniqueId());
                        final Component privateMessage = LegacyComponentSerializer
                                .legacySection().deserialize(new StringBuilder()
                                        .append(Lang.PRIVATE_MESSAGE_FORMAT
                                                .replace("{sender}", sender.getName())
                                                .replace("{recipient}", recipient.getName()))
                                        .append(args[1]).toString());
                        // Sending component (private message) to command sender
                        Lang.send(sender, privateMessage);
                        // Sending component (private message) to recipient
                        Lang.send(recipient, privateMessage, Identity.identity(sender.getUniqueId()));
                        // TO-DO: Send component to staff with social spy enabled
                        return;
                    }
                    Lang.send(sender, Lang.CANT_USE_ON_YOURSELF);
                });
    }

    public CommandAPICommand onPlayerReply() {
        return new CommandAPICommand("reply")
                .withAliases("r")
                .withArguments(new GreedyStringArgument("message"))
                .executesPlayer((sender, args) -> {
                    final Player recipient = Bukkit.getPlayer(lastRecipients.get(sender.getUniqueId()));
                    if (recipient != null && recipient.isOnline()) {
                        // Formatting private message component
                        final Component privateMessage = LegacyComponentSerializer
                                .legacySection().deserialize(new StringBuilder()
                                        .append(Lang.PRIVATE_MESSAGE_FORMAT
                                                .replace("{sender}", sender.getName())
                                                .replace("{recipient}", recipient.getName()))
                                        .append(args[0]).toString());
                        // Sending component (private message) to command sender
                        Lang.send(sender, privateMessage);
                        // Sending component (private message) to recipient
                        Lang.send(recipient, privateMessage, Identity.identity(sender.getUniqueId()));
                        // TO-DO: Send component to staff with social spy enabled
                        return;
                    }
                    Lang.send(sender, Lang.NO_PLAYER_TO_REPLY);
                });
    }
}
