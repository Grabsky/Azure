package me.grabsky.azure.commands

import me.grabsky.azure.configuration.Locale
import me.grabsky.indigo.configuration.GlobalLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.utils.Placeholders
import me.grabsky.libs.lamp.annotation.AutoComplete
import me.grabsky.libs.lamp.annotation.Command
import me.grabsky.libs.lamp.annotation.Default
import me.grabsky.libs.lamp.annotation.Switch
import me.grabsky.libs.lamp.bukkit.annotation.CommandPermission
import me.grabsky.libs.paperlib.PaperLib
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// TO-DO: Think about better naming.
// TO-DO: Perhaps there is a better way to structure these commands.
// TO-DO: Clean-up the code and make sure everything is working correctly.
class TeleportCommands {

    @Command("goto")
    @CommandPermission("azure.command.goto")
    fun onGoto(sender: Player, target: Player, @Switch("s") silent: Boolean?) {
        sender.teleport(target)
        sender.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_PLAYER, Placeholders.BADGE_INFO, Placeholder.unparsed("target", target.name))
        if (silent != true) {
            target.sendMessageOrIgnore(Locale.TELEPORT_PLAYER_TELEPORTED_TO_YOU, Placeholders.BADGE_INFO, Placeholder.unparsed("target", sender.name))
        }
    }

    @Command("gotoloc")
    @CommandPermission("azure.command.gotoloc")
    @AutoComplete("@pos:x @pos:y @pos:z *")
    fun onGotoLoc(sender: Player, x: Double, y: Double, z: Double, @Default("self") world: World) {
        val destination = Location(world, x, y, z)
        if (destination.world.worldBorder.isInside(destination)) {
            PaperLib.teleportAsync(sender, destination).thenAccept {
                if (it == true) {
                    sender.sendMessageOrIgnore(
                        Locale.TELEPORT_TELEPORTED_TO_LOCATION,
                        Placeholders.BADGE_INFO,
                        Placeholder.unparsed("x", x.toString()),
                        Placeholder.unparsed("y", y.toString()),
                        Placeholder.unparsed("z", z.toString()),
                        Placeholder.unparsed("world", world.name)
                    )
                    return@thenAccept
                }
                sender.sendMessageOrIgnore(GlobalLocale.TELEPORT_ERROR)
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_OUTSIDE_WORLD_BORDER)
    }

    @Command("teleport", "tp")
    @CommandPermission("azure.command.teleport")
    fun onTeleport(sender: CommandSender, targetOne: Player, targetTwo: Player, @Switch("s") silent: Boolean?) {
        if (targetOne == targetTwo) {
            sender.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_PLAYER_TO_PLAYER, Placeholder.unparsed("target_one", targetOne.name), Placeholder.unparsed("target_two", targetTwo.name))
            if (silent != true) {
                targetOne.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_PLAYER, Placeholders.BADGE_INFO, Placeholder.unparsed("target", targetTwo.name))
                targetTwo.sendMessageOrIgnore(Locale.TELEPORT_PLAYER_TELEPORTED_TO_YOU, Placeholders.BADGE_INFO, Placeholder.unparsed("target", targetOne.name))
            }
            return
        }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_MISSING_PERMISSIONS)
        return
    }

    @Command("teleportloc", "tploc")
    @CommandPermission("azure.command.teleport")
    @AutoComplete("* @pos:x @pos:y @pos:z * *")
    fun onTeleportLocation(sender: CommandSender, target: Player, x: Double, y: Double, z: Double, @Default("self") world: World, @Switch("s") silent: Boolean?) {
        val destination = Location(world, x, y, z)
        if (destination.world.worldBorder.isInside(destination)) {
            PaperLib.teleportAsync(target, destination).thenAccept {
                if (it == true) {
                    sender.sendMessageOrIgnore(
                        Locale.TELEPORT_TELEPORTED_PLAYER_TO_LOCATION,
                        Placeholders.BADGE_INFO,
                        Placeholder.unparsed("target", target.name),
                        Placeholder.unparsed("x", x.toString()),
                        Placeholder.unparsed("y", y.toString()),
                        Placeholder.unparsed("z", z.toString()),
                        Placeholder.unparsed("world", world.name)
                    )
                    if (silent != true) {
                        target.sendMessageOrIgnore(Locale.TELEPORT_TELEPORTED_TO_LOCATION,
                            Placeholders.BADGE_INFO,
                            Placeholder.unparsed("x", x.toString()),
                            Placeholder.unparsed("y", y.toString()),
                            Placeholder.unparsed("z", z.toString()),
                            Placeholder.unparsed("world", world.name)
                        )
                    }
                    return@thenAccept
                }
                sender.sendMessageOrIgnore(GlobalLocale.TELEPORT_ERROR)
            }
            return
        }
        sender.sendMessageOrIgnore(Locale.TELEPORT_OUTSIDE_WORLD_BORDER)
    }
}