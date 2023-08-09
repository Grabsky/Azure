package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@Command(name = "give", permission = "azure.command.give", usage = "/give (player) (material) (amount) (--silent)")
public final class GiveCommand extends RootCommand {

    private static final ExceptionHandler.Factory GIVE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_GIVE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return switch (index) {
            case 0 -> (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == true)
                    ? CompletionsProvider.of(Player.class)
                    : CompletionsProvider.of("@self");
            case 1 -> CompletionsProvider.of(Material.class);
            case 2 -> CompletionsProvider.of("1", "16", "32", "64");
            case 3 -> CompletionsProvider.of("--silent");
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final var target = arguments.next(Player.class).asRequired(GIVE_USAGE);
        final var material = arguments.next(Material.class).asRequired(GIVE_USAGE);
        final var amount = arguments.next(Integer.class, IntegerArgument.ofRange(1, 64)).asRequired(GIVE_USAGE);
        final var isSilent = arguments.next(String.class).asOptional("--not-silent").equalsIgnoreCase("--silent");
        // ...
        if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        // ...
        final ItemStack item = new ItemStack(material, amount);
        final Component display = text(amount + "x ").append(translatable(item.translationKey()).hoverEvent(item.asHoverEvent()));
        // ...
        target.getInventory().addItem(item);
        // ...
        if (sender != target) {
            Message.of(PluginLocale.COMMAND_GIVE_SUCCESS_SENDER)
                    .placeholder("player", target)
                    .placeholder("amount", amount)
                    .placeholder("item", display)
                    .send(sender);
        }
        // ...
        if (isSilent == false) {
            Message.of(PluginLocale.COMMAND_GIVE_SUCCESS_TARGET)
                    .placeholder("amount", amount)
                    .placeholder("item", display)
                    .send(target);
        }
    }

}
