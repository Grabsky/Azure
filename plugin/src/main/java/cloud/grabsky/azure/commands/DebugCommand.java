/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Command(name = "debug", permission = "azure.command.debug", usage = "/debug (...)")
public final class DebugCommand extends RootCommand implements Listener {

    private @Dependency Azure plugin;


    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of("refresh_listeners", "refresh_recipes", "delete_entity", "modify") : CompletionsProvider.EMPTY;
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
                        if (entity != null)
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
                case "compress" -> {
                    final File source = new File(plugin.getDataFolder(), "_rsc");
                    final File target = new File(plugin.getDataFolder(), "_rsc.zip");
                }
            }
        }
    }



}
