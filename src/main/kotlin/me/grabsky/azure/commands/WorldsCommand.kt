package me.grabsky.azure.commands

import me.grabsky.azure.Azure
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.indigo.configuration.GlobalLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.utils.Placeholders
import me.grabsky.indigo.utils.TWO_DECIMAL_PLACE
import me.grabsky.indigo.utils.operationTime
import me.grabsky.libs.lamp.annotation.*
import me.grabsky.libs.lamp.bukkit.annotation.CommandPermission
import me.grabsky.libs.lamp.help.CommandHelp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

@Command("worlds")
@CommandPermission("azure.command.worlds")
class WorldsCommand(private val azure: Azure) {

    @Default
    fun onDefault(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    @Subcommand("teleport")
    @CommandPermission("azure.command.worlds.teleport")
    @Usage("<world>")
    fun onWorldTeleport(sender: Player, world: World) {
        sender.teleport(world.spawnLocation)
        sender.sendMessageOrIgnore(AzureLocale.WORLD_TELEPORTED, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", world.name))
    }

    @Subcommand("create")
    @CommandPermission("azure.command.worlds.create")
    @Usage("<name> <type> <env> [seed]")
    fun onWorldCreate(sender: CommandSender, name: String, worldType: WorldType?, environment: World.Environment?, @Optional seed: Long?) {
        if (azure.server.worlds.firstOrNull { it.name == name } == null) {
            // Configuring WorldCreated
            val worldCreator = WorldCreator(name)
                .type(worldType ?: WorldType.NORMAL)
                .environment(environment ?: World.Environment.NORMAL)
            // Applying seed if specified
            if (seed != null) {
                worldCreator.seed(seed)
            }
            // Creating a world
            val world = worldCreator.createWorld()
            // Sending a message
            sender.sendMessageOrIgnore(text = AzureLocale.WORLD_CREATED, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", name), Placeholder.unparsed("seed", world?.seed.toString()))
            return
        }
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_ALREADY_EXISTS, Placeholders.BADGE_ERROR, Placeholder.unparsed("world", name))
    }

    @Subcommand("backup")
    @CommandPermission("azure.command.worlds.backup")
    @Usage("<world> [-save]")
    fun onWorldBackup(sender: CommandSender, world: World, @Switch("save", defaultValue = false) save: Boolean) {
        // Saving world if applicable
        if (save) world.save()
        // TO-DO: Archive the world folder and then back it up
        // TO-DO: Add missing locale nodes
        world.worldFolder.copyTo(File("/backups/${world.name}_${System.currentTimeMillis()}"))
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_BACKED_UP, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", world.name))
    }

    @Subcommand("save")
    @CommandPermission("azure.command.worlds.save")
    @Usage("<world>")
    fun onWorldSave(sender: CommandSender, world: World) {
        val time = operationTime({
            world.save()
        })
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_SAVED, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", world.name), Placeholder.unparsed("time", TWO_DECIMAL_PLACE.format(time)))
    }

    @Subcommand("delete")
    @CommandPermission("azure.command.worlds.delete")
    @Usage("<world> [-confirm]")
    fun onWorldDelete(sender: CommandSender, world: World, @Optional @Switch("confirm") confirm: Boolean?) {
        if (confirm != null && confirm) {
            // TO-DO: Logic
            return
        }
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_DELETE_CONFIRM, Placeholders.BADGE_ERROR, Placeholder.unparsed("world", world.name))
    }
}