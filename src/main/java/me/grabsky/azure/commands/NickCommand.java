package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.AzureKeys;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class NickCommand extends BaseCommand {
    private final Azure instance;

    public NickCommand(Azure instance) {
        super("nick", null, "azure.command.nick", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return null;
        else return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1 -> this.onNickSelf(sender, args[0]);
            case 2 -> this.onNickPlayer(sender, args[0], args[1]);
            default -> this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, AzureLang.NICK_USAGE);
    }

    @SubCommand
    public void onNickSelf(CommandSender sender, String nick) {
        if (sender instanceof Player executor) {
            if (!nick.equalsIgnoreCase("clear")) {
                final Component component = Components.parseAmpersand(nick);
                final String nickNoColors = Components.restorePlain(component);
                if (nickNoColors.length() > 3 && nickNoColors.length() < 16) {
                    if (nickNoColors.matches("[a-zA-Z0-9_]+")) {
                        final PersistentDataContainer container = executor.getPersistentDataContainer();
                        container.set(AzureKeys.CUSTOM_NAME, PersistentDataType.STRING, nick);
                        executor.displayName(component);
                        AzureLang.send(sender, AzureLang.NICK_SET.replace("{nick}", nick.replace("&", "§")));
                        return;
                    }
                    AzureLang.send(sender, AzureLang.NICK_INVALID_CHARACTERS);
                    return;
                }
                AzureLang.send(sender, AzureLang.NICK_INVALID_LENGTH);
                return;
            }
            final PersistentDataContainer container = executor.getPersistentDataContainer();
            container.remove(AzureKeys.CUSTOM_NAME);
            executor.displayName(null);
            AzureLang.send(sender, AzureLang.NICK_RESET);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onNickPlayer(CommandSender sender, String targetName, String nick) {
        if (sender.hasPermission("azure.command.nick.others")) {
            final Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                if (!nick.equalsIgnoreCase("clear")) {
                    final Component component = Components.parseAmpersand(nick);
                    final String nickNoColors = Components.restorePlain(component);
                    if (nickNoColors.length() > 3 && nickNoColors.length() < 16) {
                        if (nickNoColors.matches("[a-zA-Z0-9_]+")) {
                            target.displayName(component);
                            final PersistentDataContainer container = target.getPersistentDataContainer();
                            container.set(AzureKeys.CUSTOM_NAME, PersistentDataType.STRING, nick);
                            AzureLang.send(sender, AzureLang.NICK_SET.replace("{nick}", nick.replace("&", "§")));
                            return;
                        }
                        AzureLang.send(sender, AzureLang.NICK_INVALID_CHARACTERS);
                        return;
                    }
                    AzureLang.send(sender, AzureLang.NICK_INVALID_LENGTH);
                    return;
                }
                target.displayName(null);
                AzureLang.send(sender, AzureLang.NICK_RESET);
                return;
            }
            AzureLang.send(sender, Global.PLAYER_NOT_FOUND);
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }
}
