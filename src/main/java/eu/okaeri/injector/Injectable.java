package eu.okaeri.injector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Injectable<T> {

    public static <T> Injectable<T> of(@NonNull String name, @NonNull T object, @NonNull Class<T> type) {
        return new Injectable<>(name, object, type);
    }

    private final String name;
    private final T object;
    private final Class<T> type;
}