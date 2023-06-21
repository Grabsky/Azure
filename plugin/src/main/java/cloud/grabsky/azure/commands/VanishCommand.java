package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure.Keys;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class VanishCommand extends RootCommand {

    public VanishCommand() {
        super("vanish", null, "azure.command.vanish", "/vanish (target) (true/false)", "Modify in-game visibility.");
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
            final boolean isVanished = isVanished(sender);
            // ...
            if (setVanished(context.getManager().getPlugin(), sender, !isVanished) == true) {
                Message.of(!isVanished == true ? PluginLocale.VANISH_SUCCESS_STATE_ON : PluginLocale.VANISH_SUCCESS_STATE_OFF).send(sender);
            }
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
        final boolean nextVanishState = (state != null) ? state : !isVanished(target);
        // ...
        if (setVanished(context.getManager().getPlugin(), target, nextVanishState) == true) {
            // ...
            if (sender != target) {
                Message.of(nextVanishState == true ? PluginLocale.VANISH_SUCCESS_STATE_ON_TARGET : PluginLocale.VANISH_SUCCESS_STATE_OFF_TARGET)
                        .placeholder("target", target)
                        .send(target);
                return;
            }
            Message.of(nextVanishState == true ? PluginLocale.VANISH_SUCCESS_STATE_ON : PluginLocale.VANISH_SUCCESS_STATE_OFF).send(target);
        }
    }

    private static boolean isVanished(final @NotNull Player target) {
        return target.getPersistentDataContainer().getOrDefault(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false) == true;
    }

    private static boolean setVanished(final @NotNull Plugin plugin, final @NotNull Player target, final boolean state) {
        if (state == true && isVanished(target) == false) {
            target.getPersistentDataContainer().set(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, true);
            // Showing BossBar.
            target.showBossBar(PluginConfig.VANISH_BOSS_BAR);
            // Switching game mode to spectator.
            target.setGameMode(GameMode.SPECTATOR);
            // Hiding target from other players.
            Bukkit.getOnlinePlayers().stream().filter(player -> player != target && player.hasPermission("azure.bypass.see_vanished_players") == false).forEach(player -> {
                player.hidePlayer(plugin, target);
            });
            return true;
        } else if (state == false && isVanished(target) == true) {
            target.getPersistentDataContainer().set(Keys.IS_VANISHED, PersistentDataType.BOOLEAN, false);
            // Hiding BossBar.
            target.hideBossBar(PluginConfig.VANISH_BOSS_BAR);
            // ...
            final GameMode nextGameMode = (target.getPreviousGameMode() != null)
                    ? (target.hasPermission("azure.plugin.vanish_switch_previous_gamemode") == true)
                            ? target.getPreviousGameMode()
                            : Bukkit.getDefaultGameMode()
                    : Bukkit.getDefaultGameMode();
            // Switching to previous, or default game mode.
            target.setGameMode(nextGameMode);
            // Showing target to other players.
            Bukkit.getOnlinePlayers().stream().filter(player -> player != target).forEach(player -> {
                player.showPlayer(plugin, target);
            });
            return true;
        }
        return false;
    }

}
