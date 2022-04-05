package me.grabsky.azure.commands

import me.grabsky.azure.Azure
import me.grabsky.indigo.configuration.GlobalLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.libs.lamp.annotation.Command
import me.grabsky.libs.lamp.annotation.Default
import me.grabsky.libs.lamp.annotation.Subcommand
import me.grabsky.libs.lamp.bukkit.annotation.CommandPermission
import me.grabsky.libs.lamp.help.CommandHelp
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

@Command("azure")
@CommandPermission("azure.command.azure")
class AzureCommand(private val azure: Azure) {

    @Default
    fun onDefault(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    @Subcommand("reload")
    @CommandPermission("azure.command.azure.reload")
    fun onReload(sender: CommandSender) {
        if (azure.reloadPluginConfiguration()) {
            sender.sendMessageOrIgnore(GlobalLocale.RELOAD_SUCCES)
            return
        }
        sender.sendMessageOrIgnore(GlobalLocale.RELOAD_FAIL)
    }
}