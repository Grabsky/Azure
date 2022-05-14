package azure.plugin.commands

import azure.plugin.configuration.Locale
import indigo.framework.commands.SingleEntitySelector
import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.*
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.plugin.extensions.sendMessageOrIgnore
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// TO-DO: Proper permission handling.
@Command("feed")
@CommandPermission("azure.command.feed")
class FeedCommand {

    @Default
    @Usage("[target]")
    fun onDefault(sender: CommandSender, selectorOne: SingleEntitySelector<Player>, @Optional @Switch("s") silent: Boolean?) {
        // Getting player from selector
        val target = selectorOne.entity
        // Throwing an exception if target != sender AND sender cannot use that command on players other than himself
        if (sender != target && sender.hasPermission("azure.command.feed.others") == false) return // We should throw NoPermissionException here if possible
        // Feeding the player
        target.foodLevel = 20
        // Making sure not to send two similar messages to player
        if (sender != target) {
            sender.sendMessageOrIgnore(Locale.FEED_OTHERS, Placeholders.BADGES, Placeholder.unparsed("target", target.name))
        }
        // Sending information messages if -s (silent) flag is not true
        if (silent != true) {
            target.sendMessageOrIgnore(Locale.FEED_SELF)
        }
    }
}