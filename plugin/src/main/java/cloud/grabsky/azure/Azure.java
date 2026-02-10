/*
 * Azure (https://github.com/Grabsky/Azure)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.azure;

import cloud.grabsky.azure.api.AzureAPI;
import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.azure.chat.ChatManager;
import cloud.grabsky.azure.commands.AdminChatCommand;
import cloud.grabsky.azure.commands.AzureCommand;
import cloud.grabsky.azure.commands.BackCommand;
import cloud.grabsky.azure.commands.BanCommand;
import cloud.grabsky.azure.commands.DefeatCommand;
import cloud.grabsky.azure.commands.DeleteCommand;
import cloud.grabsky.azure.commands.EnderchestCommand;
import cloud.grabsky.azure.commands.FeedCommand;
import cloud.grabsky.azure.commands.FlyCommand;
import cloud.grabsky.azure.commands.GameModeCommand;
import cloud.grabsky.azure.commands.GiveCommand;
import cloud.grabsky.azure.commands.HatCommand;
import cloud.grabsky.azure.commands.HealCommand;
import cloud.grabsky.azure.commands.InventoryCommand;
import cloud.grabsky.azure.commands.InvulnerableCommand;
import cloud.grabsky.azure.commands.KickCommand;
import cloud.grabsky.azure.commands.MessageCommand;
import cloud.grabsky.azure.commands.MuteCommand;
import cloud.grabsky.azure.commands.NickCommand;
import cloud.grabsky.azure.commands.PackCommand;
import cloud.grabsky.azure.commands.PlayerCommand;
import cloud.grabsky.azure.commands.RepairCommand;
import cloud.grabsky.azure.commands.ReplyCommand;
import cloud.grabsky.azure.commands.ScheduleRestartCommand;
import cloud.grabsky.azure.commands.SkullCommand;
import cloud.grabsky.azure.commands.SpeedCommand;
import cloud.grabsky.azure.commands.SpyCommand;
import cloud.grabsky.azure.commands.TeleportCommand;
import cloud.grabsky.azure.commands.UnbanCommand;
import cloud.grabsky.azure.commands.UnmuteCommand;
import cloud.grabsky.azure.commands.UnverifyCommand;
import cloud.grabsky.azure.commands.VanishCommand;
import cloud.grabsky.azure.commands.VerifyCommand;
import cloud.grabsky.azure.commands.WorldCommand;
import cloud.grabsky.azure.commands.templates.CommandArgumentTemplate;
import cloud.grabsky.azure.commands.templates.CommandExceptionTemplate;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginConfig.DeleteButton;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.configuration.adapters.BossBarAdapterFactory;
import cloud.grabsky.azure.configuration.adapters.TagResolverAdapter;
import cloud.grabsky.azure.integrations.AuroraQuestsIntegration;
import cloud.grabsky.azure.integrations.DiscordIntegration;
import cloud.grabsky.azure.integrations.ExcellentShopIntegration;
import cloud.grabsky.azure.listener.PlayerListener;
import cloud.grabsky.azure.resourcepack.ResourcePackManager;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.azure.util.FileLogger;
import cloud.grabsky.azure.world.AzureWorldManager;
import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.helpers.Conditions;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationHolder;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.adapter.AbstractEnumJsonAdapter;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

public final class Azure extends BedrockPlugin implements AzureAPI, Listener {

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
    private DiscordIntegration discordIntegration;

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
        // ResourcePackManager has to be initialized before configuration is reloaded.
        this.resourcePackManager = new ResourcePackManager(this);
        // Reloading and stopping the server in case of failure.
        if (this.onReload() == false) {
            this.getServer().shutdown();
        }
        // Creating new instance of UserCache.
        this.userCache = new AzureUserCache(this);
        // Registering event listeners defined inside AzureUserCache class.
        this.getServer().getPluginManager().registerEvents((AzureUserCache) userCache, this);
        // Getting LuckPerms API from the provider.
        this.luckPerms = LuckPermsProvider.get();
        // Creating new instance of ChatManager.
        this.chatManager = new ChatManager(this);
        // Loading list of inappropriate words.
        chatManager.loadInappropriateWords();
        // Creating new instance of WorldManager.
        this.worldManager = new AzureWorldManager(this);
        // Loading worlds with autoLoad == true
        try {
            this.worldManager.loadWorlds();
        } catch (final IOException e) {
            this.getLogger().severe("An error occurred while trying to load worlds.");
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
                .registerCommand(AdminChatCommand.class)
                .registerCommand(AzureCommand.class)
                .registerCommand(BanCommand.class)
                .registerCommand(DefeatCommand.class)
                .registerCommand(DeleteCommand.class)
                .registerCommand(EnderchestCommand.class)
                .registerCommand(FeedCommand.class)
                .registerCommand(GameModeCommand.class)
                .registerCommand(GiveCommand.class)
                .registerCommand(HatCommand.class)
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
                .registerCommand(NickCommand.class)
                .registerCommand(VerifyCommand.class)
                .registerCommand(UnverifyCommand.class)
                .registerCommand(BackCommand.class)
                .registerCommand(ScheduleRestartCommand.class);
        // Registering events...
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(chatManager, this);
        this.getServer().getPluginManager().registerEvents(resourcePackManager, this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // Finalizing... (exposing instance to the API)
        AzureProvider.finalize(this);
        // Registering PlaceholderAPI placeholders...
        Placeholders.INSTANCE.register();
        // Registering AuroraQuests integration...
        AuroraQuestsIntegration.initialize(this);
        // Registering ExcellentShop integration...
        ExcellentShopIntegration.initialize(this);
    }

    @Override
    public void onDisable() {
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true) {
            this.getDiscordIntegration().onServerShutdown();
            this.getDiscordIntegration().shutdown();
        }
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
            // Reloading filtered words.
            if (this.chatManager != null)
                chatManager.loadInappropriateWords();
            // Unregistering PAPI expansion if already registered.
            if (Placeholders.INSTANCE.isRegistered() == true)
                Placeholders.INSTANCE.unregister();
            // Registering the expansion again.
            Placeholders.INSTANCE.register();
            // Shutting down JDA instance if started.
            if (this.discordIntegration != null) {
                this.getLogger().info("Shutting down Discord (JDA) integration...");
                this.discordIntegration.shutdown();
                this.discordIntegration = null;
            }
            // Initializing Discord API, logging-in to the bot.
            if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true) {
                this.getLogger().info("Enabling Discord (JDA) integration...");
                // Logging error message when token is unspecified or empty.
                if (PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN == null || PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN.isEmpty() == true)
                    this.getLogger().severe("Cannot establish connection with Discord API because specified token is incorrect.");
                // Trying to connect to Discord API. Failure should not stop the server but instead log error to the console.
                try {
                    this.discordIntegration = new DiscordIntegration(this);
                } catch (final RuntimeException e) {
                    this.getLogger().severe("Could not establish connection with Discord API.");
                    this.getLogger().severe("  " + e.getMessage());
                }
            }
            // Returning 'true' as reload finished without any exceptions.
            return true;
        } catch (final ConfigurationMappingException | IllegalStateException | IOException e) {
            this.getLogger().severe("Reloading of the plugin failed due to following error(s):");
            this.getLogger().severe(" (1) " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null)
                this.getLogger().severe(" (2) " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            // Returning false, as plugin has failed to reload.
            return false;
        }
    }

    /**
     * Logs a debug message to the console if debug mode is enabled in the plugin configuration.
     */
    public void debug(final @NotNull String message) {
        if (PluginConfig.DEBUG_MODE == true)
            this.getLogger().warning("[D] " + message);
    }

    public static final class Placeholders extends PlaceholderExpansion {
        public static final Placeholders INSTANCE = new Placeholders(); // SINGLETON

        // SimpleDateFormat responsible for formatting countdown time.
        private final static SimpleDateFormat COUNTDOWN_FORMAT = new SimpleDateFormat("HH:mm:ss");

        static {
            COUNTDOWN_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public @NotNull String getAuthor() {
            return "Grabsky";
        }

        @Override
        public @NotNull String getIdentifier() {
            return "azure";
        }

        @Override
        public @NotNull String getVersion() {
            return Azure.getInstance().getPluginMeta().getVersion();
        }

        private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(
                TagResolver.resolver(
                        StandardTags.color(),
                        StandardTags.gradient(),
                        StandardTags.rainbow()
                )
        ).build();

        @Override
        public String onRequest(final @NotNull OfflinePlayer offlinePlayer, final @NotNull String params) {
            // Placeholder: %azure_countdown_[TIMESTAMP]%
            if (params.startsWith("countdown_") == true) {
                try {
                    final String number = params.replace("countdown_", "");
                    return COUNTDOWN_FORMAT.format(new Date(Math.clamp(Long.parseLong(number) - System.currentTimeMillis(), 0, Long.MAX_VALUE)));
                } catch (final NumberFormatException e) {
                    return "00:00:00";
                }
            }
            // Placeholder: %azure_strip_tags:<_input_>%
            if (params.startsWith("strip_unsafe_tags:") == true) {
                final String input = PlaceholderAPI.setBracketPlaceholders(offlinePlayer, params.split(":")[1]);
                return MiniMessage.builder().tags(TagResolver.empty()).build().stripTags(
                        input,
                        // Stripping tags below. This leaves color, gradient and rainbow tags only.
                        StandardTags.decorations(),
                        StandardTags.hoverEvent(),
                        StandardTags.clickEvent(),
                        StandardTags.clickEvent(),
                        StandardTags.keybind(),
                        StandardTags.translatable(),
                        StandardTags.translatableFallback(),
                        StandardTags.insertion(),
                        StandardTags.font(),
                        StandardTags.reset(),
                        StandardTags.newline(),
                        StandardTags.selector(),
                        StandardTags.selector(),
                        StandardTags.score(),
                        StandardTags.nbt(),
                        StandardTags.pride(),
                        StandardTags.shadowColor()
                );
            }
            // Placeholder: %azure_is_displayname_valid:<input>%
            if (params.startsWith("is_displayname_valid:") == true) {
                final String input = PlaceholderAPI.setBracketPlaceholders(offlinePlayer, params.split(":")[1]);
                final Component displayName = MINI_MESSAGE.deserialize(input.replace(" ", ""));
                // Sending an error message if nick contains invalid formatting or is not equal to the player's name. Name modifications are not allowed, only colors.
                if (input.contains("&") == true || input.contains("§") == true || plainText().serialize(displayName).equals(offlinePlayer.getName()) == false) {
                    return "false";
                }
                return "true";
            }
            // Getting the UUID of the player.
            final UUID uniqueId = offlinePlayer.getUniqueId();
            // Getting the placeholder identifier.
            final String identifier = params.toLowerCase();
            // Handling placeholders based on the identifier.
            return switch (identifier) {
                // Placeholder: %azure_is_idle%
                case "is_idle" -> (offlinePlayer instanceof Player player && player.getIdleDuration().get(ChronoUnit.SECONDS) >= PluginConfig.GENERAL_PLAYER_IDLE_TIME) ? "true" : "false";
                // Placeholder: %azure_mined_blocks%
                case "total_mined_blocks" -> calculateTotalBlock(offlinePlayer, Statistic.MINE_BLOCK);
                // Placeholder: %azure_displayname%
                case "displayname" -> {
                    if (Azure.getInstance() != null && Azure.getInstance().getUserCache() != null) {
                        // Getting the User instance.
                        final User user = Azure.getInstance().getUserCache().getUser(uniqueId);
                        // Returning player name if user is null.
                        if (user == null || user.getDisplayName() == null) {
                            // Getting the LuckPerms' User instance.
                            final net.luckperms.api.model.user.User luckpermsUser = Azure.getInstance().getLuckPerms().getUserManager().getUser(uniqueId);
                            // Returning player's name with prefix if present.
                            if (luckpermsUser != null)
                                yield Conditions.requirePresent(luckpermsUser.getCachedData().getMetaData().getPrefix(), "") + Conditions.requirePresent(luckpermsUser.getCachedData().getMetaData().getMetaValue("color"), "") + offlinePlayer.getName();
                            // Otherwise, returning returning just the player's name.
                            yield offlinePlayer.getName();
                        }
                        // Returning the User display name, if present.
                        else if (user.getDisplayName() != null)
                            yield user.getDisplayName();
                            // Otherwise, returning player's name.
                        else yield offlinePlayer.getName();
                    }
                    yield null;
                }
                // Placeholder: %azure_is_verified%
                case "is_verified" -> (Azure.getInstance().getUserCache() != null && Azure.getInstance().getUserCache().hasUser(uniqueId) == true)
                        ? String.valueOf(Azure.getInstance().getUserCache().getUser(uniqueId).getDiscordId() != null)
                        : "N/A";
                // Placeholder: %azure_max_level%
                case "max_level" -> (Azure.getInstance().getUserCache() != null && Azure.getInstance().getUserCache().hasUser(uniqueId) == true)
                        ? String.valueOf(Azure.getInstance().getUserCache().getUser(uniqueId).getMaxLevel())
                        : "N/A";
                // Anything else is not a valid placeholder.
                default -> null;
            };
        }

        // This should be more efficient than 'Statistic' expansion for PlaceholderAPI.
        private static String calculateTotalBlock(final @NotNull OfflinePlayer player, final @NotNull Statistic statistic) {
            final AtomicLong total = new AtomicLong();
            // Iterating over all blocks in the registry.
            Registry.BLOCK.forEach(it -> {
                // Converting BlockType to Material, because this is what Player#getStatistic expects as a parameter.
                final @Nullable Material material = it.asMaterial();
                // Returning for null materials.
                if (material == null)
                    return;
                // Incrementing the total amount.
                total.addAndGet(player.getStatistic(statistic, material));
            });
            // Returning...
            return total.toString();
        }

    }

    /* PLUGIN LOADER; FOR USE WITH PLUGIN-YML FOR GRADLE */

    @SuppressWarnings("UnstableApiUsage")
    public static final class PluginLoader implements io.papermc.paper.plugin.loader.PluginLoader {

        @Override
        public void classloader(final @NotNull PluginClasspathBuilder classpathBuilder) throws IllegalStateException {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Parsing the file.
            try (final InputStream in = getClass().getResourceAsStream("/paper-libraries.json")) {
                final PluginLibraries libraries = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
                // Adding repositories to the maven library resolver.
                libraries.asRepositories().forEach(resolver::addRepository);
                // Adding dependencies to the maven library resolver.
                libraries.asDependencies().forEach(resolver::addDependency);
                // Adding library resolver to the classpath builder.
                classpathBuilder.addLibrary(resolver);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
        private static class PluginLibraries {

            private final Map<String, String> repositories;
            private final List<String> dependencies;

            public Stream<RemoteRepository> asRepositories() {
                return repositories.entrySet().stream().map(entry -> {
                    try {
                        // Replacing Maven Central repository with a pre-configured mirror.
                        // See: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/#loaders
                        if (entry.getValue().contains("maven.org") == true || entry.getValue().contains("maven.apache.org") == true) {
                            return new RemoteRepository.Builder(entry.getKey(), "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build();
                        }
                        return new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build();
                    } catch (final NoSuchFieldError e) {
                        return new RemoteRepository.Builder(entry.getKey(), "default", "https://maven-central.storage-download.googleapis.com/maven2").build();
                    }
                });
            }

            public Stream<Dependency> asDependencies() {
                return dependencies.stream().map(value -> new Dependency(new DefaultArtifact(value), null));
            }
        }
    }

}
