package me.grabsky.azure.commands.homes;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.azure.storage.objects.JsonPlayer;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SetHomeCommand extends BaseCommand {
    private final Azure instance;
    private final PlayerDataManager data;

    public SetHomeCommand(Azure instance) {
        super("sethome", null, "azure.command.sethome", ExecutorType.PLAYER);
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (sender instanceof Player executor) {
            final Set<String> homes = data.getOnlineData(executor).getHomes();
            if (index == 0 && homes != null && !homes.isEmpty()) return new ArrayList<>(homes);
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            this.onHomeSet(sender, args[0]);
        } else {
            this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, Global.CORRECT_USAGE + "/sethome <id>");
    }

    @SubCommand
    public void onHomeSet(CommandSender sender, String id) {
        final Player executor = (Player) sender;
        final JsonPlayer jsonPlayer = data.getOnlineData(executor);
        if (jsonPlayer.getHomes().size() < 10) {
            if (id.matches("[a-zA-Z0-9_-]+")) {
                jsonPlayer.setHome(id, executor.getLocation());
                AzureLang.send(sender, AzureLang.HOME_SET.replace("{id}", id));
                return;
            }
            AzureLang.send(sender, AzureLang.INVALID_CHARACTERS);
            return;
        }
        AzureLang.send(sender, AzureLang.HOME_LIMIT_EXCEEDED);
    }
}
