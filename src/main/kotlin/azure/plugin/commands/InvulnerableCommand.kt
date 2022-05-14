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

// TO-DO: Move it somewhere... it's going to be used by more than one command.
enum class Toggle(val boolean: Boolean) {
    ON(true),
    OFF(false)
}

// TO-DO: Proper permission handling
@Command("invulnerable", "in")
@CommandPermission("azure.command.invulnerable")
class InvulnerableCommand {

    @Default
    @Usage("<target> [on/off] [-s]")
    fun onDefault(sender: CommandSender, selectorOne: SingleEntitySelector<Player>, @Optional toggle: Toggle?, @Optional @Switch("s") silent: Boolean?) {
        // Getting player from selector
        val target = selectorOne.entity
        // Throwing an exception if target != sender AND sender cannot use that command on players other than himself
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
        if (bool == true) return Locale.INVULNERABLE_SELF_ON
        return Locale.INVULNERABLE_SELF_OFF
    }

    private fun messageSender(bool: Boolean): String? {
        if (bool == true) return Locale.INVULNERABLE_OTHERS_ON
        return Locale.INVULNERABLE_OTHERS_OFF
    }
}