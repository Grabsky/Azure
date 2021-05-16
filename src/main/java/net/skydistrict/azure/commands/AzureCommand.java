package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AdventureChatComponentArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import net.kyori.adventure.text.Component;
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
                )
                .withArguments(new EntitySelectorArgument("players", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executes((sender, args) -> {
                    sender.sendMessage("test");
                })
                .withSubcommand(new CommandAPICommand("test")
                        .withArguments(new AdventureChatComponentArgument("component"))
                        .executes((sender, args) -> {
                            sender.sendMessage((Component) args[0]);
                        })
                );
    }
}
