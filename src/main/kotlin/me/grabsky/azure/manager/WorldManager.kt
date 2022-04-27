package me.grabsky.azure.manager

import me.grabsky.indigo.ServerPlugin
import me.grabsky.libs.lamp.annotation.Optional
import org.bukkit.World
import org.bukkit.WorldType

enum class VanillaEnvironment(environment: World.Environment) {
    NORMAL(World.Environment.NORMAL),
    NETHER(World.Environment.NETHER),
    THE_END(World.Environment.THE_END)
}



class WorldManager(val serverPlugin: ServerPlugin) {
    fun create(name: String, env: VanillaEnvironment, type: WorldType, @Optional seed: String?) {

    }
}