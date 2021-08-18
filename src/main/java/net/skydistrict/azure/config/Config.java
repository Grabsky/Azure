package net.skydistrict.azure.config;

import me.grabsky.indigo.logger.ConsoleLogger;
import net.skydistrict.azure.Azure;
import net.skydistrict.azure.storage.SQLManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private final Azure instance;
    private final ConsoleLogger consoleLogger;

    public Config(Azure instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
    }

    public static int CACHE_MODE;
    public static int SOFT_CACHE_EXPIRE_AFTER;
    public static int TELEPORT_DELAY = 5; // s
    public static long TELEPORT_REQUEST_LIFESPAN = 30000; // ms

    // Reloads translations
    public void reload(boolean reloadCredentials) {
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
        // Passing credentials to SQLManager class
        if(reloadCredentials) {
            SQLManager sql = instance.getSQLManager();
            sql.setCredentials(
                    fc.getString("settings.data.storage.type"),
                    fc.getString("settings.data.storage.address"),
                    fc.getString("settings.data.storage.port"),
                    fc.getString("settings.data.storage.username"),
                    fc.getString("settings.data.storage.password"),
                    fc.getString("settings.data.storage.database")
            );
        }
        CACHE_MODE = fc.getInt("settings.data.cache-mode");
        SOFT_CACHE_EXPIRE_AFTER = fc.getInt("settings.data.soft-cache-expire-after");
    }

}
