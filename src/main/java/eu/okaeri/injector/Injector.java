package eu.okaeri.injector;

import eu.okaeri.injector.exception.InjectorException;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Injector {

    <T> Injector registerInjectable(@NonNull String name, @NonNull T object, @NonNull Class<T> type) throws InjectorException;

    List<Injectable> all();

    void removeIf(@NonNull Predicate<Injectable> predicate);

    <T> List<Injectable<T>> allOf(@NonNull Class<T> type);

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(@NonNull T object) throws InjectorException {
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable("", object, objectClazz);
    }

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(@NonNull String name, @NonNull T object) throws InjectorException {
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable(name, object, objectClazz);
    }

    @SuppressWarnings("unchecked")
    default <T> Injector registerExclusive(@NonNull String name, @NonNull T object) throws InjectorException {
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerExclusive(name, object, objectClazz);
    }

    default <T> Injector registerExclusive(@NonNull String name, @NonNull T object, @NonNull Class<T> type) throws InjectorException {
        this.removeIf(injectable -> name.equals(injectable.getName()) && type.isAssignableFrom(injectable.getType()));
        return this.registerInjectable(name, object, type);
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<? extends Injectable<T>> getInjectable(@NonNull String name, @NonNull Class<T> type) {

        Injectable<T> value = this.getExact(name, type).orElse(null);

        // no value and not searching for type only
        if ((value == null) && !"".equals(name)) {
            // search for type only
            return this.getExact("", type);
        }

        // just return
        return Optional.ofNullable(value);
    }

    <T> Optional<? extends Injectable<T>> getExact(@NonNull String name, @NonNull Class<T> type);

    <T> T createInstance(@NonNull Class<T> clazz) throws InjectorException;

    <T> T injectFields(@NonNull T instance) throws InjectorException;

    <T> T invokePostConstructs(@NonNull T instance) throws InjectorException;

    Object invoke(@NonNull Constructor constructor) throws InjectorException;

    Object invoke(@NonNull Object object, @NonNull Method method) throws InjectorException;

    Object[] fillParameters(@NonNull Parameter[] parameters, boolean force) throws InjectorException;
}
