package me.grabsky.azure.commands;

import me.grabsky.azure.Azure;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.utils.Components;
import me.grabsky.indigo.utils.Numbers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoreCommand extends BaseCommand {

    private final Azure instance;

    public LoreCommand(Azure instance) {
        super("lore", null, "firedot.command.lore", ExecutorType.PLAYER);
        this.instance = instance;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String sub, int index) {
        if (index == 0) return List.of("add", "clear", "remove", "set");
        else switch (sub) {
            case "remove", "set" -> {
                if (index == 1) return List.of("1");
                else return Collections.emptyList();
            }
            default -> {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onDefault(sender);
        } else switch (args[0]) {
            case "add" -> {
                if (args.length > 1) {
                    this.onLoreAdd(sender, args);
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/lore add <lore...>");
            }
            case "set" -> {
                if (args.length > 2) {
                    this.onLoreSet(sender, args[1], args);
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/lore set <index> <lore...>");
            }
            case "remove" -> {
                if (args.length == 2) {
                    this.onLoreRemove(sender, args[1]);
                    return;
                }
                AzureLang.send(sender, Global.CORRECT_USAGE + "/lore remove <index>");
            }
            case "clear" -> this.onLoreClear(sender);
            default -> this.onDefault(sender);
        }
    }

    @DefaultCommand
    public void onDefault(CommandSender sender) {
        AzureLang.send(sender, AzureLang.LORE_USAGE);
    }

    @SubCommand
    public void onLoreAdd(CommandSender sender, String[] args) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.lore.add")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    // Convert args to lore
                    final String lore = String.join(" ", List.of(args).subList(1, args.length));
                    System.out.println(lore);
                    // Add new lore line
                    final ItemMeta meta = item.getItemMeta();
                    final List<Component> itemLore = (meta.hasLore()) ? new ArrayList<>(meta.lore()) : new ArrayList<>(); // This can't be null
                    itemLore.add(Component.empty().decoration(TextDecoration.ITALIC, false).append(Components.parseAmpersand(lore)));
                    meta.lore(itemLore);
                    item.setItemMeta(meta);
                    // Sending a message
                    AzureLang.send(sender, AzureLang.LORE_UPDATED);
                    return;
                }
                AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onLoreSet(CommandSender sender, String stringLineNumber, String[] args) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.lore.set")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    final Integer lineNumber = Numbers.parseInt(stringLineNumber);
                    if (lineNumber != null && lineNumber > 0) {
                        // Check if item has a lore
                        final ItemMeta meta = item.getItemMeta();
                        if (meta.hasLore()) {
                            if (meta.lore().size() > (lineNumber - 1)) { // This can't be null
                                // Convert args to lore
                                final String lore = String.join(" ", List.of(args).subList(1, args.length));
                                // Set n element of lore array to x
                                final List<Component> itemLore = new ArrayList<>(item.lore()); // This can't be null
                                itemLore.set(lineNumber - 1, Component.empty().decoration(TextDecoration.ITALIC, false).append(Components.parseAmpersand(lore)));
                                meta.lore(itemLore);
                                item.setItemMeta(meta);
                                // Sending a message
                                AzureLang.send(sender, AzureLang.LORE_UPDATED);
                                return;
                            }
                            AzureLang.send(sender, AzureLang.LORE_INDEX_TOO_HIGH.replace("{num}", String.valueOf(meta.lore().size())));
                            return;
                        }
                        AzureLang.send(sender, AzureLang.ITEM_HAS_NO_LORE);
                        return;
                    }
                    AzureLang.send(sender, AzureLang.LORE_INDEX_TOO_LOW);
                    return;
                }
                AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onLoreRemove(CommandSender sender, String stringLineNumber) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.lore.remove")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    final Integer lineNumber = Numbers.parseInt(stringLineNumber);
                    if (lineNumber != null) {
                        // Lore
                        final ItemMeta meta = item.getItemMeta();
                        if (meta.hasLore()) {
                            if (meta.lore().size() > (lineNumber - 1)) { // This can't be null
                                // Remove n element of lore array
                                final List<Component> itemLore = new ArrayList<>(item.lore()); // This can't be null
                                itemLore.remove(lineNumber - 1);
                                meta.lore(itemLore);
                                item.setItemMeta(meta);
                                // Sending a message
                                AzureLang.send(sender, AzureLang.LORE_UPDATED);
                                return;
                            }
                            AzureLang.send(sender, AzureLang.LORE_INDEX_TOO_HIGH.replace("{num}", String.valueOf(meta.lore().size())));
                            return;
                        }
                        AzureLang.send(sender, AzureLang.ITEM_HAS_NO_LORE);
                        return;
                    }
                    AzureLang.send(sender, AzureLang.LORE_INDEX_TOO_LOW);
                    return;
                }
                AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    public void onLoreClear(CommandSender sender) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("firedot.command.lore.clear")) {
                final ItemStack item = executor.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    final ItemMeta meta = item.getItemMeta();
                    if (meta.hasLore()) {
                        meta.lore(null);
                        item.setItemMeta(meta);
                        // Sending a message
                        AzureLang.send(sender, AzureLang.LORE_CLEARED);
                        return;
                    }
                    AzureLang.send(sender, AzureLang.ITEM_HAS_NO_LORE);
                    return;
                }
                AzureLang.send(sender, Global.NO_ITEM_IN_HAND);
                return;
            }
            AzureLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        AzureLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }
}
