package cloud.grabsky.azure.commands.arguments;

import cloud.grabsky.azure.configuration.PluginLocale;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.MissingInputException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Converts two {@link String} literals to {@link Direction}.
 */
public enum DirectionArgument implements ArgumentParser<DirectionArgument.Direction>, CompletionsProvider {
    /* SINGLETON */ INSTANCE;

    @AllArgsConstructor
    public static final class Direction {

        @Getter(AccessLevel.PUBLIC)
        private final Float yaw;

        @Getter(AccessLevel.PUBLIC)
        private final Float pitch;

    }

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) {
        return List.of("@yaw @pitch");
    }

    @Override
    public DirectionArgument.Direction parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final @NotNull String valueYaw = arguments.nextString();
        final @NotNull String valuePitch = arguments.nextString();
        // ...
        final @Nullable Player player = (context.getExecutor().isPlayer() == true) ? context.getExecutor().asPlayer() : null;
        // ...
        final Float yaw = (player != null && "@yaw".equalsIgnoreCase(valueYaw) == true) ? (Float) player.getLocation().getYaw() : parseFloat(valueYaw);
        final Float pitch = (player != null && "@pitch".equalsIgnoreCase(valuePitch) == true) ? (Float) player.getLocation().getPitch() : parseFloat(valuePitch);
        // ...
        if (yaw == null || pitch == null) {
            final String input = new StringBuilder()
                    .append(yaw != null ? toRoundedFloat(yaw) : valueYaw).append(" ")
                    .append(pitch != null ? toRoundedFloat(pitch) : valuePitch)
                    .toString();
            // ...
            throw new DirectionArgument.Exception(input);
        }
        // ...
        return new Direction(yaw, pitch);
    }

    private static @Nullable Float parseFloat(final @NotNull String value) {
        try {
            return Float.parseFloat(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private static String toRoundedFloat(final float num) {
        return String.format("%.2f", num);
    }

    /**
     * {@link DirectionArgument.Exception} is thrown when invalid yaw or pitch is provided for {@link DirectionArgument.Direction} argument type.
     */
    public static final class Exception extends ArgumentParseException {

        private Exception(final String inputValue) {
            super(inputValue);
        }

        private Exception(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            Message.of(PluginLocale.Commands.INVALID_DIRECTION).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
