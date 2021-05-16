package net.skydistrict.azure.config;

import net.skydistrict.azure.Azure;
import net.skydistrict.azure.storage.SQLManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private final Azure instance;

    public Config(Azure instance) {
        this.instance = instance;
    }

    public static int CACHE_MODE;
    public static int SOFT_CACHE_EXPIRE_AFTER;

    // Reloads translations
    public void reload(boolean reloadCredentials) {
        // Saving default config
        File file = new File(instance.getDataFolder() + "/config.yml");
        if(!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
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
