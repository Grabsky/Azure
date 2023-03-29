package cloud.grabsky.azure.arguments;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.WorldType;

import java.util.List;

import static java.util.Arrays.stream;
import static net.kyori.adventure.text.Component.text;

public enum WorldTypeArgument implements ArgumentParser<WorldType>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> WORLD_TYPES = stream(WorldType.values()).map((type) -> type.name().toLowerCase()).toList();

    @Override
    public List<String> provide(final RootCommandContext context) {
        return WORLD_TYPES;
    }

    @Override
    public WorldType parse(final RootCommandContext context, final ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final WorldType type : WorldType.values()) {
            if (type.toString().equalsIgnoreCase(value) == true)
                return type;
        }
        // ...
        throw new WorldTypeParseException(value);
    }

    /**
     * {@link WorldTypeParseException} is thrown when invalid value is provided for {@link WorldType} argument type.
     */
    public static final class WorldTypeParseException extends ArgumentParseException {

        public WorldTypeParseException(final String inputValue) {
            super(inputValue);
        }

        public WorldTypeParseException(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            context.getExecutor().asCommandSender().sendMessage(text("Invalid WorldType argument.", NamedTextColor.RED));
        }

    }

}
