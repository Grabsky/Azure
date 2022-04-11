package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.annotations.ConfigPath

object AzureConfig {
    @ConfigPath("settings.chat.consoleFormat")
    var CHAT_FORMAT_CONSOLE: String? = null
    @ConfigPath("settings.chat.format.default")
    var CHAT_FORMAT_DEFAULT: String? = null
    @ConfigPath("settings.chat.format")
    var CHAT_FORMATS: Map<String, String>? = null
}