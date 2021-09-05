package me.grabsky.azure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.grabsky.azure.commands.*;
import me.grabsky.azure.commands.teleport.TeleportCommand;
import me.grabsky.azure.commands.teleport.TeleportHereCommand;
import me.grabsky.azure.commands.teleport.TeleportLocationCommand;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.listener.PlayerJoinListener;
import me.grabsky.azure.manager.PointManager;
import me.grabsky.azure.manager.TeleportRequestManager;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.azure.storage.objects.JsonLocation;
import me.grabsky.azure.storage.objects.deserializers.JsonLocationDeserializer;
import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Azure extends JavaPlugin {
    // Instances
    private static Azure instance;
    private ConsoleLogger consoleLogger;
    private AzureConfig config;
    private AzureLang lang;
    private Gson gson;
    private DataManager dataManager;
    private TeleportRequestManager teleportRequestManager;
    private PointManager pointManager;
    // Getters
    public static Azure getInstance() { return instance; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public Gson getGson() { return gson; }
    public DataManager getDataManager() { return dataManager; }
    public TeleportRequestManager getTeleportRequestManager() { return teleportRequestManager; }
    public PointManager getLocationsManager() { return pointManager; }

    private int pointsSaveTaskId;

    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        // Loading config & translations
        this.lang = new AzureLang(this);
        this.config = new AzureConfig(this);
        this.reload();
        // Registering custom json deserializers
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(JsonLocation.class, new JsonLocationDeserializer())
                .create();
        // Initializing TeleportRequestManager
        this.dataManager = new DataManager(this);
        this.teleportRequestManager = new TeleportRequestManager(this);
        // Initializing PointManager
        this.pointManager = new PointManager(this);
        pointManager.loadAll();
        this.pointsSaveTaskId = pointManager.runSaveTask();
        // Registering commands
        final CommandManager commands = new CommandManager(this);
        commands.register(
                new AzureCommand(this),
                new TeleportCommand(this),
                new TeleportHereCommand(this),
                new TeleportLocationCommand(this),
                new EnchantCommand(this),
                new RenameCommand(this),
                new LoreCommand(this),
                new SkullCommand(this),
                new PointCommand(this),
                new PlayerInfoCommand(this)
        );
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void onDisable() {
        // Cancelling tasks to prevent concurrent saves
        Bukkit.getScheduler().cancelTask(pointsSaveTaskId);
        // Saving data synchronously
        pointManager.saveAll();
    }

    public boolean reload() {
        try {
            config.reload();
            lang.reload();
            return true;
        } catch (Exception e) {
            consoleLogger.error("An error occurred while trying to reload the plugin.");
            e.printStackTrace();
            return false;
        }
    }
}
