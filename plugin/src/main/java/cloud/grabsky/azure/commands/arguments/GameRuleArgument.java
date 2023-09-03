/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
