package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public final class PackCommand extends RootCommand {

    public PackCommand() {
        super("pack", null, "azure.command.pack", "/pack", "...");
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
                ? CompletionsProvider.of(Stream.of("apply", "notify").filter(it -> context.getExecutor().asCommandSender().hasPermission(this.getPermission() + "." + it) == true).toList())
                : (context.getInput().at(1).equalsIgnoreCase("notify") == true)
                        ? CompletionsProvider.of("--confirm")
                        : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        switch (arguments.next(String.class).asOptional("help").toLowerCase()) {
            default -> Message.of(PluginLocale.COMMAND_PACK_HELP).send(context.getExecutor().asCommandSender());
            case "apply" -> {
                context.getExecutor().asPlayer().setResourcePack(
                        PluginConfig.RESOURCE_PACK_URL,
                        PluginConfig.RESOURCE_PACK_HASH,
                        PluginConfig.RESOURCE_PACK_IS_REQUIRED,
                        PluginConfig.RESOURCE_PACK_PROMPT_MESSAGE
                );
            }
            case "notify" -> {
                final CommandSender sender = context.getExecutor().asCommandSender();
                // ...
                if (sender.hasPermission(this.getPermission() + ".notify") == true) {
                    final boolean isConfirm = arguments.next(String.class).asOptional("--no-confirm").equalsIgnoreCase("--confirm");
                    // Checking for --confirm flag.
                    if (isConfirm == true) {
                        // Iterating over online players.
                        Bukkit.getServer().getOnlinePlayers().forEach(it -> {
                            // Playing notification sound.
                            if (PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND != null)
                                it.playSound(PluginConfig.RESOURCE_PACK_NOTIFICATION_SOUND);
                            // Sending notification message.
                            Message.of(PluginLocale.COMMAND_PACK_NOTIFICATION).send(it);
                        });
                        return;
                    }
                    Message.of(PluginLocale.COMMAND_PACK_NOTIFY_CONFIRM).replace("<input>", context.getInput().toString()).send(sender);
                    return;
                }
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            }
        }
    }

}
