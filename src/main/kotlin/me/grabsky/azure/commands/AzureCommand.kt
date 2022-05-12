package me.grabsky.azure.commands

import indigo.libraries.lamp.annotation.Command
import indigo.libraries.lamp.annotation.Default
import indigo.libraries.lamp.annotation.Subcommand
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.libraries.lamp.help.CommandHelp
import indigo.plugin.configuration.ServerLocale
import indigo.plugin.extensions.sendMessageOrIgnore
import me.grabsky.azure.Azure
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

@Command("azure")
@CommandPermission("azure.command.azure")
class AzureCommand(private val azure: Azure) {

    @Default
    fun onDefault(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(ServerLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(ServerLocale.COMMAND_USAGE_DIVIDER)
    }

    @Subcommand("reload")
    @CommandPermission("azure.command.azure.reload")
    fun onReload(sender: CommandSender) {
        if (azure.reloadPlugin()) {
            sender.sendMessageOrIgnore(ServerLocale.RELOAD_SUCCES)
            return
        }
        sender.sendMessageOrIgnore(ServerLocale.RELOAD_FAIL)
    }
}