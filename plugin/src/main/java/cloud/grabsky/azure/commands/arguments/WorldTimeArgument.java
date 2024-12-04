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

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.LongArgument;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public enum WorldTimeArgument implements ArgumentParser<Long>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return List.of("@morning", "@noon", "@evening", "@midnight");
    }

    @Override
    public Long parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        // Creating a copy/snapshot of current ArgumentQueue, so it can be delegated to LongArgument later on.
        final ArgumentQueue argumentsPeek = arguments.peek();
        final String value = arguments.nextString().toLowerCase();
        // ...
        return switch (value) {
            case "@morning" -> 0L;
            case "@noon" -> 6000L;
            case "@evening" -> 12000L;
            case "@midnight" -> 18000L;
            // Calling LongArgument with ArgumentQueue snapshot because value have already been consumed.
            default -> LongArgument.ofRange(0, 24000).parse(context, argumentsPeek);
        };
    }

}
