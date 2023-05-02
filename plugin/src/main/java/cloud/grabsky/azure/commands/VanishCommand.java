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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class VanishCommand extends RootCommand {

    private static final NamespacedKey IS_VANISHED = new NamespacedKey("azure", "is_vanished");

    public VanishCommand() {
        super("vanish", null, "azure.command.vanish", "/vanish (target) (true/false)", "Modify in-game visibility.");
    }

    private static final ExceptionHandler.Factory VANISH_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_SPEED_USAGE).send(context.getExecutor().asCommandSender());
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
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            // ...
            final boolean isVanished = isVanished(sender);
            // ...
            setVanished(context.getManager().getPlugin(), sender, !isVanished);
            return;
        } else if (context.getExecutor().asCommandSender().hasPermission(this.getPermission() + ".others") == true) {
            final CommandSender sender = context.getExecutor().asCommandSender();
            // ...
            final Player target = arguments.next(Player.class).asRequired(VANISH_USAGE);
            final Boolean mode = arguments.next(Boolean.class).asOptional();
            // ...
            final boolean nextVanishState = (mode != null) ? mode : !isVanished(target);
            // ...
            setVanished(context.getManager().getPlugin(), target, nextVanishState);
        }
    }

    private static boolean isVanished(final @NotNull Player target) {
        return target.getPersistentDataContainer().getOrDefault(IS_VANISHED, PersistentDataType.BYTE, (byte) 0) == (byte) 1;
    }

    private static void setVanished(final @NotNull Plugin plugin, final @NotNull Player target, final boolean state) {
        if (state == true) {
            // ...
            target.setGameMode(GameMode.SPECTATOR);
            // ...
            target.getPersistentDataContainer().set(IS_VANISHED, PersistentDataType.BYTE, (byte) 1);
            // TO-DO: Show BossBar...
            // ...
            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("azure.bypass.see_vanished_players") == false).forEach(player -> {
                player.hidePlayer(plugin, target);
            });
            return;
        }
        // ...
        target.getPersistentDataContainer().set(IS_VANISHED, PersistentDataType.BYTE, (byte) 0);
        // TO-DO: Hide BossBar...
        // ...
        // ...
        final GameMode nextGameMode = (target.getPreviousGameMode() != null)
                ? (target.hasPermission("azure.plugin.vanish_switch_previous_gamemode") == true)
                        ? target.getPreviousGameMode()
                        : Bukkit.getDefaultGameMode()
                : Bukkit.getDefaultGameMode();
        // ...
        target.setGameMode(nextGameMode);
        // ...
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("azure.bypass.see_vanished_players") == false).forEach(player -> {
            player.hidePlayer(plugin, target);
        });
    }

}
