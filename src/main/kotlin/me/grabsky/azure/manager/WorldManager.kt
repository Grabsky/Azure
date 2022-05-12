package me.grabsky.azure.manager

import indigo.framework.ServerPlugin
import indigo.framework.storage.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File
import java.util.*
import java.util.regex.Pattern

@Serializable
data class WorldProperties(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val key: String,
    val seed: Long,
    val vanillaEnvironment: VanillaEnvironment = VanillaEnvironment.NORMAL,
    val vanillaWorldType: VanillaWorldType = VanillaWorldType.NORMAL,
    val autoload: Boolean = false
)

enum class VanillaEnvironment(val bukkitEnvironment: World.Environment) {
    NORMAL(World.Environment.NORMAL),
    NETHER(World.Environment.NETHER),
    THE_END(World.Environment.THE_END)
}

enum class VanillaWorldType(val bukkitWorldType: WorldType) {
    AMPLIFIED(WorldType.AMPLIFIED),
    FLAT(WorldType.FLAT),
    LARGE_BIOMES(WorldType.LARGE_BIOMES),
    NORMAL(WorldType.NORMAL)
}

class InvalidWorldNameException(worldName: String) : Throwable("Cannot create world named '$worldName'. Name contains invalid characters - please use [A-Z, a-z, 0-9, -, _].")

class WorldManager(private val serverPlugin: ServerPlugin) {
    private val propertiesDirectory = File(serverPlugin.dataFolder,"worlds")
    private val propertiesMap: HashMap<String, WorldProperties> = HashMap()
    private val pattern = Pattern.compile("A-Za-z0-9_-")
    private val json = Json { prettyPrint = true }

    fun loadProperties(worldName: String): WorldProperties? {
        val propertiesFile = File(propertiesDirectory, "${worldName}.json")
        // Reading file contents
        val jsonString = propertiesFile.readText()
        // Parsing json string to WorldProperties object
        return json.decodeFromString<WorldProperties>(jsonString)
    }

    fun create(worldName: String, environment: VanillaEnvironment = VanillaEnvironment.NORMAL, type: VanillaWorldType = VanillaWorldType.NORMAL, seed: Long? = null): World? {
        val worldFile = File(serverPlugin.server.worldContainer, worldName)
        // IF WORLD ALREADY EXISTS - LOAD
        if (worldFile.isDirectory == true) {
            this.load(worldFile)
        }
        // ELSE - CREATE
        val creator = WorldCreator(worldName, NamespacedKey(serverPlugin.name, worldName))
            .environment(environment.bukkitEnvironment)
            .type(type.bukkitWorldType)
        if (seed != null) {
            creator.seed(seed)
        }
        // Creating the world
        val world = serverPlugin.server.createWorld(creator)
//        val properties = WorldProperties(
//            world.uid,
//
//        )
        return world
    }

    fun load(worldName: String) = this.load(File(serverPlugin.server.worldContainer, worldName))

    private fun load(worldFile: File): World? {
        if (worldFile.isDirectory == true) {
            val properties = this.loadProperties(worldFile.name)
            if (properties != null) {
                val creator = WorldCreator(worldFile.name)
                    .environment(properties.vanillaEnvironment.bukkitEnvironment)
                    .type(properties.vanillaWorldType.bukkitWorldType)
                    .seed(properties.seed)
                return creator.createWorld()
            }
        }
        return null
    }

    fun import(worldName: String) = this.import(File(serverPlugin.server.worldContainer, worldName))

    private fun import(worldFile: File) {

    }
}