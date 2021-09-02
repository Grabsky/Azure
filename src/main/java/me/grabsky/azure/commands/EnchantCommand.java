package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.util.Enchantments;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.utils.Numbers;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnchantCommand extends BaseCommand {
    private final Azure instance;
    private final List<String> enchantments;

    public EnchantCommand(@NotNull Azure instance) {
        super("enchant", null, "firedot.command.enchant", ExecutorType.ALL);
        this.instance = instance;
        this.enchantments = List.of("aqua_affinity", "bane_of_arthropods", "binding_curse", "blast_protection", "channeling", "depth_strider", "efficiency", "feather_falling", "fire_aspect", "fire_protection", "flame", "fortune", "frost_walker", "impaling", "infinity", "knockback", "looting", "loyalty", "luck_of_the_sea", "lure", "mending", "multishot", "piercing", "power", "projectile_protection", "protection", "punch", "quick_charge", "respiration", "riptide", "sharpness", "silk_touch", "smite", "soul_speed", "sweeping", "thorns", "unbreaking", "vanishing_curse");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String arg, int index) {
        if (index == 0) return List.of("add", "remove");;
        if (index == 1) return enchantments;
        System.out.println(arg);
        if (index == 2 && arg.equalsIgnoreCase("add")) return List.of("1");
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Command: /enchant
        if (args.length == 0) {
            this.onDefault(sender);
            return;
        }
        // Command: /enchant (arg)
        switch (args[0]) {
            case "add" -> {
                if (args.length == 3) {
                    this.onEnchantAdd(sender, args[1], args[2]);
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/enchant add <enchantment> [level]");
            }
            case "remove" -> {
                if (args.length == 2) {
                    this.onEnchantRemove(sender, args[1]);
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/enchant remove <enchantment>");
            }
            default -> this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, AzureLang.ENCHANT_USAGE);
    }

    public void onEnchantAdd(CommandSender sender, String enchantmentName, String stringLevel) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.enchant.add")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    final Enchantment enchantment = Enchantments.fromName(enchantmentName);
                    if (enchantment != null) {
                        final Integer level = Numbers.parseInt(stringLevel);
                        if (level != null && level > 0) {
                            final ItemMeta meta = item.getItemMeta();
                            meta.addEnchant(enchantment, level, true);
                            item.setItemMeta(meta);

                            AzureLang.send(executor, AzureLang.ENCHANTMENTS_UPDATED);
                            return;
                        }
                        AzureLang.send(executor, Global.INVALID_NUMBER);
                        return;
                    }
                    AzureLang.send(executor, AzureLang.ENCHANTMENT_NOT_FOUND);
                    return;
                }
                AzureLang.send(executor, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    public void onEnchantRemove(CommandSender sender, String enchantmentName) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.enchant.remove")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    final Enchantment enchantment = Enchantments.fromName(enchantmentName);
                    if (enchantment != null) {
                        final ItemMeta meta = item.getItemMeta();
                        if (meta.hasEnchant(enchantment)) {
                            meta.removeEnchant(enchantment);
                            item.setItemMeta(meta);
                            AzureLang.send(executor, AzureLang.ENCHANTMENTS_UPDATED);
                            return;
                        }
                        AzureLang.send(executor, AzureLang.ITEM_HAS_NO_SUCH_ENCHANTMENT);
                        return;
                    }
                    AzureLang.send(executor, AzureLang.ENCHANTMENT_NOT_FOUND);
                    return;
                }
                AzureLang.send(executor, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
