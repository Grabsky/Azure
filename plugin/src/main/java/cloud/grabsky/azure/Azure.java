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
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.WebhookMessageBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

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
    private @Nullable DiscordApi discord;

    private @Nullable BukkitTask activityRefreshTask;

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
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(chatManager, this);
        this.getServer().getPluginManager().registerEvents(resourcePackManager, this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // Initializing Discord API, logging-in to the bot.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true) {
            // Logging error message when token is unspecified or empty.
            if (PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN == null || PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN.isEmpty() == true)
                this.getLogger().severe("Cannot establish connection with Discord API because specified token is incorrect.");
            // Trying to connect to Discord API. Failure should not stop the server but instead log error to the console.
            try {
                this.discord = new DiscordApiBuilder()
                        .setToken(PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN)
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

    @Override @SneakyThrows
    public void onDisable() {
        super.onDisable();
        // ...
        if (discord != null) {
            // Forwarding message to webhook...
            if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
                // Setting message placeholders.
                final String message = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_STOP_MESSAGE_FORMAT);
                // Creating new instance of WebhookMessageBuilder.
                final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
                // Setting username if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                    builder.setDisplayName(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME));
                // Setting avatar if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                    builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR)).toURL());
                // Sending the message. Expected to be a blocking call.
                builder.send(discord, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL).join();
            }
            // Disconnecting from Discord API. Expected to be a blocking call.
            discord.disconnect().join();
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
            // Reloading discord bot custom activity.
            if (discord != null) {
                // Cancelling the current task.
                if (activityRefreshTask != null && activityRefreshTask.isCancelled() == false)
                    activityRefreshTask.cancel();
                // Setting configured activity if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState().isEmpty() == false)
                    // Scheduling a refreshing task if desired.
                    if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() > 0)
                        this.activityRefreshTask = this.getBedrockScheduler().repeat(0L, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() * 20L, Long.MAX_VALUE, (___) -> {
                            // Parsing string with PlaceholderAPI.
                            final String parsed = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState());
                            // Updating the activity.
                            discord.updateActivity(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), parsed);
                            // Continuing with the task.
                            return true;
                        });
                        // Otherwise, just setting the activity.
                    else discord.updateActivity(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState());
                // Otherwise, unsetting the activity.
                else discord.unsetActivity();
            }
            // Returning 'true' as reload finished without any exceptions.
            return true;
        } catch (final IllegalStateException | ConfigurationMappingException | IOException e) {
            this.getLogger().severe("An error occurred while trying to reload plugin.");
            this.getLogger().severe("  " + e.getMessage());
            return false;
        }
    }

    // Waiting for server to be fully enabled before updating discord activity. PlaceholderAPI extensions should be enabled by now.
    @SneakyThrows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(final ServerLoadEvent event) {
        // Setting configured activity if specified.
        if (discord != null) {
            // Forwarding message to webhook...
            if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
                // Setting message placeholders.
                final String message = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_START_MESSAGE_FORMAT);
                // Creating new instance of WebhookMessageBuilder.
                final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
                // Setting username if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                    builder.setDisplayName(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME));
                // Setting avatar if specified.
                if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                    builder.setDisplayAvatar(new URI(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR)).toURL());
                // Sending the message. Expected to be a blocking call.
                builder.send(discord, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL).join();
            }
            // Cancelling the current task.
            if (activityRefreshTask != null && activityRefreshTask.isCancelled() == false)
                activityRefreshTask.cancel();
            // Setting configured activity if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState().isEmpty() == false)
                // Scheduling a refreshing task if desired.
                if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() > 0)
                    this.activityRefreshTask = this.getBedrockScheduler().repeat(100L, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() * 20L, Long.MAX_VALUE, (___) -> {
                        // Parsing string with PlaceholderAPI.
                        final String parsed = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState());
                        // Updating the activity.
                        discord.updateActivity(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), parsed);
                        // Continuing with the task.
                        return true;
                    });
                // Otherwise, just setting the activity.
                else discord.updateActivity(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState());
        }
    }

}
