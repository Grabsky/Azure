/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.azure;

import cloud.grabsky.azure.api.AzureAPI;
import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.commands.AzureCommand;
import cloud.grabsky.azure.commands.BanCommand;
import cloud.grabsky.azure.commands.DebugCommand;
import cloud.grabsky.azure.commands.DeleteCommand;
import cloud.grabsky.azure.commands.EnderchestCommand;
import cloud.grabsky.azure.commands.FeedCommand;
import cloud.grabsky.azure.commands.FlyCommand;
import cloud.grabsky.azure.commands.GameModeCommand;
import cloud.grabsky.azure.commands.GiveCommand;
import cloud.grabsky.azure.commands.HealCommand;
import cloud.grabsky.azure.commands.InventoryCommand;
import cloud.grabsky.azure.commands.InvulnerableCommand;
import cloud.grabsky.azure.commands.KickCommand;
import cloud.grabsky.azure.commands.MessageCommand;
import cloud.grabsky.azure.commands.MuteCommand;
import cloud.grabsky.azure.commands.PackCommand;
import cloud.grabsky.azure.commands.PlayerCommand;
import cloud.grabsky.azure.commands.RepairCommand;
import cloud.grabsky.azure.commands.ReplyCommand;
import cloud.grabsky.azure.commands.SkullCommand;
import cloud.grabsky.azure.commands.SpeedCommand;
import cloud.grabsky.azure.commands.SpyCommand;
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
import cloud.grabsky.azure.resourcepack.ResourcePackManager;
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
import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    private ResourcePackManager resourcePackManager;

    @Getter(AccessLevel.PUBLIC)
    private FileLogger punishmentsFileLogger;

    @Getter(AccessLevel.PUBLIC)
    private @Nullable DiscordApi discord;

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
        // ResourcePackManager Has to be initialized before configuration is reloaded.
        this.resourcePackManager = new ResourcePackManager(this);
        // Reloading and stopping the server in case of failure.
        if (this.onReload() == false) {
            this.getServer().shutdown();
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
            this.getLogger().severe("An error occured while trying to load worlds.");
            this.getLogger().severe("  " + e.getMessage());
        }
        // ...
        this.punishmentsFileLogger = new FileLogger(this, new File(new File(this.getDataFolder(), "logs"), "punishments.log"));
        // Setting-up RootCommandManager... (applying templates, registering commands)
        this.commandManager = new RootCommandManager(this)
                // Applying templates...
                .apply(CommandArgumentTemplate.INSTANCE)
                .apply(CommandExceptionTemplate.INSTANCE)
                // Registering dependencies...
                .registerDependency(Azure.class, instance)
                .registerDependency(LuckPerms.class, luckPerms)
                .registerDependency(ChatManager.class, chatManager)
                .registerDependency(AzureWorldManager.class, worldManager)
                .registerDependency(UserCache.class, userCache)
                // Registering commands...
                .registerCommand(AzureCommand.class)
                .registerCommand(BanCommand.class)
                .registerCommand(DeleteCommand.class)
                .registerCommand(EnderchestCommand.class)
                .registerCommand(FeedCommand.class)
                .registerCommand(GameModeCommand.class)
                .registerCommand(GiveCommand.class)
                .registerCommand(HealCommand.class)
                .registerCommand(InventoryCommand.class)
                .registerCommand(InvulnerableCommand.class)
                .registerCommand(FlyCommand.class)
                .registerCommand(KickCommand.class)
                .registerCommand(MessageCommand.class)
                .registerCommand(MuteCommand.class)
                .registerCommand(PackCommand.class)
                .registerCommand(PlayerCommand.class)
                .registerCommand(ReplyCommand.class)
                .registerCommand(SpeedCommand.class)
                .registerCommand(SpyCommand.class)
                .registerCommand(TeleportCommand.class)
                .registerCommand(UnbanCommand.class)
                .registerCommand(UnmuteCommand.class)
                .registerCommand(VanishCommand.class)
                .registerCommand(WorldCommand.class)
                .registerCommand(SkullCommand.class)
                .registerCommand(RepairCommand.class)
                // Registering debug commands...
                .registerCommand(DebugCommand.class);
        // Registering events...
        this.getServer().getPluginManager().registerEvents(chatManager, this);
        this.getServer().getPluginManager().registerEvents(resourcePackManager, this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // Initializing Discord API, logging-in to the bot.
        if (PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_COMMUNICATION == true) {
            // Logging error message when token is unspecified or empty.
            if (PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_BOT_TOKEN == null || PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_BOT_TOKEN.isEmpty() == true)
                this.getLogger().severe("Cannot establish connection with Discord API because specified token is incorrect.");
            // Trying to connect to Discord API. Failure should not stop the server but instead log error to the console.
            try {
                this.discord = new DiscordApiBuilder()
                        .setToken(PluginConfig.CHAT_DISCORD_WEBHOOK_TWO_WAY_BOT_TOKEN)
                        .addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS)
                        // For mentions to display properly.
                        .setUserCacheEnabled(true)
                        .setWaitForUsersOnStartup(true)
                        .addListener(chatManager)
                        .login().join();
            } catch (final RuntimeException e) {
                this.getLogger().severe("Could not establish connection with Discord API.");
                this.getLogger().severe("  " + e.getMessage());
            }
        }
        // Finalizing... (exposing instance to the API)
        AzureProvider.finalize(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Disconnecting from Discord API. Expected to be a blocking call.
        if (discord != null)
            discord.disconnect().join();
    }

    @Override
    public boolean onReload() {
        try {
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File localeCommands = ensureResourceExistence(this, new File(this.getDataFolder(), "locale_commands.json"));
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            // Reloading configuration files.
            mapper.map(
                    ConfigurationHolder.of(PluginLocale.class, locale),
                    ConfigurationHolder.of(PluginLocale.Commands.class, localeCommands),
                    ConfigurationHolder.of(PluginConfig.class, config)
            );
            // Reloading ResourcePackManager.
            resourcePackManager.reload();
            // Returning 'true' as reload finished without any exceptions.
            return true;
        } catch (final IllegalStateException | ConfigurationMappingException | IOException e) {
            this.getLogger().severe("An error occured while trying to reload plugin.");
            this.getLogger().severe("  " + e.getMessage());
            return false;
        }
    }


    @SuppressWarnings("UnstableApiUsage")
    public static final class PluginLoader implements io.papermc.paper.plugin.loader.PluginLoader {

        @Override
        public void classloader(final @NotNull PluginClasspathBuilder classpathBuilder) throws IllegalStateException {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Parsing the file.
            try (final InputStream in = getClass().getResourceAsStream("/paper-libraries.json")) {
                final PluginLibraries libraries = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
                // Adding repositorties.
                libraries.asRepositories().forEach(resolver::addRepository);
                // Adding dependencies.
                libraries.asDependencies().forEach(resolver::addDependency);
                // Adding library resolver.
                classpathBuilder.addLibrary(resolver);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
        private static final class PluginLibraries {

            private final Map<String, String> repositories;
            private final List<String> dependencies;

            public Stream<RemoteRepository> asRepositories() {
                return repositories.entrySet().stream().map(entry -> new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build());
            }

            public Stream<Dependency> asDependencies() {
                return dependencies.stream().map(value -> new Dependency(new DefaultArtifact(value), null));
            }

        }

    }

}
