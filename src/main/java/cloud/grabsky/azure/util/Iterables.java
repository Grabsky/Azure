package cloud.grabsky.azure.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Internal
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Iterables {

    public static <T> List<T> reversed(final List<T> original) {
        final List<T> reversed = new ArrayList<>(original);
        Collections.reverse(reversed);
        return reversed;
    }

}
