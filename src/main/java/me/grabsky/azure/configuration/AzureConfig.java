package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AzureConfig {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

    public static boolean DEBUG;

    // Disclaimer: All interval/time values are converted to final-usage unit.
    public static long PLAYER_DATA_SAVE_INTERVAL;
    public static long PLAYER_DATA_EXPIRES_AFTER;

    public static boolean CHAT_WEBHOOK_ENABLED;
    public static String CHAT_WEBHOOK_URL;
    public static long CHAT_COOLDOWN;
    public static Map<String, String> CHAT_FORMATS;

    public AzureConfig(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
    }

    // Reloads translations
    public void reload() {
        // Saving default config
        File file = new File(instance.getDataFolder() + File.separator + "config.yml");
        if(!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != 1) {
            consoleLogger.error(Global.OUTDATED_CONFIG);
        }
        DEBUG = fc.getBoolean("settings.debug", false);
        // Data
        PLAYER_DATA_SAVE_INTERVAL = fc.getLong("settings.data.player-data.save-interval", 300000); // No conversion needed
        PLAYER_DATA_EXPIRES_AFTER = fc.getLong("settings.data.player-data.data-expires-after", 300000); // No conversion needed
        // Chat
        CHAT_WEBHOOK_ENABLED = fc.getBoolean("settings.chat.webhook");
        CHAT_WEBHOOK_URL = fc.getString("settings.chat.webhook-url");
        CHAT_COOLDOWN = fc.getLong("settings.chat.cooldown");
        CHAT_FORMATS = new HashMap<>();
        for (final String group : fc.getConfigurationSection("settings.chat.format").getKeys(false)) {
            CHAT_FORMATS.put(group, fc.getString("settings.chat.format." + group));
        }
    }
}
