package eu.okaeri.injector;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.injector.exception.InjectorException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OkaeriInjector implements Injector {

    private final boolean unsafe;

    public static OkaeriInjector create() {
        return create(false);
    }

    public static OkaeriInjector create(boolean unsafe) {
        return new OkaeriInjector(unsafe);
    }

    private List<Injectable> injectables = new ArrayList<>();

    @Override
    public List<Injectable> all() {
        return this.injectables;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Injectable<T>> allOf(Class<T> type) {
        List<Injectable<T>> data = new ArrayList<>();
        List found = this.injectables.stream()
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .collect(Collectors.toList());
        data.addAll(found);
        return Collections.unmodifiableList(data);
    }

    @Override
    public <T> Injector registerInjectable(String name, T object, Class<T> type) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object: " + name);
        this.injectables.add(Injectable.of(name, object, type));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<? extends Injectable<T>> getExact(String name, Class<T> type) {
        Injectable<T> value = this.injectables.stream()
                .filter(injectable -> name.isEmpty() || name.equals(injectable.getName()))
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .findAny()
                .orElse(null);
        return Optional.ofNullable(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> clazz) throws InjectorException {

        // create instance
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException exception0) {
            if (!this.unsafe) {
                throw new InjectorException("cannot initialize new instance of " + clazz, exception0);
            }
            try {
                instance = (T) allocateInstance(clazz);
            } catch (Exception exception) {
                throw new InjectorException("cannot (unsafe) initialize new instance of " + clazz, exception);
            }
        }

        // inject fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }

            String name = inject.value();
            Optional<? extends Injectable<?>> injectableOptional;

            if (name.isEmpty()) {
                name = field.getName();
                injectableOptional = this.getInjectable(name, field.getType());
            } else {
                injectableOptional = this.getExact(name, field.getType());
            }

            if (!injectableOptional.isPresent()) {
                throw new InjectorException("cannot resolve " + inject + " " + field.getType() + " [" + field.getName() + "] in instance of " + clazz);
            }

            Injectable<?> injectable = injectableOptional.get();
            field.setAccessible(true);

            try {
                field.set(instance, injectable.getObject());
            } catch (IllegalAccessException exception) {
                throw new InjectorException("cannot inject " + injectable + " to instance of " + clazz, exception);
            }
        }

        // dispatch post constructs
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            if (postConstruct == null) {
                continue;
            }
            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new InjectorException("failed to invoke @PostConstruct for instance of " + clazz, exception);
            }
        }

        return instance;
    }

    private static Object allocateInstance(Class<?> clazz) throws Exception {
        Class<?> unsafeClazz = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClazz.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object unsafeInstance = theUnsafeField.get(null);
        Method allocateInstance = unsafeClazz.getDeclaredMethod("allocateInstance", Class.class);
        return allocateInstance.invoke(unsafeInstance, clazz);
    }
}
