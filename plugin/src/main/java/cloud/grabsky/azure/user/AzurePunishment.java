package cloud.grabsky.azure.user;

import cloud.grabsky.azure.api.Punishment;
import cloud.grabsky.bedrock.util.Interval;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class AzurePunishment implements Punishment {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String reason;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String issuer;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Interval startDate;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Interval duration;

}
