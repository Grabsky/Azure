package me.grabsky.azure.commands

import me.grabsky.azure.Azure
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.indigo.api.utils.NamespacedKeys
import me.grabsky.indigo.configuration.GlobalLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.utils.Placeholders
import me.grabsky.indigo.utils.TWO_DECIMAL_PLACE
import me.grabsky.libs.lamp.annotation.*
import me.grabsky.libs.lamp.bukkit.annotation.CommandPermission
import me.grabsky.libs.lamp.help.CommandHelp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import kotlin.system.measureTimeMillis

val WORLD_AUTOLOAD = NamespacedKeys.of("azure:autoload")

@Command("worlds")
@CommandPermission("azure.command.worlds")
class WorldsCommand(private val azure: Azure) {

    @Default
    fun onDefault(sender: CommandSender, commandHelp: CommandHelp<Component>) {
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(GlobalLocale.COMMAND_USAGE_DIVIDER)
    }

    @Subcommand("spawn")
    @CommandPermission("azure.command.worlds.spawn")
    @Usage("[world]")
    fun onWorldSpawn(sender: Player, @Default("self") world: World) {
        Bukkit.unloadWorld(world, false)
        sender.teleport(world.spawnLocation)
        sender.sendMessageOrIgnore(AzureLocale.WORLD_TELEPORTED, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", world.name))
    }

    @Subcommand("setspawn")
    @CommandPermission("azure.command.worlds.setspawn")
    fun onWorldSetSpawn(sender: Player) {
        sender.world.spawnLocation = sender.location
        sender.sendMessageOrIgnore(AzureLocale.WORLD_SPAWN_SET)
    }

    @Subcommand("create")
    @CommandPermission("azure.command.worlds.create")
    @Usage("<name> <type> <env> [seed] [-autoload]")
    fun onWorldCreate(sender: CommandSender, name: String, worldType: WorldType?, environment: World.Environment?, @Optional seed: Long?, @Flag autoload: Boolean?) {
        if (azure.server.worldContainer.list()?.contains(name) == false) {
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

    @Subcommand("save")
    @CommandPermission("azure.command.worlds.save")
    @Usage("<world>")
    fun onWorldSave(sender: CommandSender, world: World) {
        val saveOperationTime = measureTimeMillis { world.save() }
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_SAVED, Placeholders.BADGE_SUCCESS, Placeholder.unparsed("world", world.name), Placeholder.unparsed("time", TWO_DECIMAL_PLACE.format(saveOperationTime)))
    }

    @Subcommand("delete")
    @CommandPermission("azure.command.worlds.delete")
    @Usage("<world> [-confirm]")
    fun onWorldDelete(sender: CommandSender, world: World, @Optional @Switch("confirm") confirm: Boolean?) {
        if (confirm != null && confirm) {
            azure.server.unloadWorld(world, false)
            val worldDir = File(azure.server.worldContainer, world.name)
            worldDir.walk().forEach(::println)
            return
        }
        sender.sendMessageOrIgnore(text = AzureLocale.WORLD_DELETE_CONFIRM, Placeholders.BADGE_ERROR, Placeholder.unparsed("world", world.name))
    }
}