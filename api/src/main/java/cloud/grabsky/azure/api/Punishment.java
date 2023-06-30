package cloud.grabsky.azure.api;

import cloud.grabsky.bedrock.util.Interval;
import cloud.grabsky.bedrock.util.Interval.Unit;
import org.jetbrains.annotations.NotNull;

public interface Punishment {

    @NotNull String getReason();

    @NotNull String getIssuer();

    @NotNull Interval getStartDate();

    @NotNull Interval getDuration();

    default @NotNull Interval getEndDate() {
        return this.getStartDate().and((long) this.getDuration().as(Unit.MILLISECONDS), Unit.MILLISECONDS);
    }

    default boolean isActive() {
        return System.currentTimeMillis() < this.getEndDate().as(Unit.MILLISECONDS);
    }

}
