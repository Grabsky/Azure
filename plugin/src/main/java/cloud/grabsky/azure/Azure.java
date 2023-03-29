package cloud.grabsky.azure;

import cloud.grabsky.azure.api.AzureAPI;
import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.arguments.GameRuleArgument;
import cloud.grabsky.azure.arguments.WorldEnvironmentArgument;
import cloud.grabsky.azure.arguments.WorldTypeArgument;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.commands.AzureCommand;
import cloud.grabsky.azure.commands.DeleteCommand;
import cloud.grabsky.azure.commands.GiveCommand;
import cloud.grabsky.azure.commands.NickCommand;
import cloud.grabsky.azure.commands.WorldCommand;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginConfig.DeleteButton;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.configuration.adapters.StandardTagResolverAdapter;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.commands.exception.IncompatibleSenderException;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.adapter.AbstractEnumJsonAdapter;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;
import static net.kyori.adventure.text.Component.text;

public final class Azure extends BedrockPlugin implements AzureAPI {

    @Getter(AccessLevel.PUBLIC)
    private static Azure instance;

    @Getter(AccessLevel.PUBLIC)
    private UserCache userCache;

    @Getter(AccessLevel.PUBLIC)
    private LuckPerms luckPerms;

    @Getter(AccessLevel.PRIVATE)
    private ConfigurationMapper mapper;

    @Getter(AccessLevel.PUBLIC)
    private ChatManager chat;

    @Override
    public Consumer<RootCommandManager> getCommandManagerTemplate() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // ...
        instance = this;
        // ...
        this.mapper = PaperConfigurationMapper.create(moshi -> {
            moshi.add(TagResolver.class, StandardTagResolverAdapter.INSTANCE);
            moshi.add(DeleteButton.Position.class, new AbstractEnumJsonAdapter<>(DeleteButton.Position.class, false) { /* DEFAULT */ });
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
        this.chat = new ChatManager(this);
        // ...
        final RootCommandManager commands = new RootCommandManager(this);
        // ...
        commands.setArgumentParser(WorldType.class, WorldTypeArgument.INSTANCE);
        commands.setCompletionsProvider(WorldType.class, WorldTypeArgument.INSTANCE);
        // ...
        commands.setArgumentParser(World.Environment.class, WorldEnvironmentArgument.INSTANCE);
        commands.setCompletionsProvider(World.Environment.class, WorldEnvironmentArgument.INSTANCE);
        // ...
        commands.setArgumentParser(GameRule.class, GameRuleArgument.INSTANCE);
        commands.setCompletionsProvider(GameRule.class, GameRuleArgument.INSTANCE);
        // overriding default exception handlers
        commands.setExceptionHandler(IncompatibleSenderException.class, (exc, context) -> {
            final String message = (exc.getExpectedType() == Player.class)
                    ? "This command can only be executed by a player."
                    : (exc.getExpectedType() == ConsoleCommandSender.class)
                        ? "This command can only be executed by a console"
                        : "You cannot execute that command.";
            context.getExecutor().asCommandSender().sendMessage(text(message, NamedTextColor.RED));
        });
        // registering commands
        commands.registerCommand(AzureCommand.class);
        commands.registerCommand(NickCommand.class);
        commands.registerCommand(GiveCommand.class);
        commands.registerCommand(WorldCommand.class);
        commands.registerCommand(new DeleteCommand(chat));
        // ........
        this.getServer().getPluginManager().registerEvents(chat, this);
        // ...
        AzureProvider.finalize(this);
    }

    public boolean reloadConfiguration() {
        try {
            return onReload();
        } catch (final ConfigurationMappingException exc) {
            return false;
        }
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException {
        try {
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            // ...
            mapper.map(PluginLocale.class, locale);
            mapper.map(PluginConfig.class, config);
            return true;
        } catch (final IOException exc) {
            throw new IllegalStateException(exc); // Re-throwing as runtime exception
        }
    }
    
}
