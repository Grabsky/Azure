package azure.plugin.commands

import azure.plugin.Azure
import azure.plugin.configuration.Locale
import azure.plugin.manager.VanillaEnvironment
import azure.plugin.manager.VanillaWorldType
import indigo.framework.utils.DecimalFormats
import indigo.framework.utils.NamespacedKeys
import indigo.framework.utils.Placeholders
import indigo.libraries.lamp.annotation.*
import indigo.libraries.lamp.bukkit.annotation.CommandPermission
import indigo.libraries.lamp.help.CommandHelp
import indigo.plugin.configuration.ServerLocale
import indigo.plugin.extensions.sendMessageOrIgnore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.World
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
        sender.sendMessageOrIgnore(ServerLocale.COMMAND_USAGE_DIVIDER)
        commandHelp.forEach { sender.sendMessage(it) }
        sender.sendMessageOrIgnore(ServerLocale.COMMAND_USAGE_DIVIDER)
    }

    @Subcommand("spawn")
    @CommandPermission("azure.command.worlds.spawn")
    @Usage("[world]")
    fun onWorldSpawn(sender: Player, @Default("self") world: World) {
        sender.teleport(world.spawnLocation)
        sender.sendMessageOrIgnore(Locale.WORLD_TELEPORTED, Placeholders.BADGES, Placeholder.unparsed("world", world.name))
    }

    @Subcommand("setspawn")
    @CommandPermission("azure.command.worlds.setspawn")
    fun onWorldSetSpawn(sender: Player) {
        sender.world.spawnLocation = sender.location
        sender.sendMessageOrIgnore(Locale.WORLD_SPAWN_SET)
    }

    @Subcommand("create")
    @CommandPermission("azure.command.worlds.create")
    @Usage("<name> <type> <env> [seed] [-autoload]")
    fun onWorldCreate(sender: CommandSender, worldName: String, environment: VanillaEnvironment, type: VanillaWorldType, @Optional seed: Long?) {
        if (File(azure.server.worldContainer, worldName).isDirectory == false) { // This checks if file exists as well
            // Creating a world with specified parameters
            val world = azure.worldManager.create(worldName, environment, type, seed)
            // Sending a message
            sender.sendMessageOrIgnore(text = Locale.WORLD_CREATED, Placeholders.BADGES, Placeholder.unparsed("world", worldName), Placeholder.unparsed("seed", world?.seed.toString()))
            return
        }
        sender.sendMessageOrIgnore(text = Locale.WORLD_ALREADY_EXISTS, Placeholders.BADGES, Placeholder.unparsed("world", worldName))
    }

    @Subcommand("load")
    @CommandPermission("azure.command.worlds.create")
    @Usage("<world>")
    fun onWorldLoad(sender: CommandSender, worldName: String) {
        azure.worldManager.load(worldName)
//        if (azure.server.worldContainer.list()?.contains(worldName) == true && File(azure.server.worldContainer, worldName).isDirectory) {
//            // Loading the world
//            WorldCreator(worldName)
//                .createWorld()
//            sender.sendMessageOrIgnore(Locale.WORLD_LOADED, Placeholders.BADGES, Placeholder.unparsed("world", worldName))
//            return
//        }
//        sender.sendMessageOrIgnore(text = ServerLocale.COMMAND_INVALID_WORLD, Placeholders.BADGES, Placeholder.unparsed("world", worldName))
    }

    @Subcommand("save")
    @CommandPermission("azure.command.worlds.save")
    @Usage("<world>")
    fun onWorldSave(sender: CommandSender, world: World) {
        val saveOperationTime = measureTimeMillis { world.save() }
        sender.sendMessageOrIgnore(text = Locale.WORLD_SAVED, Placeholders.BADGES, Placeholder.unparsed("world", world.name), Placeholder.unparsed("time", DecimalFormats.TWO_DIGIT.format(saveOperationTime)))
    }

    @Subcommand("delete")
    @CommandPermission("azure.command.worlds.delete")
    @Usage("<world> [-confirm]")
    fun onWorldDelete(sender: CommandSender, world: World, @Optional @Switch("confirm") confirm: Boolean?) {
        if (confirm == true) {
            azure.server.unloadWorld(world, false)
            val worldDir = File(azure.server.worldContainer, world.name)
            worldDir.walk().forEach(::println)
            return
        }
        sender.sendMessageOrIgnore(text = Locale.WORLD_DELETE_CONFIRM, Placeholders.BADGES, Placeholder.unparsed("world", world.name))
    }
}