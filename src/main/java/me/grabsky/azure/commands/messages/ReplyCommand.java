package me.grabsky.azure.commands.messages;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReplyCommand extends BaseCommand {
    private final PlayerDataManager data;

    public ReplyCommand(Azure instance) {
        super("reply", List.of("r"), "azure.command.reply", ExecutorType.PLAYER);
        this.data = instance.getDataManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            this.onReply(sender, args);
            return;
        }
        this.onDefault(sender);
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/reply <text>");
    }

    public void onReply(CommandSender sender, String[] args) {
        final Player executor = (Player) sender;
        final UUID lastRecipient = data.getOnlineData(executor).getLastRecipient();
        if (lastRecipient != null) {
            final Player recipient = Bukkit.getPlayer(lastRecipient);
            if (recipient != null && recipient.isOnline()) {
                final String format = AzureLang.PRIVATE_MESSAGE_FORMAT.replace("{sender}", sender.getName()).replace("{recipient}", recipient.getName());
                final String message = String.join(" ", args);
                final Component privateMessage = Components.parseSection(format + (sender.hasPermission("azure.chat.format") ? message.replace("&", "§") : message));
                // Sending component (private message) to command sender
                AzureLang.send(sender, privateMessage);
                // Sending component (private message) to recipient
                AzureLang.send(recipient, privateMessage, Identity.identity(executor.getUniqueId()));
                // Sending component (private message) to staff with social spy enabled
                final String socialSpyFormat = AzureLang.SOCIAL_SPY_FORMAT.replace("{sender}", sender.getName()).replace("{recipient}", recipient.getName());
                final Component socialSpyMessage = Components.parseSection(socialSpyFormat + message);
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    // Skipping if player is message sender or receiver
                    if (player == executor || player == recipient) continue;
                    final JsonPlayer jsonPlayer = data.getOnlineData(player.getUniqueId());
                    if (jsonPlayer != null && jsonPlayer.getSocialSpy()) {
                        AzureLang.send(player, socialSpyMessage);
                    }
                }
                return;
            }
        }
        AzureLang.send(sender, AzureLang.NO_PLAYER_TO_REPLY);
    }
}
