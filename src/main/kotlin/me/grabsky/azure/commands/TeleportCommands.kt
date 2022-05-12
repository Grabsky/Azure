package me.grabsky.azure.commands

import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.AutoComplete
import indigo.libraries.lamp.annotation.Command
import indigo.libraries.lamp.annotation.Default
import indigo.libraries.lamp.annotation.Switch
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.libraries.paperlib.PaperLib
import indigo.plugin.configuration.ServerLocale
import indigo.plugin.extensions.sendMessageOrIgnore
import me.grabsky.azure.configuration.Locale
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class TeleportCommands {

    @Command("teleport", "tp")
    @CommandPermission("azure.command.teleport")
    fun onTeleport(sender: CommandSender, targetOne: Player, targetTwo: Player, @Switch("s") silent: Boolean?) {
        if (targetOne != targetTwo) {
            targetOne.teleport(targetTwo, TeleportCause.COMMAND)
            sender.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_PLAYER_TO_PLAYER, Placeholders.BADGES, Placeholder.unparsed("target_one", targetOne.name), Placeholder.unparsed("target_two", targetTwo.name))
            if (silent != true) {
                targetOne.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_PLAYER, Placeholders.BADGES, Placeholder.unparsed("target", targetTwo.name))
                targetTwo.sendMessageOrIgnore(Locale.TELEPORT_PLAYER_TELEPORTED_TO_YOU, Placeholders.BADGES, Placeholder.unparsed("target", targetOne.name))
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_TARGETS_ARE_THE_SAME)
        return
    }

    @Command("teleportlocation", "tploc")
    @CommandPermission("azure.command.teleportloc")
    @AutoComplete("* @pos:x @pos:y @pos:z * *")
    fun onTeleportLocation(sender: CommandSender, target: Player, x: Double, y: Double, z: Double, @Default("self") world: World, @Switch("s") silent: Boolean?) {
        val destination = Location(world, x, y, z)
        if (destination.world.worldBorder.isInside(destination)) {
            PaperLib.teleportAsync(target, destination, TeleportCause.COMMAND).thenAccept {
                if (it == true) {
                    sender.sendMessageOrIgnore(
                        Locale.TELEPORT_TELEPORTED_PLAYER_TO_LOCATION,
                        Placeholders.BADGES,
                        Placeholder.unparsed("target", target.name),
                        Placeholder.unparsed("x", x.toString()),
                        Placeholder.unparsed("y", y.toString()),
                        Placeholder.unparsed("z", z.toString()),
                        Placeholder.unparsed("world", world.name)
                    )
                    if (silent != true) {
                        target.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_LOCATION,
                            Placeholders.BADGES,
                            Placeholder.unparsed("x", x.toString()),
                            Placeholder.unparsed("y", y.toString()),
                            Placeholder.unparsed("z", z.toString()),
                            Placeholder.unparsed("world", world.name)
                        )
                    }
                    return@thenAccept
                }
                sender.sendMessageOrIgnore(ServerLocale.TELEPORT_ERROR)
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_OUTSIDE_WORLD_BORDER)
    }
}