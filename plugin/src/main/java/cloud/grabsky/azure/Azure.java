package cloud.grabsky.azure;

import cloud.grabsky.azure.api.AzureAPI;
import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.commands.AzureCommand;
import cloud.grabsky.azure.commands.BanCommand;
import cloud.grabsky.azure.commands.DeleteCommand;
import cloud.grabsky.azure.commands.EnderchestCommand;
import cloud.grabsky.azure.commands.FeedCommand;
import cloud.grabsky.azure.commands.GameModeCommand;
import cloud.grabsky.azure.commands.GiveCommand;
import cloud.grabsky.azure.commands.HealCommand;
import cloud.grabsky.azure.commands.InventoryCommand;
import cloud.grabsky.azure.commands.InvulnerableCommand;
import cloud.grabsky.azure.commands.KickCommand;
import cloud.grabsky.azure.commands.MuteCommand;
import cloud.grabsky.azure.commands.PackCommand;
import cloud.grabsky.azure.commands.PlayerCommand;
import cloud.grabsky.azure.commands.SpeedCommand;
import cloud.grabsky.azure.commands.TeleportCommand;
import cloud.grabsky.azure.commands.UnbanCommand;
import cloud.grabsky.azure.commands.UnmuteCommand;
import cloud.grabsky.azure.commands.VanishCommand;
import cloud.grabsky.azure.commands.WorldCommand;
import cloud.grabsky.azure.commands.templates.CommandArgumentTemplate;
import cloud.grabsky.azure.commands.templates.CommandExceptionTemplate;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginConfig.DeleteButton;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.configuration.adapters.BossBarAdapterFactory;
import cloud.grabsky.azure.configuration.adapters.TagResolverAdapter;
import cloud.grabsky.azure.listener.PlayerListener;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.azure.util.FileLogger;
import cloud.grabsky.azure.world.AzureWorldManager;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationHolder;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.adapter.AbstractEnumJsonAdapter;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Azure extends BedrockPlugin implements AzureAPI {

    @Getter(AccessLevel.PUBLIC)
    private static Azure instance;

    @Getter(AccessLevel.PUBLIC)
    private UserCache userCache;

    @Getter(AccessLevel.PUBLIC)
    private LuckPerms luckPerms;

    @Getter(AccessLevel.PUBLIC)
    private ChatManager chatManager;

    @Getter(AccessLevel.PUBLIC)
    private AzureWorldManager worldManager;

    @Getter(AccessLevel.PUBLIC)
    private RootCommandManager commandManager;

    @Getter(AccessLevel.PUBLIC)
    private FileLogger punishmentsFileLogger;

    private ConfigurationMapper mapper;

    @Override
    public void onEnable() {
        super.onEnable();
        // ...
        instance = this;
        // ...
        this.mapper = PaperConfigurationMapper.create(moshi -> {
            moshi.add(TagResolver.class, TagResolverAdapter.INSTANCE);
            moshi.add(DeleteButton.Position.class, new AbstractEnumJsonAdapter<>(DeleteButton.Position.class, false) { /* DEFAULT */ });
            moshi.add(BossBarAdapterFactory.INSTANCE);
        });
        // this#onReload throws ConfigurationMappingException which should stop the server in case of failure.
        if (this.onReload() == false) {
            return;
        }
        // ...
        this.userCache = new AzureUserCache(this);
        this.getServer().getPluginManager().registerEvents((AzureUserCache) userCache, this);
        // ...
        this.luckPerms = LuckPermsProvider.get();
        // ...
        this.chatManager = new ChatManager(this);
        // ...
        this.worldManager = new AzureWorldManager(this);
        // Loading worlds with autoLoad == true
        try {
            this.worldManager.loadWorlds();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        // ...
        this.punishmentsFileLogger = new FileLogger(this, new File(new File(this.getDataFolder(), "logs"), "punishments.log"));
        // Setting-up RootCommandManager... (applying templates, registering commands)
        this.commandManager = new RootCommandManager(this)
                .apply(CommandArgumentTemplate.INSTANCE)
                .apply(CommandExceptionTemplate.INSTANCE)
                .registerCommand(AzureCommand.class)
                .registerCommand(GiveCommand.class)
                .registerCommand(SpeedCommand.class)
                .registerCommand(PackCommand.class)
                .registerCommand(new GameModeCommand(this))
                .registerCommand(TeleportCommand.class)
                .registerCommand(new WorldCommand(this))
                .registerCommand(new DeleteCommand(this))
                .registerCommand(new VanishCommand(this))
                .registerCommand(new BanCommand(this))
                .registerCommand(new MuteCommand(this))
                .registerCommand(new UnbanCommand(this))
                .registerCommand(new UnmuteCommand(this))
                .registerCommand(new KickCommand(this))
                .registerCommand(InventoryCommand.class)
                .registerCommand(EnderchestCommand.class)
                .registerCommand(new PlayerCommand(this))
                .registerCommand(HealCommand.class)
                .registerCommand(FeedCommand.class)
                .registerCommand(InvulnerableCommand.class);
        // ...
        // Registering events...
        this.getServer().getPluginManager().registerEvents(chatManager, this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // Finalizing... (exposing instance to the API)
        AzureProvider.finalize(this);
    }

    public boolean reloadConfiguration() {
        try {
            return onReload();
        } catch (final IllegalStateException | ConfigurationMappingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException, IllegalStateException {
        try {
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File localeCommands = ensureResourceExistence(this, new File(this.getDataFolder(), "locale_commands.json"));
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            // ...
            mapper.map(
                    ConfigurationHolder.of(PluginLocale.class, locale),
                    ConfigurationHolder.of(PluginLocale.Commands.class, localeCommands),
                    ConfigurationHolder.of(PluginConfig.class, config)
            );
            return true;
        } catch (final IOException e) {
            throw new IllegalStateException(e); // Re-throwing as runtime exception.
        }
    }


    /**
     * Some {@link NamespacedKey} instances used within the {@link Azure} plugin are defined here.
     */
    public static final class Keys {

        public static final NamespacedKey IS_VANISHED = new NamespacedKey("azure", "is_vanished");

    }
}
