package me.grabsky.azure.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class ChatListener : Listener {
    val cooldowns: MutableMap<UUID, Long> = mutableMapOf()

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        // Checking for cooldown
        if (cooldowns[event.player.uniqueId] != null && System.currentTimeMillis() - cooldowns[event.player.uniqueId]!! < 1000) {
            event.isCancelled = true
            event.player.sendMessageOrIgnore(AzureLocale.CHAT_COOLDOWN)
            return
        }
        cooldowns[event.player.uniqueId] = System.currentTimeMillis()
        // TO-DO: Chat format logic
    }
}