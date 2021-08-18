package net.skydistrict.azure.commands;

import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import net.skydistrict.azure.Azure;
import net.skydistrict.azure.config.Lang;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AzureCommand extends BaseCommand {
    private final Azure instance;

    public AzureCommand(@NotNull Azure instance) {
        super("azure", null, "skydistrict.command.azure", ExecutorType.ALL);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return Collections.singletonList("reload");
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("skydistrict.command.azure.reload")) {
                if (instance.reload(false)) {
                    Lang.send(sender, Global.RELOAD_SUCCESS);
                } else {
                    Lang.send(sender, Global.RELOAD_FAIL);
                }
                return;
            }
            Lang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        Lang.send(sender, Lang.PLUGIN_HELP);
    }
}
