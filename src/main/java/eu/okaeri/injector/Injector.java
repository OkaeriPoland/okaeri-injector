package eu.okaeri.injector;

import eu.okaeri.injector.exception.InjectorException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Injector {

    <T> Injector registerInjectable(String name, T object, Class<T> type) throws InjectorException;

    List<Injectable> all();

    void removeIf(Predicate<Injectable> predicate);

    <T> List<Injectable<T>> allOf(Class<T> type);

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(T object) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object");
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable(null, object, objectClazz);
    }

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(String name, T object) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object: " + name);
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable(name, object, objectClazz);
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<? extends Injectable<T>> getInjectable(String name, Class<T> type) {

        Injectable<T> value = this.getExact(name, type).orElse(null);

        // no value and not searching for type only
        if ((value == null) && !"".equals(name)) {
            // search for type only
            return this.getExact("", type);
        }

        // just return
        return Optional.ofNullable(value);
    }

    <T> Optional<? extends Injectable<T>> getExact(String name, Class<T> type);

    <T> T createInstance(Class<T> clazz) throws InjectorException;
}
