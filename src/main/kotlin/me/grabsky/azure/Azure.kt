package me.grabsky.azure

import me.grabsky.indigo.KotlinPlugin
import me.grabsky.indigo.api.config.ConfigManager
import me.grabsky.indigo.api.logger.ConsoleLogger
import me.grabsky.indigo.utils.operationTime

class Azure : KotlinPlugin() {
    override lateinit var consoleLogger: ConsoleLogger

    companion object {
        // This is Azure plugin instance (for internal use)
        internal lateinit var INS: Azure
            private set
    }

    private lateinit var configManager: ConfigManager

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
        return true
    }
}