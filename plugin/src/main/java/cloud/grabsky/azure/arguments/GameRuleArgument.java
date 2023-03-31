package cloud.grabsky.azure.arguments;

import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Arrays.stream;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("rawtypes")
public enum GameRuleArgument implements ArgumentParser<GameRule>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> GAME_RULES = stream(GameRule.values()).map(GameRule::getName).toList();

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return GAME_RULES;
    }

    @Override
    public GameRule<?> parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final GameRule<?> gameRule : GameRule.values()) {
            if (gameRule.getName().equalsIgnoreCase(value) == true)
                return gameRule;
        }
        // ...
        throw new GameRuleParseException(value);
    }

    /**
     * {@link GameRuleParseException} is thrown when invalid value is provided for {@link GameRule} argument type.
     */
    public static final class GameRuleParseException extends ArgumentParseException {

        public GameRuleParseException(final String inputValue) {
            super(inputValue);
        }

        public GameRuleParseException(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            context.getExecutor().asCommandSender().sendMessage(text("Invalid GameRule<?> argument.", NamedTextColor.RED));
        }

    }

}
