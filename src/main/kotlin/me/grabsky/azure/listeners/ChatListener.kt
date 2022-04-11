package me.grabsky.azure.listeners

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.grabsky.azure.configuration.AzureConfig
import me.grabsky.azure.configuration.AzureLocale
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.utils.parseComponent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.luckperms.api.LuckPermsProvider
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

val luckPerms = LuckPermsProvider.get()

// TO-DO: Generate LP group resolvers dynamically
class GameChatRenderer : ChatRenderer {
    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        if (viewer !is ConsoleCommandSender) {
            return parseComponent(text = AzureConfig.CHAT_FORMAT_DEFAULT,
                Placeholder.unparsed("playername", source.name),
                // Placeholder.unparsed("group", luckPerms)
            ).append(message)
        }
        return Component.text("//")
    }
}

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
        // Setting the renderer
        // event.renderer(GameChatRenderer)
    }
}