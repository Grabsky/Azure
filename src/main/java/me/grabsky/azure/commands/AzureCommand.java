package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AzureCommand extends BaseCommand {
    private final Azure instance;

    public AzureCommand(@NotNull Azure instance) {
        super("azure", null, "firedot.command.azure", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return List.of("reload");
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.onReload(sender);
            return;
        }
        this.onDefault(sender);
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, AzureLang.PLUGIN_HELP);
    }

    @SubCommand
    public void onReload(CommandSender sender) {
        if (sender.hasPermission("firedot.command.azure.reload")) {
            if (instance.reload()) {
                AzureLang.send(sender, Global.RELOAD_SUCCESS);
            } else {
                AzureLang.send(sender, Global.RELOAD_FAIL);
            }
            return;
        }
        AzureLang.send(sender, Global.MISSING_PERMISSIONS);
    }
}
