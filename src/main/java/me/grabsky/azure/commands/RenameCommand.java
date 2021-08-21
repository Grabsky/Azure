package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class RenameCommand extends BaseCommand {

    private final Azure instance;

    public RenameCommand(Azure instance) {
        super("rename", null, "skydistrict.command.rename", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else {
            this.onItemRename(sender, String.join(" ", args));
        }
    }

    public void onDefault(CommandSender sender) {
        final Player executor = (Player) sender;
        final ItemStack item = executor.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            final ItemMeta meta = item.getItemMeta();
            meta.displayName(null);
            item.setItemMeta(meta);
            AzureLang.send(sender, AzureLang.NAME_CLEARED);
            return;
        }
        AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
    }

    public void onItemRename(CommandSender sender, String name) {
        final Player executor = (Player) sender;
        final ItemStack item = executor.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            final ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(Components.parseAmpersand(name)));
            item.setItemMeta(meta);
            AzureLang.send(sender, AzureLang.NAME_UPDATED);
            return;
        }
        AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
    }
}
