package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class NickCommand extends BaseCommand {
    private final Azure instance;
    private final NamespacedKey customNameKey;

    public NickCommand(Azure instance) {
        super("nick", null, "azure.command.nick", ExecutorType.ALL);
        this.instance = instance;
        this.customNameKey = new NamespacedKey(instance, "customName");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return null;
        else return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else if (args.length == 1) {
            this.onNick(sender, args[0]);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        if (sender instanceof Player executor) {
            executor.displayName(Component.empty());
        }
    }

    @SubCommand
    public void onNick(CommandSender sender, String nick) {
        if (sender instanceof Player executor) {
            final Component component = Components.parseAmpersand(nick);
            final String nickNoColors = Components.restorePlain(component);
            if (nickNoColors.length() > 3 && nickNoColors.length() < 16) {
                if (nickNoColors.matches("[a-zA-Z0-9_]+")) {
                    executor.displayName(component);
                    final PersistentDataContainer container = executor.getPersistentDataContainer();
                    container.set(customNameKey, PersistentDataType.STRING, nick);
                    System.out.println(1);
                    // SUCCESS
                    return;
                }
                System.out.println(2);
                // INVALID CHARACTERS
                return;
            }
            // INVALID LENGTH
            System.out.println(3);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
