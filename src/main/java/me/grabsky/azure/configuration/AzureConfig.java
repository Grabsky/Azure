package me.grabsky.azure.configuration;

import me.grabsky.azure.Azure;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AzureConfig {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

    public AzureConfig(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
    }

    public static int CACHE_MODE;
    public static int SOFT_CACHE_EXPIRE_AFTER;
    public static int TELEPORT_DELAY = 5; // s
    public static long TELEPORT_REQUEST_LIFESPAN = 30000; // ms

    // Reloads translations
    public void reload() {
        // Saving default config
        File file = new File(instance.getDataFolder() + "/config.yml");
        if(!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != 1) {
            consoleLogger.error("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        CACHE_MODE = fc.getInt("settings.data.cache-mode");
        SOFT_CACHE_EXPIRE_AFTER = fc.getInt("settings.data.soft-cache-expire-after");
    }

}
