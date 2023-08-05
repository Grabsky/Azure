package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends RootCommand implements Listener {

    private final @NotNull Azure plugin;

    public DebugCommand(final @NotNull Azure plugin) {
        super("debug", null, "azure.command.debug", "/debug", null);
        // ...
        this.plugin = plugin;
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of("refresh_listeners", "refresh_recipes", "delete_entity") : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (arguments.hasNext() == true) {
            final String argument = arguments.next(String.class).asRequired().toLowerCase();
            // ...
            switch (argument) {
                case "refresh_listeners" -> {
                    // Unregistering existing...
                    HandlerList.unregisterAll(this);
                    // Registering...
                    plugin.getServer().getPluginManager().registerEvents(this, plugin);
                    // ...
                    Message.of("Listeners has been refreshed.").send(sender);
                }
                case "delete_entity" -> {
                    if (sender instanceof Player senderPlayer) {
                        final Entity entity = senderPlayer.getTargetEntity(20);
                        if (entity instanceof LivingEntity == true)
                            entity.remove();
                    }
                }
                case "refresh_recipes" -> {
                    // ...
                }
                case "modify" -> {
                    if (sender instanceof Player senderPlayer) {
                        final ItemStack item = senderPlayer.getInventory().getItemInMainHand();
                        // ...
                        if (item.getType() == Material.AIR)
                            return;
                        // ...
                        item.editMeta(meta -> {
                            final PersistentDataContainer container = meta.getPersistentDataContainer();
                        });
                    }
                }
            }
        }
    }

}
