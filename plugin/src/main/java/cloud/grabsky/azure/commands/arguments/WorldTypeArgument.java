package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum WorldTypeArgument implements ArgumentParser<WorldType>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> WORLD_TYPES = List.of("amplified", "default", "flat", "largebiomes");

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return WORLD_TYPES;
    }

    @Override
    public WorldType parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final WorldType type : WorldType.values()) {
            if (type.toString().equalsIgnoreCase(value) == true)
                return type;
        }
        // ...
        throw new WorldTypeArgument.Exception(value);
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for {@link WorldType} argument type.
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
            Message.of(PluginLocale.Commands.INVALID_WORLD_TYPE).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
