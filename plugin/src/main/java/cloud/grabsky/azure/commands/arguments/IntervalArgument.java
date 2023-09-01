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
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class IntervalArgument implements ArgumentParser<Interval>, CompletionsProvider {
    /* SINGLETON */ public static final IntervalArgument DEFAULT_RANGE = new IntervalArgument(Long.MIN_VALUE, Long.MAX_VALUE);

    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<=\\d)(?=\\D)");

    public static IntervalArgument ofRange(final long min, final long max, final Unit rangeUnit) {
        return new IntervalArgument(min * rangeUnit.getFactor(), max * rangeUnit.getFactor());
    }

    @Getter(AccessLevel.PUBLIC)
    private final long min;

    @Getter(AccessLevel.PUBLIC)
    private final long max;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        final String value = context.getInput().at(context.getInput().maxIndex(), "");
        final String[] split = SPLIT_PATTERN.split(value);
        // ...
        final @Nullable Long num = parseLong(split[0]);
        // Checking that the number is not null.
        if (num != null) {
            // Filtering and returning completions for intervals that are in specified min-max range.
            return Stream.of(
                    Pair.of(Interval.of(num, Unit.SECONDS), Unit.SECONDS),
                    Pair.of(Interval.of(num, Unit.MINUTES), Unit.MINUTES),
                    Pair.of(Interval.of(num, Unit.HOURS), Unit.HOURS),
                    Pair.of(Interval.of(num, Unit.DAYS), Unit.DAYS),
                    Pair.of(Interval.of(num, Unit.MONTHS), Unit.MONTHS),
                    Pair.of(Interval.of(num, Unit.YEARS), Unit.YEARS)
            ).filter(pair -> inRange((long) pair.first().as(Unit.MILLISECONDS), min, max) == true).map(pair -> num + pair.second().getShortCode()).toList();
        }
        return new ArrayList<>();
    }

    @Override
    public Interval parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue queue) throws ArgumentParseException, MissingInputException {
        final String value = queue.next(String.class).asRequired();
        // Splitting on position between digit and letter.
        final String[] split = SPLIT_PATTERN.split(value);
        // Throwing exception when split total elements is not 2.
        if (split.length != 2)
            throw new IntervalArgument.ParseException(value);
        // Parsing to long.
        final @Nullable Long num = parseLong(split[0]);
        final @Nullable Unit unit = Unit.fromShortCode(split[1].toLowerCase());
        // Throwing an exception if either number or unit has not been provided and is null.
        if (num == null || unit == null)
            throw new IntervalArgument.ParseException(value);
        // Creating a new Interval instance.
        final Interval result = Interval.of(num, unit);
        // Returning Interval if in range.
        if (inRange((long) result.as(Unit.MILLISECONDS), min, max) == true)
            return result;
        // Throwing an exception otherwise.
        throw new IntervalArgument.RangeException(value, min, max);
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
