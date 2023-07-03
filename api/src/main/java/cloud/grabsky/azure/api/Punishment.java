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
        return this.getStartDate().add(this.getDuration());
    }

    default @NotNull Interval getDurationLeft() {
        return this.getEndDate().remove(Interval.now());
    }

    default boolean isPermanent() {
        return this.getDuration().as(Unit.MILLISECONDS) == Long.MAX_VALUE;
    }

    default boolean isActive() {
        return this.isPermanent() == true || this.getEndDate().as(Unit.MILLISECONDS) > System.currentTimeMillis();
    }

}
