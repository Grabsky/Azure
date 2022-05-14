package azure.plugin.commands

import azure.plugin.configuration.Locale
import indigo.framework.commands.SingleEntitySelector
import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.*
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.libraries.paperlib.PaperLib
import indigo.plugin.configuration.ServerLocale
import indigo.plugin.extensions.sendMessageOrIgnore
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class TeleportCommands {

    @Command("teleport", "tp")
    @CommandPermission("azure.command.teleport")
    @Usage("<target> <destination> [-s]")
    fun onTeleport(sender: CommandSender, selectorOne: SingleEntitySelector<Player>, selectorTwo: SingleEntitySelector<Player>, @Switch("s") silent: Boolean?) {
        // Getting players from selectors
        val target = selectorOne.entity
        val destination = selectorTwo.entity
        // Checking if target is not the same as destination target (eg. /teleport player_one player_one, /teleport @s sender_name)
        if (target != destination) {
            // Teleporting
            target.teleport(destination, TeleportCause.COMMAND)
            // Making sure not to send two similar messages to player
            if (sender != target && sender != destination) {
                sender.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_PLAYER_TO_PLAYER, Placeholders.BADGES, Placeholder.unparsed("target", target.name), Placeholder.unparsed("destination", destination.name))
            }
            // Sending information messages if -s (silent) flag is not enabled
            if (silent != true) {
                target.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_PLAYER, Placeholders.BADGES, Placeholder.unparsed("target", destination.name))
                destination.sendMessageOrIgnore(Locale.TELEPORT_PLAYER_TELEPORTED_TO_YOU, Placeholders.BADGES, Placeholder.unparsed("target", target.name))
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_TARGETS_ARE_THE_SAME)
        return
    }

    @Command("teleportlocation", "tploc")
    @CommandPermission("azure.command.teleportloc")
    @Usage("<target> <x> <y> <z> [world] [-s]")
    @AutoComplete("* @pos:x @pos:y @pos:z * *")
    fun onTeleportLocation(sender: CommandSender, targetSelector: SingleEntitySelector<Player>, x: Double, y: Double, z: Double, @Default("self") world: World, @Switch("s") silent: Boolean?) {
        // Getting player from selector
        val target = targetSelector.entity
        // Creating a destination location from coordinates
        val destination = Location(world, x, y, z)
        // Checking if destination location is not outside the world border
        if (destination.world.worldBorder.isInside(destination)) {
            // Teleporting asynchronously to prevent sync chunk loads
            PaperLib.teleportAsync(target, destination, TeleportCause.COMMAND).thenAccept {
                // Checking if player was teleported successfully
                if (it == true) {
                    // Making sure not to send two similar messages to player
                    if (sender != target) {
                        sender.sendMessageOrIgnore(
                            Locale.TELEPORT_TELEPORTED_PLAYER_TO_LOCATION,
                            Placeholders.BADGES,
                            Placeholder.unparsed("target", target.name),
                            Placeholder.unparsed("x", x.toString()),
                            Placeholder.unparsed("y", y.toString()),
                            Placeholder.unparsed("z", z.toString()),
                            Placeholder.unparsed("world", world.name)
                        )
                    }
                    // Sending information messages if -s (silent) flag is not enabled
                    if (silent != true) {
                        target.sendMessageOrIgnore(
                            Locale.TELEPORT_TELEPORTED_TO_LOCATION,
                            Placeholders.BADGES,
                            Placeholder.unparsed("x", x.toString()),
                            Placeholder.unparsed("y", y.toString()),
                            Placeholder.unparsed("z", z.toString()),
                            Placeholder.unparsed("world", world.name)
                        )
                    }
                    return@thenAccept
                }
                // Sending an error message because teleportation failed (could happen for various reasons)
                sender.sendMessageOrIgnore(ServerLocale.TELEPORT_ERROR)
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_OUTSIDE_WORLD_BORDER)
    }
}