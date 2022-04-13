package me.grabsky.azure.configuration

import me.grabsky.indigo.api.config.annotations.ConfigPath
import net.kyori.adventure.text.Component

object AzureLocale {
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
    @ConfigPath("commands.worlds.world-spawn-set")
    var WORLD_SPAWN_SET: Component? = null
    @ConfigPath("commands.worlds.world-already-loaded")
    var WORLD_ALREADY_LOADED: String? = null
    @ConfigPath("commands.worlds.world-delete-confirm")
    var WORLD_DELETE_CONFIRM: String? = null
    @ConfigPath("commands.worlds.world-already-exists")
    var WORLD_ALREADY_EXISTS: String? = null

    // Commands => Editor
    @ConfigPath("commands.editor.item-name-updated")
    var EDITOR_ITEM_NAME_UPDATED: Component? = null
    @ConfigPath("commands.editor.item-name-reset")
    var EDITOR_ITEM_NAME_RESET: Component? = null
    @ConfigPath("commands.editor.item-lore-updated")
    var EDITOR_ITEM_LORE_UPDATED: Component? = null
    @ConfigPath("commands.editor.item-lore-reset")
    var EDITOR_ITEM_LORE_RESET: Component? = null
    @ConfigPath("commands.editor.enchant-added")
    var EDITOR_ITEM_ENCHANT_ADDED: String? = null
    @ConfigPath("commands.editor.enchant-removed")
    var EDITOR_ITEM_ENCHANT_REMOVED: String? = null
    @ConfigPath("commands.editor.enchant-reset")
    var EDITOR_ITEM_ENCHANT_RESET: Component? = null
}