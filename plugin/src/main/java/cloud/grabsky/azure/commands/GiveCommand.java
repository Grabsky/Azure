package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.translatable;

public final class GiveCommand extends RootCommand {

    public GiveCommand() {
        super("give", null, "azure.command.give", "/give <player> <material> <amount>", "Gives an item.");
    }

    private static final ExceptionHandler.Factory SEND_USAGE_ON_MISSING_INPUT = (exception) -> {
        if (exception instanceof MissingInputException) return (_0, context) -> {
            context.getExecutor().asCommandSender().sendMessage(PluginLocale.COMMAND_GIVE_USAGE);
        };
        // DEFAULT HANDLER
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return switch (index) {
            case 0 -> CompletionsProvider.of(Player.class);
            case 1 -> CompletionsProvider.of(Material.class);
            case 2 -> CompletionsProvider.of("1", "16", "32", "64");
            case 3 -> CompletionsProvider.of("--silent");
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final RootCommandContext context, final ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // arguments
        final var target = arguments.next(Player.class).asRequired(SEND_USAGE_ON_MISSING_INPUT);
        final var material = arguments.next(Material.class).asRequired(SEND_USAGE_ON_MISSING_INPUT);
        final var amount = arguments.next(Integer.class, IntegerArgument.ofRange(1, 64)).asRequired(SEND_USAGE_ON_MISSING_INPUT);
        final var flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
        // ...
        target.getInventory().addItem(new ItemStack(material, amount));
        // message
        Message.of(PluginLocale.COMMAND_GIVE_SENDER)
                .placeholder("player", target)
                .placeholder("amount", amount)
                .placeholder("material", translatable(material.translationKey()))
                .send(sender);
        // ...
        if (sender != target && containsIgnoreCase(flags, "--silent") == false) {
            Message.of(PluginLocale.COMMAND_GIVE_TARGET)
                    .placeholder("amount", amount)
                    .placeholder("material", translatable(material.translationKey()))
                    .send(target);
        }
    }

    private static boolean containsIgnoreCase(final String[] arr, final String search) {
        for (final String element : arr) {
            if (search.equalsIgnoreCase(element) == true)
                return true;
        }
        return false;
    }

}
