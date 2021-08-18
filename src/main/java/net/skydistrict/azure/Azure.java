package net.skydistrict.azure;

import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.milkbowl.vault.chat.Chat;
import net.skydistrict.azure.commands.AzureCommand;
import net.skydistrict.azure.config.Config;
import net.skydistrict.azure.config.Lang;
import net.skydistrict.azure.manager.TeleportRequestManager;
import net.skydistrict.azure.storage.DataManager;
import net.skydistrict.azure.storage.SQLManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Azure extends JavaPlugin {
    // Instances
    private static Azure instance;
    private ConsoleLogger consoleLogger;
    private Config config;
    private Lang lang;
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
        this.lang = new Lang(this);
        this.config = new Config(this);

        // Creating SQLManager instance and reloading config (+ passing credentials)
        // this.sql = new SQLManager();
        this.reload(false);

        this.teleportRequestManager = new TeleportRequestManager(this);

        // Registering commands
        final CommandManager commands = new CommandManager(this);
        commands.register(
                new AzureCommand(this)
        );

        // Hook into Vault if plugin is present
        final RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        this.chat = (rsp != null) ? rsp.getProvider() : null;

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
        config.reload(reloadCredentials);
        lang.reload();
        return true;
    }
}
