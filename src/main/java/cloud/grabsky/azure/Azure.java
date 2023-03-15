package cloud.grabsky.azure;

import cloud.grabsky.azure.arguments.GameRuleArgument;
import cloud.grabsky.azure.arguments.WorldEnvironmentArgument;
import cloud.grabsky.azure.arguments.WorldTypeArgument;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.commands.*;
import cloud.grabsky.azure.configuration.AzureConfig;
import cloud.grabsky.azure.configuration.AzureConfig.DeleteButton;
import cloud.grabsky.azure.configuration.AzureLocale;
import cloud.grabsky.azure.configuration.adapters.StandardTagResolverAdapter;
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
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;
import static net.kyori.adventure.text.Component.text;

public final class Azure extends BedrockPlugin implements Listener {

    @Getter(AccessLevel.PUBLIC)
    private static Azure instance;

    @Getter(AccessLevel.PUBLIC)
    private LuckPerms luckPerms;

    @Getter(AccessLevel.PRIVATE)
    private ConfigurationMapper mapper;

    @Getter(AccessLevel.PUBLIC)
    private ChatManager chat;

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
        // ...
        if (this.onReload() == false) {
            return; // Plugin should be disabled automatically whenever exception is thrown.
        }
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
            mapper.map(AzureLocale.class, locale);
            mapper.map(AzureConfig.class, config);
            return true;
        } catch (final IOException exc) {
            throw new IllegalStateException(exc); // Re-throwing as runtime exception
        }
    }

}
