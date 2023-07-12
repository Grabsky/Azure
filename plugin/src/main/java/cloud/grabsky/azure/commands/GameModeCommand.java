package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.translatable;

public final class GameModeCommand extends RootCommand {

    private final Azure plugin;

    public GameModeCommand(final @NotNull Azure plugin) {
        super("gamemode", null, "azure.command.gamemode", "/gamemode (player) (gamemode)", "...");
        // ...
        this.plugin = plugin;
    }

    private static final ExceptionHandler.Factory GAMEMODE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_GAMEMODE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return switch (index) {
            case 0 -> (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == true)
                            ? CompletionsProvider.of(Player.class)
                            : CompletionsProvider.of("@self");
            case 1 -> CompletionsProvider.of(GameMode.class);
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        final Player target = arguments.next(Player.class).asRequired(GAMEMODE_USAGE);
        final @Nullable GameMode mode = arguments.next(GameMode.class).asOptional();
        // ...
        if (mode == null) {
            Message.of(PluginLocale.COMMAND_GAMEMODE_INFO)
                    .placeholder("player", target)
                    .placeholder("mode", translatable(target.getGameMode().translationKey()))
                    .send(sender);
            return;
        }
        // ...
        if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }
        target.setGameMode(mode);
        // ...
        if (sender != target)
            Message.of(PluginLocale.COMMAND_GAMEMODE_SET_SUCCESS_SENDER)
                    .placeholder("player", target)
                    .placeholder("mode", translatable(mode.translationKey()))
                    .send(sender);
        // ...
        Message.of(PluginLocale.COMMAND_GAMEMODE_SET_SUCCESS_TARGET)
                .placeholder("mode", translatable(mode.translationKey()))
                .send(target);
        // Getting user object.
        final User targetUser = plugin.getUserCache().getUser(target);
        // Disabling vanish state if enabled.
        if (targetUser.isVanished() == true) {
            targetUser.setVanished(false, false);
            // Sending message to the player.
            Message.of(PluginLocale.COMMAND_VANISH_SUCCESS_TARGET).placeholder("state", PluginLocale.DISABLED.color(NamedTextColor.RED)).send(target);
        }
    }

}
