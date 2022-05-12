package azure.plugin

import azure.plugin.commands.AzureCommand
import azure.plugin.commands.EditorCommand
import azure.plugin.commands.TeleportCommands
import azure.plugin.commands.WorldsCommand
import azure.plugin.configuration.Locale
import azure.plugin.configuration.Settings
import azure.plugin.listeners.ChatListener
import azure.plugin.manager.WorldManager
import indigo.framework.ServerPlugin
import indigo.framework.commands.CommandManager
import indigo.framework.config.ConfigManager
import indigo.framework.config.NotAnObjectException
import indigo.libraries.configurate.serialize.SerializationException
import java.io.File

object AzureProvider {
    internal lateinit var INS: Azure
}

class Azure : ServerPlugin() {
    private lateinit var configManager: ConfigManager
    private lateinit var commandManager: CommandManager

    lateinit var worldManager: WorldManager

    override fun onEnable() {
        super.onEnable()
        // Creating an instance of main class
        AzureProvider.INS = this
        // Setting up configuration files
        this.configManager = ConfigManager(this)
        this.reloadPluginConfiguration()
        // Setting up WorldManager
        this.worldManager = WorldManager(this)
        // Setting up CommandManager
        this.commandManager = CommandManager(this)
        // Registering commands
        this.commandManager.registerCommands(
            AzureCommand(this),
            WorldsCommand(this),
            EditorCommand(),
            TeleportCommands(),
        )
        // Registering events
        this.server.pluginManager.registerEvents(ChatListener(), this)
    }

    override fun reloadPlugin(): Boolean {
        // Reloading plugin configuration
        return this.reloadPluginConfiguration()
    }

    // Reloads configuration files
    internal fun reloadPluginConfiguration(): Boolean {
        var errorOccurred = false
        // Creating a list of configurations available for this plugin
        val configurations = arrayOf(
            Settings::class to File(dataFolder, "settings.conf"),
            Locale::class to File(dataFolder, "locale.conf"),
        )
        // Iterating over configurations and trying to load them
        configurations.forEach {
            try {
                configManager.reload(it.first, it.second)
            } catch (e: NotAnObjectException) {
                errorOccurred = true
                consoleLogger.error("An error occured while trying to load '${it.second.name}' configuration file:")
                consoleLogger.error("  ${e.message}")
            } catch (e: SerializationException) {
                errorOccurred = true
                consoleLogger.error("An error occured while trying to load '${it.second.name}' configuration file:")
                consoleLogger.error("  ${e.message}")
            }
        }
        return !errorOccurred
    }
}