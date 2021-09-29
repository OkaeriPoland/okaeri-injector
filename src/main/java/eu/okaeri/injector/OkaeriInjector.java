package eu.okaeri.injector;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.injector.exception.InjectorException;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OkaeriInjector implements Injector {

    private final List<Injectable> injectables;
    private final boolean unsafe;

    public static OkaeriInjector create() {
        return create(false);
    }

    public static OkaeriInjector create(boolean unsafe) {
        return create(new CopyOnWriteArrayList<>(), unsafe);
    }

    public static OkaeriInjector create(List<Injectable> injectables, boolean unsafe) {
        return new OkaeriInjector(injectables, unsafe);
    }

    @Override
    public List<Injectable> all() {
        return Collections.unmodifiableList(this.injectables);
    }

    @Override
    public void removeIf(@NonNull Predicate<Injectable> filter) {
        this.injectables.removeIf(filter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Injectable<T>> allOf(@NonNull Class<T> type) {
        List<Injectable<T>> data = new ArrayList<>();
        List found = this.injectables.stream()
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .collect(Collectors.toList());
        data.addAll(found);
        return Collections.unmodifiableList(data);
    }

    @Override
    public <T> Injector registerInjectable(@NonNull String name, @NonNull T object, @NonNull Class<T> type) throws InjectorException {
        this.injectables.add(0, Injectable.of(name, object, type));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<? extends Injectable<T>> getExact(@NonNull String name, @NonNull Class<T> type) {
        Injectable<T> value = this.injectables.stream()
                .filter(injectable -> name.isEmpty() || name.equals(injectable.getName()))
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .findAny()
                .orElse(null);
        return Optional.ofNullable(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(@NonNull Class<T> clazz) throws InjectorException {

        // create instance
        T instance = tryCreateInstance(clazz, this.unsafe);

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
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getAnnotation(PostConstruct.class) != null)
                .sorted(Comparator.comparingInt(method -> method.getAnnotation(PostConstruct.class).order()))
                .forEach(method -> {
                    try {
                        Object result = this.invoke(instance, method);
                        if (result != null) {
                            this.registerInjectable(method.getName(), result);
                        }
                    } catch (InjectorException exception) {
                        throw new InjectorException("failed to invoke @PostConstruct for instance of " + clazz, exception);
                    }
                });

        return instance;
    }

    @Override
    public Object invoke(@NonNull Constructor constructor) throws InjectorException {

        constructor.setAccessible(true);
        Object[] call = this.fillParameters(constructor.getParameters(), true);

        try {
            return constructor.newInstance(call);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new InjectorException("error invoking " + constructor, exception);
        }
    }

    @Override
    public Object invoke(@NonNull Object object, @NonNull Method method) throws InjectorException {

        method.setAccessible(true);
        Object[] call = this.fillParameters(method.getParameters(), true);

        try {
            return method.invoke(object, call);
        } catch (Exception exception) {
            throw new InjectorException("error invoking " + method, exception);
        }
    }

    @Override
    public Object[] fillParameters(@NonNull Parameter[] parameters, boolean force) throws InjectorException {

        Object[] call = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            String name = (param.getAnnotation(Inject.class) != null) ? param.getAnnotation(Inject.class).value() : "";

            Optional<? extends Injectable<?>> injectable = this.getInjectable(name, paramType);
            if (!injectable.isPresent()) {
                if (force) {
                    throw new InjectorException("cannot fill parameters, no injectable of type " + paramType + " [" + name + "] found");
                } else {
                    continue;
                }
            }

            call[i] = paramType.cast(injectable.get().getObject());
        }

        return call;
    }

    private static Object allocateInstance(@NonNull Class<?> clazz) throws Exception {
        Class<?> unsafeClazz = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClazz.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object unsafeInstance = theUnsafeField.get(null);
        Method allocateInstance = unsafeClazz.getDeclaredMethod("allocateInstance", Class.class);
        return allocateInstance.invoke(unsafeInstance, clazz);
    }

    @SuppressWarnings("unchecked")
    private static <T> T tryCreateInstance(@NonNull Class<T> clazz, boolean unsafe) {
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException exception0) {
            if (!unsafe) {
                throw new InjectorException("cannot initialize new instance of " + clazz, exception0);
            }
            try {
                instance = (T) allocateInstance(clazz);
            } catch (Exception exception) {
                throw new InjectorException("cannot (unsafe) initialize new instance of " + clazz, exception);
            }
        }
        return instance;
    }
}
