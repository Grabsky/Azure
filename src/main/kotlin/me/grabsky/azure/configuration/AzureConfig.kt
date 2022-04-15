package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.annotations.ConfigPath

object AzureConfig {
    @ConfigPath("settings.chat.console-format")
    var CHAT_FORMAT_CONSOLE: String? = null
    @ConfigPath("settings.chat.fallback-format")
    var CHAT_FORMAT_FALLBACK: String? = null
    @ConfigPath("settings.chat.per-group-format")
    var CHAT_FORMATS: Map<String, String>? = null
}