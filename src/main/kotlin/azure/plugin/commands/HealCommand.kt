package azure.plugin.commands

import azure.plugin.configuration.Locale
import indigo.framework.commands.SingleEntitySelector
import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.*
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.plugin.extensions.sendMessageOrIgnore
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// TO-DO: Proper permission handling.
@Command("heal")
@CommandPermission("azure.command.heal")
class HealCommand {

    @Default
    @Usage("[target]")
    fun onDefault(sender: CommandSender, selectorOne: SingleEntitySelector<Player>, @Optional @Switch("s") silent: Boolean?) {
        // Getting player from selector
        val target = selectorOne.entity
        // Throwing an exception if target != sender AND sender cannot use that command on players other than himself
        if (target != sender && sender.hasPermission("azure.command.heal.others") == false) return // We should throw NoPermissionException here if possible
        // Healing the player
        target.health = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        // Making sure not to send two similar messages to player
        if (sender != target) {
            sender.sendMessageOrIgnore(Locale.HEAL_OTHERS, Placeholders.BADGES, Placeholder.unparsed("target", target.name))
        }
        // Sending information messages if -s (silent) flag is not true
        if (silent != true) {
            target.sendMessageOrIgnore(Locale.HEAL_SELF)
        }
    }
}