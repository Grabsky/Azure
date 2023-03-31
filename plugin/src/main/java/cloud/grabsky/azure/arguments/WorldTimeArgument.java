package cloud.grabsky.azure.arguments;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public enum WorldTimeArgument implements ArgumentParser<Long>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return List.of("@morning", "@noon", "@evening", "@midnight");
    }

    @Override
    public Long parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        try {
            return switch (value) {
                case "@morning" -> 0L;
                case "@noon" -> 6000L;
                case "@evening" -> 12000L;
                case "@midnight" -> 18000L;
                default -> Long.parseLong(value);
            };
        } catch (final NumberFormatException exc) {
            throw new WorldTimeArgument.Exception(value, exc);
        }
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for time argument type.
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
            context.getExecutor().asCommandSender().sendMessage(text("Invalid Long argument.", NamedTextColor.RED));
        }

    }

}
