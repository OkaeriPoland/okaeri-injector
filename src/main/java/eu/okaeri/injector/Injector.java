package eu.okaeri.injector;

import eu.okaeri.injector.exception.InjectorException;

import java.util.Optional;

public interface Injector {

    <T> Injector registerInjectable(String name, T object, Class<T> type) throws InjectorException;

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(T object) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object");
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable(null, object, objectClazz);
    }

    @SuppressWarnings("unchecked")
    default <T> Injector registerInjectable(String name, T object) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object");
        Class<T> objectClazz = (Class<T>) object.getClass();
        return this.registerInjectable(name, object, objectClazz);
    }

    <T> Optional<? extends Injectable<T>> getInjectable(String name, Class<T> type);

    <T> T createInstance(Class<T> clazz) throws InjectorException;
}
