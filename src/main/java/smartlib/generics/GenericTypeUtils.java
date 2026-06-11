package smartlib.generics;

import java.util.Optional;

public final class GenericTypeUtils {
    private GenericTypeUtils() {
    }

    public static <T> Optional<T> safeCast(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return Optional.of(clazz.cast(obj));
        }
        return Optional.empty();
    }
}
