package me.grabsky.azure.commands

import me.grabsky.azure.configuration.Locale
import me.grabsky.indigo.configuration.GlobalLocale
import me.grabsky.indigo.extensions.clearEnchantments
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.libs.lamp.annotation.*
import me.grabsky.libs.lamp.bukkit.annotation.CommandPermission
import me.grabsky.libs.lamp.help.CommandHelp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

private val parser = MiniMessage.builder()
    .editTags {
        it.resolver(StandardTags.color())
        it.resolver(StandardTags.decorations())
        it.resolver(StandardTags.gradient())
        it.resolver(StandardTags.rainbow())
    }
    .build()

private val COMPONENT_WITH_NO_ITALIC = Component.empty().decoration(TextDecoration.ITALIC, false)

@Command("editor")
@CommandPermission("azure.command.editor")
class EditorCommand {

    @Default
    @CommandPermission("azure.command.editor")
    fun onEditorHelp(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    // Editor => Name
    @Subcommand("name")
    @CommandPermission("azure.command.editor.name")
    fun onEditorName(sender: Player, @Optional name: String?) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            if (name != null) {
                it.displayName(COMPONENT_WITH_NO_ITALIC.append(parser.deserialize(name)))
                sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_NAME_UPDATED)
                return@editMeta
            }
            it.displayName(null)
            sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_NAME_RESET)
            return@editMeta
        }
    }

    // Editor => Lore
    @Default @Subcommand("lore")
    @CommandPermission("azure.command.editor.lore")
    fun onEditorLoreHelp(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    // Editor => Lore => Add
    @Subcommand("lore add")
    @CommandPermission("azure.command.editor.lore.add")
    @Usage("<text>")
    fun onEditorLoreAdd(sender: Player, text: String) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        if (sender.inventory.itemInMainHand.type != Material.AIR) {
            sender.inventory.itemInMainHand.editMeta {
                val parsedLine = COMPONENT_WITH_NO_ITALIC.append(parser.deserialize(text))
                it.lore((it.lore() ?: arrayListOf<Component>()) + parsedLine)
                sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_LORE_UPDATED)
                return@editMeta
            }
        }
        sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
    }

    // Editor => Lore => Set
    @Subcommand("lore set")
    @CommandPermission("azure.command.editor.lore.set")
    @Usage("<line> <text>")
    fun onEditorLoreSet(sender: Player, @Range(min = 1.0) line: Int, @Optional text: String) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            if ((it.lore()?.size ?: 0) >= line) {
                val parsedLine = COMPONENT_WITH_NO_ITALIC.append(parser.deserialize(text))
                it.lore((it.lore() ?: arrayListOf<Component>()) + parsedLine)
                sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_LORE_UPDATED)
                return@editMeta
            }
            sender.sendMessageOrIgnore(GlobalLocale.COMMAND_NUMBER_NOT_IN_RANGE, Placeholder.unparsed("input", line.toString()), Placeholder.unparsed("min", "1"), Placeholder.unparsed("max", it.lore()?.size.toString()))
            return@editMeta
        }
        return
    }

    // Editor => Lore => Remove
    @Subcommand("lore remove")
    @CommandPermission("azure.command.editor.lore.remove")
    @Usage("<line>")
    fun onEditorLoreRemove(sender: Player, line: Int) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            if ((it.lore()?.size ?: 0) >= line) {
                it.lore(it.lore()!!.filterIndexed { index, _ -> index == (line - 1) })
                sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_LORE_UPDATED)
                return@editMeta
            }
            sender.sendMessageOrIgnore(GlobalLocale.COMMAND_NUMBER_NOT_IN_RANGE, Placeholder.unparsed("input", line.toString()), Placeholder.unparsed("min", "1"), Placeholder.unparsed("max", it.lore()?.size.toString()))
            return@editMeta
        }
    }

    // Editor => Lore => Reset
    @Subcommand("lore reset")
    @CommandPermission("azure.command.editor.lore.clear")
    fun onEditorLoreClear(sender: Player) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            it.lore(null)
            sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_LORE_RESET)
            return@editMeta
        }
    }

    // Editor => Enchant
    @Default @Subcommand("enchant")
    @CommandPermission("azure.command.editor.enchant")
    fun onEditorEnchantHelp(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    // Editor => Enchant -> Add
    @Subcommand("enchant add")
    @CommandPermission("azure.command.editor.enchant.add")
    @Usage("<enchant> [level]")
    fun onEditorEnchantAdd(sender: Player, enchantment: Enchantment, @Default("1") level: Int) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            it.addEnchant(enchantment, level, true)
            sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_ENCHANT_ADDED, Placeholder.unparsed("enchantment", enchantment.key.asString()))
        }
    }

    // Editor => Enchant => Remove
    @Subcommand("enchant remove")
    @CommandPermission("azure.command.editor.enchant.remove")
    @Usage("<enchant>")
    fun onEditorEnchantRemove(sender: Player, enchantment: Enchantment) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            it.removeEnchant(enchantment)
            sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_ENCHANT_REMOVED, Placeholder.unparsed("enchantment", enchantment.key.asString()))
        }
    }

    // Editor => Enchant => Reset
    @Subcommand("enchant reset")
    @CommandPermission("azure.command.editor.enchant.reset")
    fun onEditorEnchantReset(sender: Player, commandHelp: CommandHelp<Component>) {
        // Ignoring if player has no item in hand
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessageOrIgnore(GlobalLocale.NO_ITEM_IN_HAND)
            return
        }
        // Editing ItemMeta
        sender.inventory.itemInMainHand.editMeta {
            it.clearEnchantments()
            sender.sendMessageOrIgnore(Locale.EDITOR_ITEM_ENCHANT_RESET)
        }
    }
}