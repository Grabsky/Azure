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

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

@Command(name = "repair", permission = "azure.command.repair", usage = "/repair")
public final class RepairCommand extends RootCommand {

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Checking if item in hand is not air.
        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
            // Checking if item in hand can be repaired.
            if (sender.getInventory().getItemInMainHand() instanceof Damageable) {
                // Reparing item and sending success message to the sender.
                sender.getInventory().getItemInMainHand().editMeta(Damageable.class, (meta) -> meta.setDamage(0));
                Message.of(PluginLocale.COMMAND_REPAIR_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.COMMAND_REPAIR_FAILURE_ITEM_NOT_REPAIRABLE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.COMMAND_REPAIR_FAILURE_NO_ITEM_IN_HAND).send(sender);
    }

}
