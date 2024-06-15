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
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.Azure;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "skull", permission = "azure.command.skull", usage = "/skull (texture)")
public final class SkullCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Azure plugin;


    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    private static final ExceptionHandler.Factory SKULL_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_SKULL_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();

        // Trying to handle command as if Player (name) was provided.
        try {
            // Getting the next argument from peek() as to make sure value can be reused by other handlers, defined under this try...catch block.
            final Player target = arguments.peek().next(Player.class).asOptional(sender);
            // ...
            // Returning in case in case specified target is different from sender, and sender does not have permissions to create heads of other players.
            if (sender != target && sender.hasPermission(this.getPermission() + ".others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Creating and adding skull to player inventory.
            sender.getInventory().addItem(
                    new ItemBuilder(Material.PLAYER_HEAD, 1).setSkullTexture(target).build()
            );
            // Sending success message to the player.
            Message.of(PluginLocale.COMMAND_SKULL_SUCCESS_PLAYER).placeholder("player", target).send(sender);
            // Returning as to prevent further code execution.
            return;
        } catch (final CommandLogicException e) { /* IGNORING TO CONTINUE */ }

        // Returning in case sender does not have permissions to create heads of other players or custom heads.
        if (sender.hasPermission(this.getPermission() + ".others") == false) {
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
            return;
        }

        // Getting the value. It should be either name of an offline player or base64 value.
        final String value = arguments.next(String.class).asRequired(SKULL_USAGE);

        // Wrapping onto asynchronous task as further instructions may involve blocking operations.
        plugin.getBedrockScheduler().runAsync(0L, (task) -> {

            // Matching value with valid username regex.
            if (USERNAME_PATTERN.matcher(value).find() == true) {
                // Creating player profile from the name.
                final PlayerProfile profile = Bukkit.createProfile(value);
                // Completing the profile.
                profile.complete();
                // Sending error message to the sender in case PlayerProfile has not been completed. In our case, this usually means that provided name does not point to any profile.
                if (profile.isComplete() == true) {
                    // Scheduling item creation and inventory management onto the main thread.
                    plugin.getBedrockScheduler().run(0L, (otherTask) -> {
                        // Creating and adding skull to player inventory.
                        sender.getInventory().addItem(
                                new ItemBuilder(Material.PLAYER_HEAD, 1).setSkullTexture(profile).build()
                        );
                        // Sending success message to the sender.
                        Message.of(PluginLocale.COMMAND_SKULL_SUCCESS_PLAYER).placeholder("player", profile.getName()).send(sender);
                    });
                    // Returning as to prevent further code execution.
                    return;
                }
                // Sending failure message to the sender.
                Message.of(PluginLocale.Commands.INVALID_PLAYER).placeholder("input", value).send(sender);
                return;
            }

            // Trying to handle command as if Base64 encoded String was provided...
            try {
                // Decoding provided String from Base64 and validating the result using Gson. When that fails, other parse methods are tried.
                new Gson().getAdapter(JsonElement.class).fromJson(
                        new String(Base64.getDecoder().decode(value.getBytes()))
                );
                // Scheduling item creation and inventory management onto the main thread.
                plugin.getBedrockScheduler().run(0L, (otherTask) -> {
                    // Creating and adding skull to player inventory.
                    sender.getInventory().addItem(
                            new ItemBuilder(Material.PLAYER_HEAD, 1).setSkullTexture(value).build()
                    );
                    // Sending success message to the sender.
                    Message.of(PluginLocale.COMMAND_SKULL_SUCCESS_BASE64).send(sender);
                });
            } catch (final IllegalArgumentException | IOException e) {
                // Sending error message to the sender.
                Message.of(PluginLocale.COMMAND_SKULL_FAILURE).send(sender);
            }

        });

    }

}