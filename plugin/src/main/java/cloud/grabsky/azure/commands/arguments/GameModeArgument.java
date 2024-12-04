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
package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.GameMode;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public enum GameModeArgument implements ArgumentParser<GameMode>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> GAME_MODES = List.of("adventure", "creative", "survival", "spectator");

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return GAME_MODES;
    }

    @Override
    public GameMode parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final GameMode mode : GameMode.values()) {
            if (mode.toString().equalsIgnoreCase(value) == true)
                return mode;
        }
        // ...
        throw new GameModeArgument.Exception(value);
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for {@link GameMode} argument type.
     */
    public static final class Exception extends ArgumentParseException {

        public Exception(final String inputValue) {
            super(inputValue);
        }

        public Exception(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            Message.of(PluginLocale.Commands.INVALID_GAMEMODE).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
