package net.skydistrict.azure.config;

import me.grabsky.indigo.adventure.MiniMessage;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.skydistrict.azure.Azure;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

// READ BEFORE RE-WRITING:
// Having per-player locales is nice, but not really worth an effort.
// It works good ONLY if EVERY SINGLE PLUGIN you use, have something similar implemented.
// Don't waste your time and do something cool instead!
public class Lang {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;
    private final File file;
    private final int currentVersion = 1;
    private FileConfiguration fileConfiguration;

    public static Component CORRECT_USAGE;
    public static Component MISSING_PERMISSIONS;
    public static Component PLAYER_NOT_ONLINE;
    public static Component PLAYER_NEVER_PLAYED;
    public static Component CANT_USE_ON_YOURSELF;
    public static Component TARGETS_ARE_THE_SAME;
    public static Component NO_ITEM_IN_HAND;
    public static Component AZURE_HELP;
    public static Component AZURE_RELOAD_SUCCESS;
    public static Component AZURE_RELOAD_FAIL;
    public static String TELEPORTED_TO_PLAYER;
    public static String TELEPORTED_TO_LOCATION;
    public static String TELEPORTED_PLAYER_TO_PLAYER;
    public static String TELEPORTED_PLAYER_TO_LOCATION;
    public static String PLAYER_TELEPORTED_TO_YOU;
    public static String PRIVATE_MESSAGE_FORMAT;
    public static Component NO_PLAYER_TO_REPLY;
    public static String SKULL_RECEIVED;
    public static Component NAME_UPDATED;
    public static Component NAME_CLEARED;
    public static Component LORE_UPDATED;
    public static Component LORE_CLEARED;
    public static String LORE_INDEX_OUT_OF_BOUNDS;
    public static Component ITEM_HAS_NO_LORE;
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
        // Global
        CORRECT_USAGE = component("general.correct-usage");
        MISSING_PERMISSIONS = component("general.missing-permissions");
        PLAYER_NOT_ONLINE = component("general.player-not-online");
        PLAYER_NEVER_PLAYED = component("general.player-never-played");
        CANT_USE_ON_YOURSELF = component("general.cant-use-on-yourself");
        TARGETS_ARE_THE_SAME = component("general.targets-are-the-same");
        NO_ITEM_IN_HAND = component("general.no-item-in-hand");
        // Azure
        AZURE_RELOAD_SUCCESS = component("commands.azure.reload.reload-success");
        AZURE_RELOAD_FAIL = component("commands.azure.reload.reload-fail");
        // TeleportCommandBundle
        TELEPORTED_TO_PLAYER = string("commands.teleport.you-have-been-teleported-to-player");
        TELEPORTED_TO_LOCATION = string("commands.teleport.you-have-been-teleported-to-location");
        TELEPORTED_PLAYER_TO_PLAYER = string("commands.teleport.teleported-player-to-player");
        TELEPORTED_PLAYER_TO_LOCATION = string("commands.teleport.teleported-player-to-location");
        PLAYER_TELEPORTED_TO_YOU = string("commands.teleport.player-teleported-to-you");
        // MessageCommandBundle
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
        return MiniMessage.get().parse(this.string(path));
    }

    /** Sends parsed component */
    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }

    /** Parses and sends component */
    public static void send(@NotNull CommandSender sender, @NotNull String text) {
        final Component component = MiniMessage.get().parse(text);
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
        final Component component = MiniMessage.get().parse(text);
        if (component != Component.empty()) {
            sender.sendMessage(identity, component);
        }
    }

}
