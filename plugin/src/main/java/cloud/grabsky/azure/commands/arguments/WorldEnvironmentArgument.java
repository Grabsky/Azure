package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

public enum WorldEnvironmentArgument implements ArgumentParser<World.Environment>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> WORLD_ENVIRONMENTS = List.of("normal", "nether", "the_end");

    @Override
    public @NotNull List<String> provide(RootCommandContext context) {
        return WORLD_ENVIRONMENTS;
    }

    @Override
    public World.Environment parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final World.Environment env : World.Environment.values()) {
            if (env.toString().equalsIgnoreCase(value))
                return env;
        }
        // ...
        throw new WorldEnvironmentParseException(value);
    }

    /**
     * {@link WorldEnvironmentParseException} is thrown when invalid value is provided for {@link World.Environment} argument type.
     */
    public static final class WorldEnvironmentParseException extends ArgumentParseException {

        public WorldEnvironmentParseException(final String inputValue) {
            super(inputValue);
        }

        public WorldEnvironmentParseException(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            context.getExecutor().asCommandSender().sendMessage(text("Invalid World.Environment argument.", NamedTextColor.RED));
        }

    }

}
