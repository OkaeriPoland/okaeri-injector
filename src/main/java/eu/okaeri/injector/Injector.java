package eu.okaeri.injector;

import eu.okaeri.injector.exception.InjectorException;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Injector {

    <T> Injector registerInjectable(@NonNull String name, @NonNull T object, @NonNull Class<T> type) throws InjectorException;

    List<Injectable> all();

    Stream<Injectable> stream();

    void removeIf(@NonNull Predicate<Injectable> predicate);

    <T> List<Injectable<T>> allOf(@NonNull Class<T> type);

    <T> Stream<Injectable<T>> streamInjectableOf(@NonNull Class<T> type);

    <T> Stream<T> streamOf(@NonNull Class<T> type);

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

    /**
     * Registers injectable.
     * <p>
     * Eliminates all other injectables with the same name and being same type, supertype or subtype.
     */
    @SuppressWarnings("unchecked")
    default <T> Injector registerExclusive(@NonNull String name, @NonNull T object) throws InjectorException {
        Class<T> type = (Class<T>) object.getClass();
        this.removeIf(injectable -> name.equals(injectable.getName()) && (type.isAssignableFrom(injectable.getType()) || injectable.getType().isAssignableFrom(type)));
        return this.registerInjectable(name, object, type);
    }

    /**
     * Registers injectable.
     * <p>
     * Eliminates all other injectables with the same name and being same type or subtype of {@code type}.
     * <p>
     * Warning: to remove supertypes (e.g. injectables registered with interface type) use {@link #registerExclusive(String, Object)}
     * or manually remove them with {@link #removeIf(Predicate)} before invoking this method.
     */
    default <T> Injector registerExclusive(@NonNull String name, @NonNull T object, @NonNull Class<T> type) throws InjectorException {
        this.removeIf(injectable -> name.equals(injectable.getName()) && type.isAssignableFrom(injectable.getType()));
        return this.registerInjectable(name, object, type);
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<? extends Injectable<T>> getInjectable(@NonNull String name, @NonNull Class<T> type) {

        Injectable<T> value = this.getInjectableExact(name, type).orElse(null);

        // no value and not searching for type only
        if ((value == null) && !"".equals(name)) {
            // search for type only
            return this.getInjectableExact("", type);
        }

        // just return
        return Optional.ofNullable(value);
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<T> get(@NonNull String name, @NonNull Class<T> type) {
        return this.getInjectable(name, type).map(Injectable::getObject);
    }

    default <T> T getOrThrow(@NonNull String name, @NonNull Class<T> type) {
        return this.get(name, type).orElseThrow(() -> new InjectorException("no injectable for " + name + " of type " + type));
    }

    <T> Optional<? extends Injectable<T>> getInjectableExact(@NonNull String name, @NonNull Class<T> type);

    default <T> Optional<T> getExact(@NonNull String name, @NonNull Class<T> type) {
        return this.getInjectableExact(name, type).map(Injectable::getObject);
    }

    default <T> T getExactOrThrow(@NonNull String name, @NonNull Class<T> type) {
        return this.getExact(name, type).orElseThrow(() -> new InjectorException("no exact injectable for " + name + " of type " + type));
    }

    <T> T createInstance(@NonNull Class<T> clazz) throws InjectorException;

    <T> T injectFields(@NonNull T instance) throws InjectorException;

    <T> T invokePostConstructs(@NonNull T instance) throws InjectorException;

    Object invoke(@NonNull Constructor constructor) throws InjectorException;

    Object invoke(@NonNull Object object, @NonNull Method method) throws InjectorException;

    Object[] fillParameters(@NonNull Parameter[] parameters, boolean force) throws InjectorException;
}
