package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.framework.lang.AbstractLang;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AzureLang extends AbstractLang {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File file;

    // General
    public static Component INVALID_COORDS;
    // Azure
    public static Component PLUGIN_HELP;
    // Teleport
    public static Component TELEPORT_CANCELLED;
    public static Component TELEPORT_FAILED;
    public static String TELEPORTED_TO_PLAYER;
    public static String TELEPORTED_TO_LOCATION;
    public static String TELEPORTED_PLAYER_TO_PLAYER;
    public static String TELEPORTED_PLAYER_TO_LOCATION;
    public static String PLAYER_TELEPORTED_TO_YOU;
    public static Component OUTSIDE_WORLD_BORDER;
    // Teleport Request
    public static String TELEPORT_REQUEST_SENT;
    public static String TELEPORT_REQUEST_RECEIVED;
    public static Component TELEPORT_REQUEST_ACCEPTED;
    public static String TELEPORT_REQUEST_ACCEPTED_TARGET;
    public static Component TELEPORT_REQUEST_DENIED;
    public static String TELEPORT_REQUEST_DENIED_TARGET;
    public static Component TELEPORT_REQUEST_ALREADY_SENT;
    public static Component TELEPORT_REQUEST_NOT_FOUND;
    public static Component TELEPORT_REQUEST_CANCELLED;
    // Private Message
    public static String PRIVATE_MESSAGE_FORMAT;
    public static Component NO_PLAYER_TO_REPLY;
    // Skull
    public static String SKULL_RECEIVED;
    // Name
    public static Component NAME_UPDATED;
    public static Component NAME_CLEARED;
    // Lore
    public static Component LORE_USAGE;
    public static Component LORE_UPDATED;
    public static Component LORE_CLEARED;
    public static Component LORE_INDEX_TOO_LOW;
    public static String LORE_INDEX_TOO_HIGH;
    public static Component ITEM_HAS_NO_LORE;
    // Enchant
    public static Component ENCHANT_USAGE;
    public static Component ENCHANTMENTS_UPDATED;
    public static Component ITEM_HAS_NO_SUCH_ENCHANTMENT;
    public static Component ENCHANTMENT_NOT_FOUND;
    // Point
    public static Component POINT_USAGE;
    public static String POINT_ADDED;
    public static String POINT_OVERWRITTEN;
    public static Component INVALID_CHARACTERS;
    public static String POINT_DELETED;
    public static String TELEPORTED_TO_POINT;
    public static Component POINT_NOT_FOUND;
    // PlayerInfo
    public static String PLAYERINFO_ONLINE;
    public static String PLAYERINFO_OFFLINE;

    public AzureLang(Azure instance) {
        super(instance);
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    @Override
    public void reload() {
        // Saving default plugin translation file
        if(!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (fileConfiguration.getInt("version") != 1) {
            consoleLogger.error("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        // General
        INVALID_COORDS = component("general.invalid-coords");
        // Azure
        PLUGIN_HELP = component("commands.azure.help");
        // Teleport
        TELEPORTED_TO_PLAYER = string("commands.teleport.teleported-to-player");
        TELEPORTED_TO_LOCATION = string("commands.teleport.teleported-to-location");
        TELEPORTED_PLAYER_TO_PLAYER = string("commands.teleport.teleported-player-to-player");
        TELEPORTED_PLAYER_TO_LOCATION = string("commands.teleport.teleported-player-to-location");
        PLAYER_TELEPORTED_TO_YOU = string("commands.teleport.player-teleported-to-you");
        OUTSIDE_WORLD_BORDER = component("commands.teleport.outside-world-border");
        // Teleport Request
        TELEPORT_REQUEST_SENT = string("commands.tprequest.request-sent");
        TELEPORT_REQUEST_RECEIVED = string("commands.tprequest.request-received");
        TELEPORT_REQUEST_ACCEPTED = component("commands.tprequest.request-accepted");
        TELEPORT_REQUEST_DENIED = component("commands.tprequest.request-denied");
        TELEPORT_REQUEST_DENIED_TARGET = string("commands.tprequest.request-denied-target");
        TELEPORT_REQUEST_ALREADY_SENT = component("commands.tprequest.request-already-sent");
        TELEPORT_REQUEST_NOT_FOUND = component("commands.tprequest.request-not-found");
        TELEPORT_REQUEST_CANCELLED = component("commands.tprequest.request-cancelled");
        // Private Message
        PRIVATE_MESSAGE_FORMAT = string("commands.message.format");
        NO_PLAYER_TO_REPLY = component("commands.message.no-player-to-reply");
        // Skull
        SKULL_RECEIVED = string("commands.skull.skull-received");
        // Name
        NAME_UPDATED = component("commands.name.name-updated");
        NAME_CLEARED = component("commands.name.name-cleared");
        // Lore
        LORE_USAGE = component("commands.lore.usage");
        LORE_UPDATED = component("commands.lore.lore-updated");
        LORE_CLEARED = component("commands.lore.lore-cleared");
        LORE_INDEX_TOO_LOW = component("commands.lore.lore-index-too-low");
        LORE_INDEX_TOO_HIGH = string("commands.lore.lore-index-too-high");
        ITEM_HAS_NO_LORE = component("commands.lore.item-has-no-lore");
        // Enchant
        ENCHANT_USAGE = component("commands.enchant.usage");
        ENCHANTMENTS_UPDATED = component("commands.enchant.enchantments-updated");
        ITEM_HAS_NO_SUCH_ENCHANTMENT = component("commands.enchant.item-has-no-such-enchantment");
        ENCHANTMENT_NOT_FOUND = component("commands.enchant.enchantment-not-found");
        // Point
        POINT_USAGE = component("commands.point.usage");
        POINT_ADDED = string("commands.point.point-added");
        POINT_OVERWRITTEN = string("commands.point.point-overwritten");
        INVALID_CHARACTERS = component("commands.point.invalid-characters");
        POINT_DELETED = string("commands.point.point-deleted");
        TELEPORTED_TO_POINT = string("commands.point.teleported-to-point");
        POINT_NOT_FOUND = component("commands.point.point-not-found");
        // PlayerInfo
        PLAYERINFO_ONLINE = string("commands.playerinfo.online");
        PLAYERINFO_OFFLINE = string("commands.playerinfo.offline");
    }
}
