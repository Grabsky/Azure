package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.LongArgument;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public enum WorldSeedArgument implements ArgumentParser<Long>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return (context.getExecutor().isPlayer() == true)
                ? List.of("@random", String.valueOf(context.getExecutor().asPlayer().getWorld().getSeed()))
                : List.of("@random");
    }

    @Override
    public Long parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final ArgumentQueue peek = arguments.peek();
        // ...
        final String value = arguments.nextString();
        // ...
        return (value.equalsIgnoreCase("@random") == true)
                ? new Random().nextLong()
                : LongArgument.DEFAULT_RANGE.parse(context, peek);
    }

}
