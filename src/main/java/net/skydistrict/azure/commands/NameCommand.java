package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NameCommand {

    public void register() {
        this.onNameChange().register();
    }

    public CommandAPICommand onNameChange() {
        return new CommandAPICommand("name")
                .withPermission("skydistrict.command.name")
                .withArguments(new GreedyStringArgument("name"))
                .executesPlayer((sender, args) -> {
                    if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                        // Updating name of ItemStack held by player
                        final ItemStack item = sender.getInventory().getItemInMainHand();
                        final ItemMeta meta = item.getItemMeta();
                        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(String.valueOf(args[0])));
                        item.setItemMeta(meta);
                        // Sending message
                        Lang.send(sender, Lang.ITEM_NAME_UPDATED);
                        return;
                    }
                    Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                });
    }
}
