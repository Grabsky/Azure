package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AzureConfig {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

    // Disclaimer: All interval/time values are converted to final-usage unit.
    public static long PLAYER_DATA_SAVE_INTERVAL;
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
            consoleLogger.error(Global.OUTDATED_CONFIG);
        }
        PLAYER_DATA_SAVE_INTERVAL = fc.getLong("settings.data.player-data.save-interval", 300000); // No conversion needed
        PLAYER_DATA_EXPIRES_AFTER = fc.getLong("settings.data.player-data.data-expires-after", 300000); // No conversion needed
        POINTS_SAVE_INTERVAL = fc.getLong("settings.data.point.save-interval", 600000) / 1000 * 20; // Converted to ticks

    }

}
