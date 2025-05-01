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

import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.exception.CommandLogicException;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Command(name = "glow", permission = "azure.command.glow", usage = "/glow")
public final class GlowCommand extends RootCommand {

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final Player sender = context.getExecutor().asPlayer();
        // Toggling glow.
        sender.setGlowing(!sender.isGlowing());
        // Sending success message to the target.
        Message.of(PluginLocale.COMMAND_GLOW_SUCCESS).placeholder("state", getColoredBooleanLong(sender.isGlowing())).send(sender);
    }

    private Component getColoredBooleanLong(final boolean bool) {
        return (bool == true) ? PluginLocale.getBooleanLong(true).color(GREEN) : PluginLocale.getBooleanLong(false).color(RED);
    }

}

