package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import cloud.grabsky.commands.exception.NumberParseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class IntervalArgument implements ArgumentParser<Interval>, CompletionsProvider {
    /* SINGLETON */ public static final IntervalArgument DEFAULT_RANGE = new IntervalArgument(Long.MIN_VALUE, Long.MAX_VALUE);

    public static IntervalArgument ofRange(final long min, final long max, final Unit rangeUnit) {
        return new IntervalArgument(min * rangeUnit.getFactor(), max * rangeUnit.getFactor());
    }

    @Getter(AccessLevel.PUBLIC)
    private final long min;

    @Getter(AccessLevel.PUBLIC)
    private final long max;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        final String value = context.getInput().at(context.getInput().length());
        final String[] split = value.split("(?<=\\d)(?=\\D)");
        // ...
        final @Nullable Long num = parseLong(split[0]);
        // ...
        if (num != null) {
            return Stream.of(split[0] + "s", split[0] + "min", split[0] + "h", split[0] + "d", split[0] + "m", split[0] + "y")
                    .filter(completion -> {
                        final String[] completionSplit = completion.split("(?<=\\d)(?=\\D)");
                        // Choosing the the right unit.
                        final @Nullable Unit unit = switch (completionSplit[1].toLowerCase()) {
                            case "s" -> Unit.SECONDS;
                            case "min" -> Unit.MINUTES;
                            case "h" -> Unit.HOURS;
                            case "d" -> Unit.DAYS;
                            case "m" -> Unit.MONTHS;
                            case "y" -> Unit.YEARS;
                            default -> null;
                        };
                        // Checking if parsed unit is not null.
                        if (unit != null)
                            // Returning 'true' if interval is in range.
                            return inRange((long) Interval.of(num, unit).as(Unit.MILLISECONDS), min, max);
                        // Returning 'false' if invalid unit has been provided.
                        return false;
                    }).toList();
        }
        return new ArrayList<>();
    }

    @Override
    public Interval parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // ...
        final String[] split = value.split("(?<=\\d)(?=\\D)");
        // Throwing exception when split total elements is not 2.
        if (split.length != 2)
            throw new IntervalArgument.ParseException(value);
        // Parsing to long.
        final @Nullable Long num = parseLong(split[0]);
        // Checking if parsed long is not null.
        if (num != null) {
            // Choosing the the right unit.
            final @Nullable Unit unit = switch (split[1].toLowerCase()) {
                case "s" -> Unit.SECONDS;
                case "min" -> Unit.MINUTES;
                case "h" -> Unit.HOURS;
                case "d" -> Unit.DAYS;
                case "m" -> Unit.MONTHS;
                case "y" -> Unit.YEARS;
                default -> null;
            };
            // Checking if parsed unit is not null.
            if (unit != null) {
                final Interval result = Interval.of(num, unit);
                // ...
                if (inRange((long) result.as(Unit.MILLISECONDS), min, max) == true)
                    return result;
                throw new IntervalArgument.RangeException(value, min, max);
            }
        }
        throw new IntervalArgument.ParseException(value);
    }

    private @Nullable Long parseLong(final String value) {
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }


    /**
     * {@link ParseException} is thrown when invalid number is provided for {@link Interval} argument type.
     */
    public static final class ParseException extends NumberParseException {

        private ParseException(final String inputValue) {
            super(inputValue);
        }

        private ParseException(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final @NotNull RootCommandContext context) {
            Message.of(PluginLocale.Commands.INVALID_INTERVAL)
                    .placeholder("input", super.inputValue)
                    .send(context.getExecutor().asCommandSender());
        }
    }

    /**
     * {@link RangeException} is thrown when provided {@link Interval} is out of specified range.
     */
    public static final class RangeException extends NumberParseException {

        @Getter(AccessLevel.PUBLIC)
        private final long min;

        @Getter(AccessLevel.PUBLIC)
        private final long max;

        private RangeException(final String inputValue, final long min, final long max) {
            super(inputValue);
            this.min = min;
            this.max = max;
        }

        private RangeException(final String inputValue, final long min, final long max, final Throwable cause) {
            super(inputValue, cause);
            this.min = min;
            this.max = max;
        }

        @Override
        public void accept(final @NotNull RootCommandContext context) {
            Message.of(PluginLocale.Commands.INVALID_INTERVAL_NOT_IN_RANGE)
                    .placeholder("input", super.getInputValue())
                    .placeholder("min", Interval.of(this.getMin(), Unit.MILLISECONDS).toString())
                    .placeholder("max", Interval.of(this.getMax(), Unit.MILLISECONDS).toString())
                    .send(context.getExecutor().asCommandSender());
        }
    }

}
