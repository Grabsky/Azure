package me.grabsky.azure.commands.homes;

import me.grabsky.azure.Azure;
import me.grabsky.azure.storage.PlayerDataManager;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

// TO-DO: Actual implementation
public class HomesCommand extends BaseCommand {
    private final Azure instance;
    private final PlayerDataManager data;

    public HomesCommand(Azure instance) {
        super("homes", null, "azure.command.homes", ExecutorType.ALL);
        this.instance = instance;
        this.data = instance.getDataManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return null;
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {

    }

    public void onHomesOthers(CommandSender sender, String playerName) {

    }
}
