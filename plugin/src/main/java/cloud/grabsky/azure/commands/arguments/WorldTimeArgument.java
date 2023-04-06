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
