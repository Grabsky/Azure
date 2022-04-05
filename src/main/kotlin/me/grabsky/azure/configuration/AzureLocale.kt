package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.annotations.ConfigPath
import net.kyori.adventure.text.Component

class AzureLocale {
    companion object {
        // Chat
        @ConfigPath("chat.chat-cooldown")
        var CHAT_COOLDOWN: Component? = null

        // Commands => Worlds
        @ConfigPath("commands.worlds.world-teleported")
        var WORLD_TELEPORTED: String? = null
        @ConfigPath("commands.worlds.world-created")
        var WORLD_CREATED: String? = null
        @ConfigPath("commands.worlds.world-deleted")
        var WORLD_DELETED: String? = null
        @ConfigPath("commands.worlds.world-loaded")
        var WORLD_LOADED: String? = null
        @ConfigPath("commands.worlds.world-unloaded")
        var WORLD_UNLOADED: String? = null
        @ConfigPath("commands.worlds.world-saved")
        var WORLD_SAVED: String? = null
        @ConfigPath("commands.worlds.world-backed-up")
        var WORLD_BACKED_UP: String? = null
        @ConfigPath("commands.worlds.world-already-loaded")
        var WORLD_ALREADY_LOADED: String? = null
        @ConfigPath("commands.worlds.world-delete-confirm")
        var WORLD_DELETE_CONFIRM: String? = null
        @ConfigPath("commands.worlds.world-already-exists")
        var WORLD_ALREADY_EXISTS: String? = null
    }
}