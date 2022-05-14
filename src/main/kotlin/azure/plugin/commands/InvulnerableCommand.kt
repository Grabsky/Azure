package azure.plugin.commands

import azure.plugin.configuration.Locale
import indigo.framework.commands.SingleEntitySelector
import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.*
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.plugin.extensions.sendMessageOrIgnore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

enum class Toggle(val boolean: Boolean) {
    ON(true),
    OFF(false)
}

@Command("invulnerable", "in")
@CommandPermission("azure.command.invulnerable")
class InvulnerableCommand {

    @Default
    @Usage("<target> [on/off] [-s]")
    fun onDefault(sender: CommandSender, selectorOne: SingleEntitySelector<Player>, @Optional toggle: Toggle?, @Optional @Switch("s") silent: Boolean?) {
        // Getting player from selector
        val target = selectorOne.entity
        if (sender != target && sender.hasPermission("azure.command.invulnerable.others") == false) return // We should throw NoPermissionException here if possible
        // Updating mode
        target.isInvulnerable = toggle?.boolean ?: !target.isInvulnerable
        // Making sure not to send two similar messages to player
        if (sender != target) {
            sender.sendMessageOrIgnore(this.messageSender(target.isInvulnerable), Placeholders.BADGES, Placeholder.unparsed("target", target.name))
        }
        // Sending information messages if -s (silent) flag is not true
        if (silent != true) {
            target.sendMessageOrIgnore(this.messageTarget(target.isInvulnerable))
        }
    }

    private fun messageTarget(bool: Boolean): Component? {
        if (bool == true) return Locale.INVULNERABLE_ON
        return Locale.INVULNERABLE_OFF
    }

    private fun messageSender(bool: Boolean): String? {
        if (bool == true) return Locale.INVULNERABLE_OTHERS_ON
        return Locale.INVULNERABLE_OTHERS_OFF
    }
}