package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.user.AzureUser;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

// TO-DO: Store vanish state in JSON.
// TO-DO: Weight based checks, rather than permission based.
public final class VanishCommand extends RootCommand {

    private final Azure plugin;

    public VanishCommand(final @NotNull Azure plugin) {
        super("vanish", null, "azure.command.vanish", "/vanish (target) (true/false)", "Modify in-game visibility.");
        // ...
        this.plugin = plugin;
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> CompletionsProvider.of(Boolean.class);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            // ...
            final AzureUser user = (AzureUser) plugin.getUserCache().getUser(sender);
            // ...
            final boolean nextVanishState = !user.isVanished();
            // Changing vanish state.
            user.setVanished(nextVanishState);
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextVanishState == true)).send(sender);
            return;
        }
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final Player target = arguments.next(Player.class).asRequired();
        final Boolean state = arguments.next(Boolean.class).asOptional();
        // ...
        if (sender != target && context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // ...
        final AzureUser targetUser = (AzureUser) plugin.getUserCache().getUser(target);
        // ...
        final boolean nextVanishState = (state != null) ? state : !targetUser.isVanished();
        // ...
        targetUser.setVanished(nextVanishState);

        if (sender != target) {
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS)
                    .placeholder("player", target)
                    .placeholder("state", getColoredBooleanLong(nextVanishState == true))
                    .send(sender);
            return;
        }
        Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", getColoredBooleanLong(nextVanishState == true)).send(target);
    }

    private Component getColoredBooleanLong(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanLong(true).color(GREEN) : PluginLocale.getBooleanLong(false).color(RED);
    }

}
