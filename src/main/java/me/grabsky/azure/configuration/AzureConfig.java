package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AzureConfig {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

    public static String COUNTRY_API_TOKEN;
    public static long PLAYER_SAVE_INTERVAL;
    public static long PLAYER_DATA_EXPIRES_AFTER;
    public static long POINTS_SAVE_INTERVAL;

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
            consoleLogger.error("Your config.yml file is outdated. Plugin may not work properly.");
        }
        COUNTRY_API_TOKEN = fc.getString("settings.data.country-api-token", "API_KEY");
        PLAYER_SAVE_INTERVAL = fc.getLong("settings.data.player-data.save-interval", 300) * 20L;
        PLAYER_DATA_EXPIRES_AFTER = fc.getLong("settings.data.player-data.data-expires-after", 300) * 1000;
        POINTS_SAVE_INTERVAL = fc.getLong("settings.data.point.save-interval", 600) * 20L;

    }

}
