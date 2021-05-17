package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.skydistrict.azure.Azure;
import net.skydistrict.azure.config.Lang;

public class AzureCommand {

    public void register() {
        this.onAzure().register();
    }

    public CommandAPICommand onAzure() {
        return new CommandAPICommand("azure")
                .withAliases("az")
                .withPermission("skydistrict.command.azure")
                .executes((sender, args) -> {
                    Lang.send(sender, Lang.AZURE_HELP);
                })
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("skydistrict.command.azure.reload")
                        .executes((sender, args) -> {
                            if (Azure.getInstance().reload(false)) {
                                Lang.send(sender, Lang.AZURE_RELOAD_SUCCESS);
                                return;
                            }
                            Lang.send(sender, Lang.AZURE_RELOAD_FAIL);
                        })
                );
    }
}
