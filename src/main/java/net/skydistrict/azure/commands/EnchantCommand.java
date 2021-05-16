package net.skydistrict.azure.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import net.skydistrict.azure.config.Lang;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantCommand {

    public void register() {
        this.onEnchantsChange().register();
    }

    public CommandAPICommand onEnchantsChange() {
        return new CommandAPICommand("enchant")
                .withPermission("skydistrict.command.enchant")
                .withSubcommand(new CommandAPICommand("add")
                        .withPermission("skydistrict.command.enchant.add")
                        .withArguments(new EnchantmentArgument("enchantment"), new IntegerArgument("level"))
                        .executesPlayer(((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Adding new enchantment
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                meta.addEnchant((Enchantment) args[0], (int) args[1], true);
                                item.setItemMeta(meta);
                                // Sending message
                                Lang.send(sender, Lang.ITEM_ENCHANTS_UPDATED);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        }))
                ).withSubcommand(new CommandAPICommand("remove")
                        .withPermission("skydistrict.command.enchant.remove")
                        .withArguments(new EnchantmentArgument("enchantment"))
                        .executesPlayer((sender, args) -> {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                // Updating name of ItemStack held by player
                                final ItemStack item = sender.getInventory().getItemInMainHand();
                                final ItemMeta meta = item.getItemMeta();
                                if (meta.hasEnchant((Enchantment) args[0])) {
                                    meta.removeEnchant((Enchantment) args[0]);
                                    item.setItemMeta(meta);
                                    // Sending message
                                    Lang.send(sender, Lang.ITEM_ENCHANTS_UPDATED);
                                    return;
                                }
                                Lang.send(sender, Lang.ITEM_NO_SUCH_ENCHANTMENT);
                                return;
                            }
                            Lang.send(sender, Lang.NO_ITEM_IN_HAND);
                        })
                );
    }
}
