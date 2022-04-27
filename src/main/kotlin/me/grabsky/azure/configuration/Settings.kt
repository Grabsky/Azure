package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.ConfigProperty

internal object Settings {
    @ConfigProperty(path = ["settings", "chat", "console-format"])
    var CHAT_FORMAT_CONSOLE: String? = null
    @ConfigProperty(path = ["settings", "chat", "fallback-format"])
    var CHAT_FORMAT_FALLBACK: String? = null
    @ConfigProperty(path = ["settings", "chat", "per-group-format"])
    var CHAT_FORMATS: Map<String, String>? = null
}