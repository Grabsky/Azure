package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.ConfigProperty
import net.kyori.adventure.text.Component

internal object Locale {
    // Chat
    @ConfigProperty(path = ["chat", "chat-cooldown"])
    var CHAT_COOLDOWN: Component? = null

    // Commands => Worlds
    @ConfigProperty(path = ["commands", "worlds", "world-teleported"])
    var WORLD_TELEPORTED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-created"])
    var WORLD_CREATED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-deleted"])
    var WORLD_DELETED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-loaded"])
    var WORLD_LOADED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-unloaded"])
    var WORLD_UNLOADED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-saved"])
    var WORLD_SAVED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-spawn-set"])
    var WORLD_SPAWN_SET: Component? = null
    @ConfigProperty(path = ["commands", "worlds", "world-already-loaded"])
    var WORLD_ALREADY_LOADED: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-delete-confirm"])
    var WORLD_DELETE_CONFIRM: String? = null
    @ConfigProperty(path = ["commands", "worlds", "world-already-exists"])
    var WORLD_ALREADY_EXISTS: String? = null

    // Commands => Editor
    @ConfigProperty(path = ["commands", "editor", "item-name-updated"])
    var EDITOR_ITEM_NAME_UPDATED: Component? = null
    @ConfigProperty(path = ["commands", "editor", "item-name-reset"])
    var EDITOR_ITEM_NAME_RESET: Component? = null
    @ConfigProperty(path = ["commands", "editor", "item-lore-updated"])
    var EDITOR_ITEM_LORE_UPDATED: Component? = null
    @ConfigProperty(path = ["commands", "editor", "item-lore-reset"])
    var EDITOR_ITEM_LORE_RESET: Component? = null
    @ConfigProperty(path = ["commands", "editor", "enchant-added"])
    var EDITOR_ITEM_ENCHANT_ADDED: String? = null
    @ConfigProperty(path = ["commands", "editor", "enchant-removed"])
    var EDITOR_ITEM_ENCHANT_REMOVED: String? = null
    @ConfigProperty(path = ["commands", "editor", "enchant-reset"])
    var EDITOR_ITEM_ENCHANT_RESET: Component? = null

    // Commands => Teleport
    @ConfigProperty(path = ["commands", "teleport", "teleported-to-player"])
    var TELEPORT_TELEPORTED_TO_PLAYER: String? = null
    @ConfigProperty(path = ["commands", "teleport", "teleported-to-location"])
    var TELEPORT_TELEPORTED_TO_LOCATION: String? = null
    @ConfigProperty(path = ["commands", "teleport", "teleported-player-to-player"])
    var TELEPORT_TELEPORTED_PLAYER_TO_PLAYER: String? = null
    @ConfigProperty(path = ["commands", "teleport", "teleported-player-to-location"])
    var TELEPORT_TELEPORTED_PLAYER_TO_LOCATION: String? = null
    @ConfigProperty(path = ["commands", "teleport", "player-teleported-to-you"])
    var TELEPORT_PLAYER_TELEPORTED_TO_YOU: String? = null
    @ConfigProperty(path = ["commands", "teleport", "outside-world-border"])
    var TELEPORT_OUTSIDE_WORLD_BORDER: Component? = null
    @ConfigProperty(path = ["commands", "teleport", "targets-are-the-same"])
    var TELEPORT_TARGETS_ARE_THE_SAME: Component? = null
}