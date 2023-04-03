package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AzureCommand extends RootCommand {

    public AzureCommand() {
        super("azure", null, "azure.command.azure", "/azure", null);
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return (index == 0)
                ? (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".reload") == true)
                        ? CompletionsProvider.of("reload")
                        : CompletionsProvider.EMPTY
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        if (queue.hasNext() == false) {
            // Sending message to command sender.
            Message.of(PluginLocale.COMMAND_AZURE_HELP).send(sender);
            return;
        } else if (queue.next(String.class).asRequired().equalsIgnoreCase("reload") == true && sender.hasPermission(this.getPermission() + ".reload") == true) {
            if (Azure.getInstance().reloadConfiguration() == true) {
                // Sending success message to command sender.
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}
