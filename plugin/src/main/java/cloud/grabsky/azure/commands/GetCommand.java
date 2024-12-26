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
package cloud.grabsky.azure.commands;

import cloud.grabsky.azure.configuration.PluginItems;
import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

@Command(name = "get", permission = "azure.command.get", usage = "/get (item) (amount)")
public final class GetCommand extends RootCommand {

    private static final ExceptionHandler.Factory GET_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_GET_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) {
        return switch (index) {
            case 0 -> (_) -> PluginItems.ITEMS.keySet().stream().toList();
            case 1 -> CompletionsProvider.of("1", "16", "32", "64");
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Parsing arguments.
        final var input = arguments.next(String.class).asRequired(GET_USAGE);
        final var amount = arguments.next(Integer.class, IntegerArgument.ofRange(1, 64)).asOptional(1);
        // Checking whether specified item is configured or not.
        if (PluginItems.ITEMS.containsKey(input) == true) {
            // Getting the configured item.
            final ItemStack item = PluginItems.ITEMS.get(input);
            // Updating the amount to match one specified using argument.
            item.setAmount(amount);
            // Adding item to sender's inventory.
            sender.getInventory().addItem(item);
            // Preparing hover-able item text component.
            final Component display = empty().color(WHITE).append(item.displayName()).hoverEvent(item.asHoverEvent());
            // Sending success message to the sender.
            Message.of(PluginLocale.COMMAND_GET_SUCCESS).placeholder("amount", amount).placeholder("item", display).send(sender);
            // Returning...
            return;
        }
        Message.of(PluginLocale.COMMAND_GET_FAILURE_NOT_FOUND).placeholder("input", input).send(sender);
    }

}
