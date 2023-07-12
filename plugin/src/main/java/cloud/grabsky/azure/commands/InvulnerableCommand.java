package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InvulnerableCommand extends RootCommand {

    public InvulnerableCommand() {
        super("invulnerable", null, "azure.command.invulnerable", "/invulnerable (player) (state)", null);
    }

    private static final ExceptionHandler.Factory INVULNERABLE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_INVULNERABLE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

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
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Getting target player.
        final Player target = (arguments.hasNext() == false && sender instanceof Player senderPlayer)
                ? arguments.next(Player.class).asOptional(senderPlayer)
                : arguments.next(Player.class).asRequired(INVULNERABLE_USAGE);
        // Getting the state.
        final boolean state = (arguments.hasNext() == false)
                ? arguments.next(Boolean.class).asOptional(!target.isInvulnerable()) // Negating current state.
                : arguments.next(Boolean.class).asRequired(INVULNERABLE_USAGE);
        // Exiting command block in case specified target is different from sender, and sender does not have permissions to "use" other players.
        if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // Making target invulnerable.
        target.setInvulnerable(state);
        // Sending success message to the sender. (if applicable)
        if (sender != target)
            Message.of(PluginLocale.COMMAND_INVULNERABLE_SUCCESS)
                    .placeholder("player", target)
                    .placeholder("state", getColoredBooleanLong(state == true))
                    .send(sender);
        // Sending success message to the target.
        Message.of(PluginLocale.COMMAND_INVULNERABLE_SUCCESS_TARGET)
                .placeholder("state", getColoredBooleanLong(state == true))
                .send(target);
    }

    private Component getColoredBooleanLong(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanLong(true).color(GREEN) : PluginLocale.getBooleanLong(false).color(RED);
    }

}