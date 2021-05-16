package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand {

    public LoreCommand() {
        CommandAPI.unregister("enchant");
    }

    public void register() {
        this.onLoreChange().register();
    }

    public CommandAPICommand onLoreChange() {
        return new CommandAPICommand("lore")
                .withPermission("skydistrict.command.lore")
                .withSubcommand(new CommandAPICommand("set")
                        .withPermission("skydistrict.command.lore.set")
                        .withArguments(new GreedyStringArgument("text"))
                        .executesPlayer((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Updating lore of ItemStack held by player
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                final List<Component> lines = new ArrayList<>();
                                for (String line : String.valueOf(args[0]).split("\\|")){
                                    lines.add(Component.empty().decoration(TextDecoration.ITALIC, false).append(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
                                }
                                meta.lore(lines);
                                item.setItemMeta(meta);
                                // Sending message
                                Lang.send(sender, Lang.LORE_UPDATED);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        })
                ).withSubcommand(new CommandAPICommand("add")
                        .withPermission("skydistrict.command.lore.add")
                        .withArguments(new StringArgument("text"))
                        .executesPlayer((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Updating lore of ItemStack held by player
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                final List<Component> lines = new ArrayList<>(meta.lore());
                                lines.add(Component.empty().decoration(TextDecoration.ITALIC, false).append(LegacyComponentSerializer.legacyAmpersand().deserialize(String.valueOf(args[0]))));
                                meta.lore(lines);
                                item.setItemMeta(meta);
                                // Sending message
                                Lang.send(sender, Lang.LORE_UPDATED);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        })
                ).withSubcommand(new CommandAPICommand("remove")
                        .withPermission("skydistrict.command.lore.remove")
                        .withArguments(new IntegerArgument("line", 0))
                        .executesPlayer((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Updating lore of ItemStack held by player
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                if (meta.hasLore()) {
                                    final List<Component> lines = new ArrayList<>(meta.lore());
                                    if (lines.size() > (int) args[0]) {
                                        lines.remove((int) args[0]);
                                        meta.lore(lines);
                                        item.setItemMeta(meta);
                                        // Sending message
                                        Lang.send(sender, Lang.LORE_UPDATED);
                                        return;
                                    }

                                }
                                Lang.send(sender, Lang.ITEM_HAS_NO_LORE);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        })
                ).withSubcommand(new CommandAPICommand("clear")
                        .withPermission("skydistrict.command.lore.clear")
                        .executesPlayer((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Clearing lore of ItemStack held by player
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                if (meta.hasLore()) {
                                    meta.lore(new ArrayList<>());
                                    item.setItemMeta(meta);
                                    // Sending message
                                    Lang.send(sender, Lang.LORE_CLEARED);
                                    return;
                                }
                                Lang.send(sender, Lang.ITEM_HAS_NO_LORE);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        })
                );
    }

}
