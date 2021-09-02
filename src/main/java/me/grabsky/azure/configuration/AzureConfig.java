package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AzureConfig {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

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
        POINTS_SAVE_INTERVAL = fc.getLong("settings.data.point.save-interval", 600) * 20L;

    }

}
