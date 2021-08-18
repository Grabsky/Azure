package net.skydistrict.azure.config;

import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skydistrict.azure.Azure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Lang {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File file;

    private FileConfiguration fileConfiguration;

    // General
    public static Component CORRECT_USAGE;
    public static Component MISSING_PERMISSIONS;
    public static Component PLAYER_NOT_ONLINE;
    public static Component PLAYER_NEVER_PLAYED;
    public static Component CANT_USE_ON_YOURSELF;
    public static Component TARGETS_ARE_THE_SAME;
    public static Component NO_ITEM_IN_HAND;
    public static String TELEPORTING;
    public static Component INVALID_COORDS;
    // Azure
    public static Component PLUGIN_HELP;
    public static Component RELOAD_SUCCEED;
    public static Component RELOAD_FAILED;
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
    public static Component LORE_UPDATED;
    public static Component LORE_CLEARED;
    public static String LORE_INDEX_OUT_OF_BOUNDS;
    public static Component ITEM_HAS_NO_LORE;
    // Enchant
    public static Component ENCHANTMENTS_UPDATED;
    public static Component ITEM_HAS_NO_SUCH_ENCHANTMENT;

    public Lang(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    // Reloads translations
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
        MISSING_PERMISSIONS = component("general.missing-permissions");
        PLAYER_NOT_ONLINE = component("general.player-not-online");
        PLAYER_NEVER_PLAYED = component("general.player-never-played");
        CANT_USE_ON_YOURSELF = component("general.cant-use-on-yourself");
        TARGETS_ARE_THE_SAME = component("general.targets-are-the-same");
        NO_ITEM_IN_HAND = component("general.no-item-in-hand");
        TELEPORTING = string("general.teleporting");
        TELEPORT_CANCELLED = component("general.teleport-cancelled");
        TELEPORT_FAILED = component("general.teleport-failed");
        INVALID_COORDS = component("general.invalid-coords");
        // Azure
        PLUGIN_HELP = component("commands.azure.help");
        RELOAD_SUCCEED = component("commands.azure.reload.reload-succeed");
        RELOAD_FAILED = component("commands.azure.reload.reload-failed");
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
        LORE_UPDATED = component("commands.lore.lore-updated");
        LORE_CLEARED = component("commands.lore.lore-cleared");
        LORE_INDEX_OUT_OF_BOUNDS = string("commands.lore.lore-index-out-of-bounds");
        ITEM_HAS_NO_LORE = component("commands.lore.item-has-no-lore");
        // Enchant
        ENCHANTMENTS_UPDATED = component("commands.enchant.enchantments-updated");
        ITEM_HAS_NO_SUCH_ENCHANTMENT = component("commands.enchant.item-has-no-such-enchantment");
    }

    private String string(String path) {
        final StringBuilder sb = new StringBuilder();
        if (fileConfiguration.isList(path)) {
            final List<String> list = fileConfiguration.getStringList(path);
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i + 1 != list.size()) {
                    sb.append("\n");
                }
            }
        } else {
            sb.append(fileConfiguration.getString(path));
        }
        return sb.toString();
    }

    private Component component(String path) {
        return LegacyComponentSerializer.legacySection().deserialize(this.string(path));
    }

    /** Sends parsed component */
    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }

    /** Parses and sends component */
    public static void send(@NotNull CommandSender sender, @NotNull String text) {
        final Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }

    /** Sends parsed component (with specified identity) */
    public static void send(@NotNull CommandSender sender, @NotNull Component component, @NotNull Identity identity) {
        if (component != Component.empty()) {
            sender.sendMessage(identity, component);
        }
    }

    /** Parses and sends component (with specified identity) */
    public static void send(@NotNull CommandSender sender, @NotNull String text, @NotNull Identity identity) {
        final Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        if (component != Component.empty()) {
            sender.sendMessage(identity, component);
        }
    }

}
