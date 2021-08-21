package me.grabsky.azure;

import me.grabsky.azure.commands.*;
import me.grabsky.azure.commands.teleport.TeleportCommand;
import me.grabsky.azure.commands.teleport.TeleportHereCommand;
import me.grabsky.azure.commands.teleport.TeleportLocationCommand;
import me.grabsky.azure.configuration.AzureConfig;
import me.grabsky.azure.configuration.AzureLang;
import me.grabsky.azure.manager.TeleportRequestManager;
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
    private TeleportRequestManager teleportRequestManager;
    private Chat chat;
    // Getters
    public static Azure getInstance() { return instance; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public TeleportRequestManager getTeleportRequestManager() { return teleportRequestManager; }
    public Chat getVaultChat() { return chat; }

    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        // Loading config & translations
        this.lang = new AzureLang(this);
        this.config = new AzureConfig(this);
        this.reload();
        // Initializing TeleportRequestManager
        this.teleportRequestManager = new TeleportRequestManager(this);
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
                new SkullCommand(this)
        );
        // Hook into Vault if plugin is present
        // final RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        // this.chat = (rsp != null) ? rsp.getProvider() : null;
    }

    @Override
    public void onDisable() {
        // Save data
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
