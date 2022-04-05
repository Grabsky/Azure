package me.grabsky.azure

import me.grabsky.azure.commands.AzureCommand
import me.grabsky.azure.commands.WorldsCommand
import me.grabsky.azure.configuration.AzureConfig
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.azure.listeners.ChatListener
import me.grabsky.indigo.KotlinPlugin
import me.grabsky.indigo.api.commands.CommandManager
import me.grabsky.indigo.api.config.ConfigManager
import me.grabsky.indigo.api.logger.ConsoleLogger
import me.grabsky.indigo.utils.operationTime
import java.io.File

class Azure : KotlinPlugin() {
    override lateinit var consoleLogger: ConsoleLogger

    companion object {
        // This is Azure plugin instance (for internal use)
        internal lateinit var INS: Azure
            private set
    }

    private lateinit var configManager: ConfigManager
    private lateinit var commandManager: CommandManager

    override fun onEnable() {
        super.onEnable()
        val time = operationTime(func = {
            // Creating an instance of main class
            Azure.INS = this
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
                useBrigadier = true
            )
            // Registering events
            this.server.pluginManager.registerEvents(ChatListener(), this)
        })
    }

    override fun onDisable() {
        super.onDisable()
        val time = operationTime(func = {
            // Currently there is no disable logic
        })
    }

    override fun onReload(): Boolean {
        return this.reloadPluginConfiguration()
    }

    // Reloads configuration files
    internal fun reloadPluginConfiguration(): Boolean {
        configManager.reload(AzureConfig::class, File("${dataFolder}${File.separator}config.conf"))
        configManager.reload(AzureLocale::class, File("${dataFolder}${File.separator}locale.conf"))
        return true
    }
}