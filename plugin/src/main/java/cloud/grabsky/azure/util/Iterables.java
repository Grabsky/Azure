package cloud.grabsky.azure.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Iterables {

    public static <T> List<T> reversed(final Collection<T> original) {
        // Creating a copy of provided collection.
        final List<T> reversed = new ArrayList<>(original);
        // Reversing using Collections#reverse utility.
        Collections.reverse(reversed);
        // Returning the reversed list.
        return reversed;
    }

}
