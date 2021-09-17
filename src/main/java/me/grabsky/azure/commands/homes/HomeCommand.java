package me.grabsky.azure.commands.homes;

import io.papermc.lib.PaperLib;
import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HomeCommand extends BaseCommand {
    private final Azure instance;
    private final PlayerDataManager data;

    public HomeCommand(Azure instance) {
        super("home", null, "azure.command.home", ExecutorType.PLAYER);
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (sender instanceof Player executor) {
            final Set<String> homes = data.getOnlineData(executor).getHomes();
            if (index == 0 && homes != null && !homes.isEmpty()) return new ArrayList<>(homes);
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            this.onHomeTeleport(sender, args[0]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/home <id>");
    }
    
    @SubCommand
    public void onHomeTeleport(CommandSender sender, String id) {
        final Player executor = (Player) sender;
        final JsonPlayer jsonPlayer = data.getOnlineData(executor);         
        if (jsonPlayer.hasHome(id)) {
            PaperLib.teleportAsync(executor, jsonPlayer.getHome(id));
            AzureLang.send(sender, AzureLang.TELEPORTED_TO_HOME.replace("{id}", id));
            return;
        }
        AzureLang.send(sender, AzureLang.HOME_NOT_FOUND);
    }
}
