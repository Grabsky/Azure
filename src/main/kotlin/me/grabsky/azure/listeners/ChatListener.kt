package me.grabsky.azure.listeners

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.grabsky.azure.configuration.Locale
import me.grabsky.azure.configuration.Settings
import me.grabsky.indigo.extensions.sendMessageOrIgnore
import me.grabsky.indigo.extensions.style
import me.grabsky.indigo.extensions.toPlainString
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.luckperms.api.LuckPermsProvider
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

object GameChatRenderer : ChatRenderer {
    private val luckPerms = LuckPermsProvider.get()
    private val richMessageParser = MiniMessage.builder()
        .editTags {
            it.resolver(StandardTags.color())
            it.resolver(StandardTags.decorations())
            it.resolver(StandardTags.gradient())
            it.resolver(StandardTags.rainbow())
        }
        .build()
    private val consoleFormatParser = MiniMessage.builder()
        .editTags { it.resolver(StandardTags.insertion()) }
        .build()

    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        val group = luckPerms.userManager.getUser(source.uniqueId)?.primaryGroup ?: "default"
        // PLAYERS
        if (viewer !is ConsoleCommandSender) {
            val chatFormat = Settings.CHAT_FORMATS?.get(group) ?: Settings.CHAT_FORMAT_FALLBACK!!
            val finalMessage = if (source.hasPermission("azure.plugin.chat.richformat")) richMessageParser.deserialize(message.toPlainString()) else message.style(null)
            // Parsing & returning the format
            return MiniMessage.miniMessage().deserialize(chatFormat,
                Placeholder.unparsed("player", source.name),
                Placeholder.component("message", finalMessage)
            )
        }
        // CONSOLE
        return consoleFormatParser.deserialize(
            Settings.CHAT_FORMAT_CONSOLE!!,
            Placeholder.unparsed("group", group.replaceFirstChar { it.uppercase() }),
            Placeholder.unparsed("player", source.name),
            Placeholder.unparsed("message", MiniMessage.miniMessage().stripTags(message.toPlainString()))
        )
    }
}

class ChatListener : Listener {
    private val cooldowns: MutableMap<UUID, Long> = mutableMapOf()

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        // Checking for cooldown
        if (cooldowns[event.player.uniqueId] != null && System.currentTimeMillis() - cooldowns[event.player.uniqueId]!! < 1000) {
            event.isCancelled = true
            event.player.sendMessageOrIgnore(Locale.CHAT_COOLDOWN)
            return
        }
        cooldowns[event.player.uniqueId] = System.currentTimeMillis()
        // Setting the renderer
        val time = System.nanoTime()
        val c = GameChatRenderer.render(event.player, event.player.displayName(), event.message(), Audience.empty())
        println("Operation took ${(System.nanoTime() - time) / 1000000f}ms")
    }
}