package cloud.grabsky.azure.commands;


import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;

public final class AzureCommand extends RootCommand {

    public AzureCommand() {
        super("azure", null, "azure.command.azure", "/azure", null);
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, int index) {
        return (index == 0)
                ? CompletionsProvider.of("reload")
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final RootCommandContext context, final ArgumentQueue queue) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (queue.next(String.class).asRequired().equalsIgnoreCase("reload") == true) {
            if (Azure.getInstance().reloadConfiguration() == true) {
                sendMessage(sender, PluginLocale.COMMAND_AZURE_RELOAD_SUCCESS);
                return;
            }
            sendMessage(sender, PluginLocale.COMMAND_AZURE_RELOAD_FAILURE);
        }
    }

}
