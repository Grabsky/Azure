package cloud.grabsky.azure.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class BiContainer<L, R> {

    public static <L, R> BiContainer<L, R> of(final L left, final R right) {
        return new BiContainer<>(left, right);
    }

    @Getter(AccessLevel.PUBLIC)
    private final L left;

    @Getter(AccessLevel.PUBLIC)
    private final R right;

}
