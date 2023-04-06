package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.GameRule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public enum GameRuleArgument implements ArgumentParser<GameRule>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    private static final List<String> GAME_RULES = Stream.of(GameRule.values()).map(GameRule::getName).toList();

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return GAME_RULES;
    }

    @Override
    public GameRule<?> parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        for (final var rule : GameRule.values()) {
            if (rule.getName().equalsIgnoreCase(value) == true)
                return rule;
        }
        // ...
        throw new GameRuleArgument.Exception(value);
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for {@link GameRule} argument type.
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
            Message.of(PluginLocale.Commands.INVALID_GAMERULE).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
