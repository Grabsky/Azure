package me.grabsky.azure;

import me.grabsky.azure.commands.AzureCommand;
import me.grabsky.azure.commands.teleport.TeleportCommand;
import me.grabsky.azure.commands.teleport.TeleportHereCommand;
import me.grabsky.azure.commands.teleport.TeleportLocationCommand;
import me.grabsky.azure.config.AzureConfig;
import me.grabsky.azure.config.AzureLang;
import me.grabsky.azure.manager.TeleportRequestManager;
import me.grabsky.azure.storage.DataManager;
import me.grabsky.azure.storage.SQLManager;
import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.java.JavaPlugin;

public class Azure extends JavaPlugin {
    // Instances
    private static Azure instance;
    private ConsoleLogger consoleLogger;
    private AzureConfig config;
    private AzureLang lang;
    private SQLManager sql;
    private DataManager dataManager;
    private TeleportRequestManager teleportRequestManager;
    private Chat chat;
    // Getters
    public static Azure getInstance() { return instance; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public SQLManager getSQLManager() { return sql; }
    public DataManager getDataManager() { return dataManager; }
    public TeleportRequestManager getTeleportRequestManager() { return teleportRequestManager; }
    public Chat getVaultChat() { return chat; }

    @Override
    public void onEnable() {
        // Setting up instances
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        this.lang = new AzureLang(this);
        this.config = new AzureConfig(this);

        // Creating SQLManager instance and reloading config (+ passing credentials)
        // this.sql = new SQLManager();
        this.reload(false);

        this.teleportRequestManager = new TeleportRequestManager(this);

        // Registering commands
        final CommandManager commands = new CommandManager(this);
        commands.register(
                new AzureCommand(this),
                new TeleportCommand(this),
                new TeleportHereCommand(this),
                new TeleportLocationCommand(this)
        );

        // Hook into Vault if plugin is present
        // final RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        // this.chat = (rsp != null) ? rsp.getProvider() : null;

        // SQLManager should have database credentials; initializing pipeline
        // if(sql.initialize()) {
        //     consoleLogger.success("Successfully connected to database.");
        //     this.dataManager = new DataManager(this);
        // } else {
        //     consoleLogger.error("An error occurred while connecting to database.");
        // }
        // Running data unload task
        // PlayerCache.runUnloadTask(this);
    }

    @Override
    public void onDisable() {
        // sql.close();
        // save data
    }

    public boolean reload(boolean reloadCredentials) {
        try {
            config.reload(reloadCredentials);
            lang.reload();
            return true;
        } catch (Exception e) {
            consoleLogger.error("An error occurred while trying to reload the plugin.");
            e.printStackTrace();
            return false;
        }
    }
}
