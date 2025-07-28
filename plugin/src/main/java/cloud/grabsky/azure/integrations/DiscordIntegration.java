package cloud.grabsky.azure.integrations;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.configuration.PluginConfig;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.azure.user.AzureUserCache;
import cloud.grabsky.bedrock.components.Message;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fellbaum.jemoji.EmojiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

/*
 * TO-DO:
 * - JOIN / QUIT MESSAGES
 * - PUNISHMENTS
 * -
 */
public final class DiscordIntegration implements Listener {

    private final Azure plugin;

    @Getter(AccessLevel.PUBLIC)
    private final JDA client;

    private @Nullable BukkitTask activityRefreshTask;

    // Stores all currently active codes.
    private final Cache<UUID, String> codes = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final WebhookClient WebhookForwardingChat;

    @Getter(AccessLevel.PUBLIC) // This webhook is accessed from other classes.
    private final WebhookClient WebhookForwardingJoinQuit;

    private final WebhookClient WebhookForwardingStartStop;

    @Getter(AccessLevel.PUBLIC) // This webhook is accessed from other classes.
    private final WebhookClient WebhookForwardingPunishments;

    private final WebhookClient WebhookForwardingDeathMessages;

    @Getter(AccessLevel.PUBLIC) // This webhook is accessed from other classes.
    private final WebhookClient WebhookForwardingAuctionHouseListings;

    // Strict MiniMessage instance used for deserialization of translatable components and colors appended internally in Discord -> Minecraft chat forwarding.
    private static final MiniMessage STRICT_MINI_MESSAGE = MiniMessage.builder().tags(TagResolver.resolver(StandardTags.translatable(), StandardTags.color())).build();

    private static final int SUCCESS_COLOR = 0x57F287;
    private static final int FAILURE_COLOR = 0xED4245;

    public DiscordIntegration(final @NotNull Azure plugin) {
        this.plugin = plugin;
        // ...
        this.client = JDABuilder.createDefault(PluginConfig.DISCORD_INTEGRATIONS_DISCORD_BOT_TOKEN)
                // Enabling required intents.
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                // Enabling members cache.
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                // Enabling support for annotated event listeners.
                .setEventManager(new AnnotatedEventManager())
                // Registering event listeners.
                .addEventListeners(this)
                // Building and logging in. This method does not block.
                .build();
        // Setting up webhooks.
        WebhookForwardingChat = (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_URL) : null;
        WebhookForwardingPunishments = (PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_PUNISHMENTS_FORWARDING_WEBHOOK_URL) : null;
        WebhookForwardingJoinQuit = (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL) : null;
        WebhookForwardingDeathMessages = (PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_URL) : null;
        WebhookForwardingAuctionHouseListings = (PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_AUCTION_LISTINGS_FORWARDING_WEBHOOK_URL) : null;
        WebhookForwardingStartStop = (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_ENABLED)
                ? JDAWebhookClient.withUrl(PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL) : null;
        // Registering Bukkit event listeners.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown() {
        // Shutting down the JDA.
        this.client.shutdown();
        // Closing all webhook clients.
        if (WebhookForwardingChat != null)
            WebhookForwardingChat.close();
        if (WebhookForwardingJoinQuit != null)
            WebhookForwardingJoinQuit.close();
        if (WebhookForwardingStartStop != null)
            WebhookForwardingStartStop.close();
        if (WebhookForwardingPunishments != null)
            WebhookForwardingPunishments.close();
        if (WebhookForwardingDeathMessages != null)
            WebhookForwardingDeathMessages.close();
        if (WebhookForwardingAuctionHouseListings != null)
            WebhookForwardingAuctionHouseListings.close();
        // Unregistering event listeners.
        HandlerList.unregisterAll(this);
    }

    @SubscribeEvent
    private void onShutdown(final @NotNull ShutdownEvent event) {
        // Cancelling the current task.
        if (activityRefreshTask != null && activityRefreshTask.isCancelled() == false) {
            activityRefreshTask.cancel();
            activityRefreshTask = null;
        }
    }

    @SubscribeEvent
    private void onReady(final @NotNull ReadyEvent event) {
        // Cancelling the current task.
        if (activityRefreshTask != null && activityRefreshTask.isCancelled() == false)
            activityRefreshTask.cancel();
        // Setting configured activity if specified.
        if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState().isEmpty() == false)
            // Scheduling a refreshing task if desired.
            if (PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() > 0)
                this.activityRefreshTask = plugin.getBedrockScheduler().repeat(0L, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getRefreshRate() * 20L, Long.MAX_VALUE, (_) -> {
                    // Parsing string with PlaceholderAPI.
                    final String parsed = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState());
                    // Updating the activity.
                    client.getPresence().setPresence(Activity.of(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), parsed), false);
                    // Continuing with the task.
                    return true;
                });
                // Otherwise, just setting the activity.
            else client.getPresence().setPresence(Activity.of(PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getType(), PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_BOT_ACTIVITY.getState())), false);
        // Otherwise, unsetting the activity.
        else client.getPresence().setActivity(null);
    }

    @SubscribeEvent
    private void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        // Skipping in case chat forwarding is not enabled.
        if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED == false)
            return;
        // Skipping irrelevant channels and bot replies.
        if (event.getChannel().getId().equals(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHANNEL_ID) == true && event.getAuthor().isBot() == false && event.getAuthor().isSystem() == false) {
            // Getting the message and stripping all tags and formatting. Not final because it's modified in the next step.
            String message = event.getMessage().getContentDisplay();
            // Replacing all emojis in this message with a translatable component.
            message = EmojiManager.replaceAllEmojis(message, (emoji) -> "<white><lang:'" + emoji.getDiscordAliases().getFirst() + "'></white>");
            // Appending '(Gif)' or similar string to the message if it contains a gif attachment.
            message = (message.startsWith("https://tenor.com/view/") == true)
                    ? (message.isBlank() == false)
                            ? message + " " + PluginLocale.CHAT_ATTACHMENT
                            : PluginLocale.CHAT_ATTACHMENT
                    : message;
            // Appending '(Attachment)' or similar string to the message if it contains an attachment like image etc.
            message = (event.getMessage().getAttachments().isEmpty() == false)
                    ? (message.isBlank() == false)
                            ? message + " " + PluginLocale.CHAT_ATTACHMENT
                            : PluginLocale.CHAT_ATTACHMENT
                    : message;
            // Appending '(Re: User)' in front of the message if it's a reply.
            if (event.getMessage().getReferencedMessage() != null) {
                final @Nullable User user = plugin.getUserCache().fromDiscord(event.getMessage().getAuthor().getId());
                message = (event.getMessage().getReferencedMessage() != null)
                        ? "<#848484>(Re: <#A89468>" + (user != null ? user.getName() : event.getMessage().getAuthor().getName()) + "</#A89468>)</#848484> " + message
                        : message;
            }
            // Parsing the message using MiniMessage, with just the translatable and color tags enabled.
            final Component finalMessage = STRICT_MINI_MESSAGE.deserialize(message);
            // Getting the User from discord identifier. Can be null if no matching user is found.
            final @Nullable User user = plugin.getUserCache().fromDiscord(event.getMessage().getAuthor().getId());
            // Getting the message components.
            final String effectiveName = (user != null) ? user.getName() : event.getMessage().getAuthor().getName();
            // Sending message to the console.
            Message.of(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CONSOLE_FORMAT)
                    .placeholder("username", effectiveName)
                    .placeholder("message", finalMessage)
                    .send(Bukkit.getConsoleSender());
            // Sending message to all players.
            Message.of(PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_CHAT_FORMAT)
                    .placeholder("username", effectiveName)
                    .placeholder("message", finalMessage)
                    .send(Bukkit.getServer().filterAudience(audience -> audience instanceof Player));
        }
    }

    @SubscribeEvent
    public void onButtonInteraction(final @NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("verification_button") == true) {
            // Sending error message if user already has a role.
            if (event.getMember() != null && event.getMember().getRoles().stream().anyMatch(it -> it.getId().equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID) == true) == true) {
                event.replyEmbeds(new EmbedBuilder()
                        .setDescription(PluginLocale.DISCORD_VERIFICATION_FAILURE_VERIFIED)
                        .setColor(FAILURE_COLOR)
                        .build()
                ).setEphemeral(true).queue();
            }
            // Otherwise, showing a modal.
            event.getInteraction().replyModal(Modal.create("verification_modal", PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_MODAL_LABEL)
                    .addActionRow(TextInput.create("verification_code", PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_MODAL_INPUT_LABEL, TextInputStyle.SHORT)
                            .setMinLength(7).setMaxLength(7).setRequired(true).build()
                    ).build()
            ).queue();
        }
    }

    @SubscribeEvent
    public void onModalInteraction(final @NotNull ModalInteractionEvent event) {
        // Returning if interaction does not come from a guild.
        if (event.getMember() == null || event.getGuild() == null)
            return;
        if (event.getModalId().equals("verification_modal") == true) {
            final @Nullable ModalMapping component = event.getInteraction().getValue("verification_code");
            // Returning
            if (component == null)
                return;
            final String code = component.getAsString();
            // ...
            final @Nullable UUID uniqueId = codes.asMap().entrySet().stream().filter(it -> it.getValue().equals(code) == true).map(Map.Entry::getKey).findFirst().orElse(null);
            // Sending error message if UUID is null.
            if (uniqueId == null) {
                event.getInteraction().replyEmbeds(
                        new EmbedBuilder().setDescription(PluginLocale.DISCORD_VERIFICATION_FAILURE_INVALID_CODE).setColor(FAILURE_COLOR).build()
                ).setEphemeral(true).queue();
                // Returning...
                return;
            }
            // Getting User object from the UUID.
            final @Nullable User user = plugin.getUserCache().getUser(uniqueId);
            // Throwing IllegalStateException if User object for this UUID returned 'null'.
            if (user == null)
                throw new IllegalStateException("Verification failed. Missing User object for: " + uniqueId);
            // Updating Discord ID associated with this user.
            user.setDiscordId(event.getInteraction().getMember().getId());
            // Saving...
            plugin.getUserCache().as(AzureUserCache.class).saveUser(user);
            // Adding permission to the player, if configured.
            if ("".equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION) == false)
                // Loading LuckPerms' User and adding permission node to them.
                plugin.getLuckPerms().getUserManager().modifyUser(uniqueId, (it) -> {
                    it.data().add(PermissionNode.builder(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION).build());
                });
            // Adding role if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID.isEmpty() == false) {
                // Getting configured server.
                final Guild guild = event.getGuild();
                // Getting verification role.
                final @Nullable Role role = guild.getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID);
                // Getting Member object associated with this user, if the role still exists.
                if (role != null)
                    guild.addRoleToMember(event.getMember(), role).queue();
            }
            // Sending success message to the user.
            event.getInteraction().replyEmbeds(
                    new EmbedBuilder().setDescription(PluginLocale.DISCORD_VERIFICATION_SUCCESS).setColor(SUCCESS_COLOR).build()
            ).setEphemeral(true).queue();
            // Sending success message to the player.
            final Player player = Bukkit.getPlayer(uniqueId);
            // ...
            if (player != null && player.isOnline() == true)
                Message.of(PluginLocale.COMMAND_VERIFY_SUCCESS).send(player);
            // Invalidating...
            codes.invalidate(uniqueId);
        }
    }

    /* BUKKIT STUFF */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerLoad(final @NotNull ServerLoadEvent event) {
        // Forwarding message to webhook...
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_ENABLED == true && PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_URL.isEmpty() == false) {
            // Setting message placeholders.
            final String message = PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_START_MESSAGE_FORMAT);
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setUsername(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(null, PluginConfig.DISCORD_INTEGRATIONS_START_AND_STOP_FORWARDING_WEBHOOK_AVATAR));
            // Sending the message. Expected to be a blocking call.
            WebhookForwardingStartStop.send(builder.build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerChat(final @NotNull AsyncChatEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        if (event.viewers().isEmpty() == false) {
            // Serializing Component to plain String.
            final String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            // Creating new instance of WebhookMessageBuilder.
            final club.minnced.discord.webhook.send.WebhookMessageBuilder builder = new WebhookMessageBuilder()
                    .setAllowedMentions(AllowedMentions.none())
                    .setContent(plainMessage.startsWith("xaero-waypoint:") == true ? "[Xaero's Waypoint]" : plainMessage);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setUsername(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_CHAT_FORWARDING_WEBHOOK_AVATAR));
            // Sending the message.
            WebhookForwardingChat.send(builder.build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerJoinForward(final @NotNull PlayerJoinEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
            // Setting message placeholders.
            final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_JOIN_MESSAGE_FORMAT);
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setUsername(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR));
            // Sending the message.
            WebhookForwardingJoinQuit.send(builder.build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerQuitForward(final @NotNull PlayerQuitEvent event) {
        // Skipping in case discord integrations are not enabled or misconfigured.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
            // Setting message placeholders.
            final String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_QUIT_MESSAGE_FORMAT);
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(message);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setUsername(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_JOIN_AND_QUIT_FORWARDING_WEBHOOK_AVATAR));
            // Sending the message.
            WebhookForwardingJoinQuit.send(builder.build());
        }
    }

    // THIS IS NOT A LISTENER, THIS METHOD IS CALLED BY CHAT MANAGER
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerDeathForward(final @NotNull PlayerDeathEvent event, final String text) {
        // Discord integration... Must be handled here because we're cancelling the death message right after this event is called.
        if (PluginConfig.DISCORD_INTEGRATIONS_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_ENABLED == false || PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_URL.isEmpty() == true)
            return;
        // Forwarding message to webhook...
        if (plugin.getUserCache().getUser(event.getPlayer()).isVanished() == false) {
            // Setting message placeholders.
            final String webhookMessage = MiniMessage.miniMessage().stripTags(text)
                    .replace("› ", "") // Very dirty workaround but this had to be done ASAP; Will be improved in the future.
                    .replace("<prefix>", "")
                    .replace("<suffix>", "")
                    .replace("<victim>", "**" + event.getPlayer().getName() + "**")
                    .replace("<victim_displayname>", "**" + event.getPlayer().getName() + "**")
                    .replace("<attacker>", "**" + (event.getDamageSource().getCausingEntity() != null ? event.getDamageSource().getCausingEntity().getName() : "") + "**")
                    .replace("<attacker_displayname>", "**" + (event.getDamageSource().getCausingEntity() != null ? event.getDamageSource().getCausingEntity().getName() : "") + "**")
                    .replace("<mob>", "**" + (event.getDamageSource().getCausingEntity() != null && event.getDamageSource().getCausingEntity() instanceof Mob mob ? PluginLocale.MOBS.getOrDefault(mob.getType().translationKey(), mob.getType().translationKey()) : "") + "**");
            // Creating new instance of WebhookMessageBuilder.
            final WebhookMessageBuilder builder = new WebhookMessageBuilder().setContent(webhookMessage);
            // Setting username if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_USERNAME.isEmpty() == false)
                builder.setUsername(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_USERNAME));
            // Setting avatar if specified.
            if (PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_AVATAR.isEmpty() == false)
                builder.setAvatarUrl(PlaceholderAPI.setPlaceholders(event.getPlayer(), PluginConfig.DISCORD_INTEGRATIONS_DEATH_MESSAGE_FORWARDING_WEBHOOK_AVATAR));
            // Sending the message.
            WebhookForwardingDeathMessages.send(builder.build());
        }
    }

    /* VERIFICATION */

//    public void updateVerificationRole(final @NotNull String discordId) throws IllegalStateException {
//        // Getting configured server.
//        final @Nullable Guild guild = client.getGuildById(PluginConfig.DISCORD_INTEGRATIONS_DISCORD_SERVER_ID);
//        // Throwing IllegalStateException if configured server is inaccessible.
//        if (guild == null)
//            throw new IllegalStateException("Server is inaccessible: " + PluginConfig.DISCORD_INTEGRATIONS_DISCORD_SERVER_ID);
//        // Checking if user has left the server
//        if (guild.getMemberById(discordId) != null) {
//            // Removing permission from the player, if configured.
//            if ("".equals(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION) == false)
//                // Loading LuckPerms' User and removing permission from them.
//                plugin.getLuckPerms().getUserManager().modifyUser(thisUser.getUniqueId(), (it) -> {
//                    it.data().remove(PermissionNode.builder(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_PERMISSION).build());
//                });
//            // Getting verification role.
//            final @Nullable org.javacord.api.entity.permission.Role role = server.getRoleById(PluginConfig.DISCORD_INTEGRATIONS_VERIFICATION_ROLE_ID).orElse(null);
//            // If the role exists, it is now being removed from the user.
//            if (role != null)
//                plugin.getDiscord().getUserById(thisUser.getDiscordId()).thenAccept(it -> server.removeRoleFromUser(it, role));
//            // Removing associated ID.
//            thisUser.setDiscordId(null);
//            // Saving... Hopefully this won't cause any CME or data loss due to the fact we're saving file earlier too.
//            // I think in the worst case scenario either this or country info would be lost.
//            this.saveUser(thisUser);
//        }
//    }

}
