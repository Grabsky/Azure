package me.grabsky.azure

import me.grabsky.azure.commands.AzureCommand
import me.grabsky.azure.commands.EditorCommand
import me.grabsky.azure.commands.WorldsCommand
import me.grabsky.azure.configuration.AzureConfig
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.azure.listeners.ChatListener
import me.grabsky.indigo.ServerPlugin
import me.grabsky.indigo.api.commands.CommandManager
import me.grabsky.indigo.api.config.ConfigManager
import me.grabsky.indigo.api.config.NotAnObjectException
import me.grabsky.indigo.api.logger.ConsoleLogger
import me.grabsky.libs.configurate.serialize.SerializationException
import java.io.File
import kotlin.system.measureTimeMillis

object AzureProvider {
    internal lateinit var INS: Azure
}

class Azure : ServerPlugin() {
    override lateinit var consoleLogger: ConsoleLogger

    private lateinit var configManager: ConfigManager
    private lateinit var commandManager: CommandManager

    override fun onEnable() {
        super.onEnable()
        val time = measureTimeMillis {
            // Creating an instance of main class
            AzureProvider.INS = this
            // Setting up loggers
            this.consoleLogger = ConsoleLogger(this)
            // Setting up configuration files
            this.configManager = ConfigManager(this)
            this.reloadPluginConfiguration()
            // Setting up CommandManager
            this.commandManager = CommandManager(this)
            // Registering commands
            this.commandManager.registerCommands(
                AzureCommand(this),
                WorldsCommand(this),
                EditorCommand(),
                useBrigadier = true
            )
            // Registering events
            this.server.pluginManager.registerEvents(ChatListener(), this)
        }
    }

    override fun onDisable() {
        super.onDisable()
        val time = measureTimeMillis {
            // Running disable logic
        }
    }

    override fun onReload() {
        super.onDisable()
        val time = measureTimeMillis {
            // Running reload logic
            this.reloadPluginConfiguration()
        }
    }

    // Reloads configuration files
    internal fun reloadPluginConfiguration(): Boolean {
        var errorOccurred = false
        // Creating a list of configurations available for this plugin
        val configurations = arrayOf(
            AzureConfig::class to File(dataFolder, "config.conf"),
            AzureLocale::class to File(dataFolder, "locale.conf"),
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
        return errorOccurred
    }
}