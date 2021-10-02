package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.configuration.Global;
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
    // Chat
    public static Component CHAT_COOLDOWN;
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
    // Private Message
    public static String PRIVATE_MESSAGE_FORMAT;
    public static Component NO_PLAYER_TO_REPLY;
    // SocialSpy
    public static String SOCIAL_SPY_FORMAT;
    public static Component SOCIAL_SPY_ON;
    public static Component SOCIAL_SPY_OFF;
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
    // Homes
    public static String TELEPORTED_TO_HOME;
    public static String HOME_SET;
    public static String HOME_NOT_FOUND;
    public static Component HOME_LIMIT_EXCEEDED;
    public static Component INVALID_CHARACTERS;
    public static String HOME_DELETED;
    // Heal
    public static Component YOU_HAVE_BEEN_HEALED;
    public static String PLAYER_HAS_BEEN_HEALED;
    // Feed
    public static Component YOU_HAVE_BEEN_FED;
    public static String PLAYER_HAS_BEEN_FED;
    // Speed
    public static String WALK_SPEED_SET;
    public static String FLY_SPEED_SET;
    // Invulnerable
    public static Component INVULNERABLE_ON;
    public static Component INVULNERABLE_OFF;
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
            consoleLogger.error(Global.OUTDATED_LANG);
        }
        // General
        INVALID_COORDS = component("general.invalid-coords");
        // Chat
        CHAT_COOLDOWN = component("chat.chat-cooldown");
        // Azure
        PLUGIN_HELP = component("commands.azure.help");
        // Teleport
        TELEPORTED_TO_PLAYER = string("commands.teleport.teleported-to-player");
        TELEPORTED_TO_LOCATION = string("commands.teleport.teleported-to-location");
        TELEPORTED_PLAYER_TO_PLAYER = string("commands.teleport.teleported-player-to-player");
        TELEPORTED_PLAYER_TO_LOCATION = string("commands.teleport.teleported-player-to-location");
        PLAYER_TELEPORTED_TO_YOU = string("commands.teleport.player-teleported-to-you");
        OUTSIDE_WORLD_BORDER = component("commands.teleport.outside-world-border");
        // Private Message
        PRIVATE_MESSAGE_FORMAT = string("commands.message.format");
        NO_PLAYER_TO_REPLY = component("commands.message.no-player-to-reply");
        // SocialSpy
        SOCIAL_SPY_FORMAT = string("commands.socialspy.format");
        SOCIAL_SPY_ON = component("commands.socialspy.socialspy-on");
        SOCIAL_SPY_OFF = component("commands.socialspy.socialspy-off");
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
        // Homes
        TELEPORTED_TO_HOME = string("commands.home.teleported-to-home");
        HOME_NOT_FOUND = string("commands.home.home-not-found");
        HOME_SET = string("commands.sethome.home-set");
        INVALID_CHARACTERS = component("commands.sethome.invalid-characters");
        HOME_LIMIT_EXCEEDED = component("commands.sethome.home-limit-exceeded");
        HOME_DELETED = string("commands.delhome.home-deleted");
        // Heal
        YOU_HAVE_BEEN_HEALED = component("commands.heal.you-have-been-healed");
        PLAYER_HAS_BEEN_HEALED = string("commands.heal.player-has-been-healed");
        // Feed
        YOU_HAVE_BEEN_FED = component("commands.feed.you-have-been-fed");
        PLAYER_HAS_BEEN_FED = string("commands.feed.player-has-been-fed");
        // Speed
        WALK_SPEED_SET = string("commands.speed.walk-speed-set");
        FLY_SPEED_SET = string("commands.speed.fly-speed-set");
        // Invulnerable
        INVULNERABLE_ON = component("commands.invulnerable.invulnerable-on");
        INVULNERABLE_OFF = component("commands.invulnerable.invulnerable-off");
        // PlayerInfo
        PLAYERINFO_ONLINE = string("commands.playerinfo.online");
        PLAYERINFO_OFFLINE = string("commands.playerinfo.offline");
    }
}
