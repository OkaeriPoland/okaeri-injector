package eu.okaeri.injector;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.injector.exception.InjectorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OkaeriInjector implements Injector {

    public static OkaeriInjector create() {
        return new OkaeriInjector();
    }

    private List<Injectable> injectables = new ArrayList<>();

    @Override
    public <T> Injector registerInjectable(String name, T object, Class<T> type) throws InjectorException {
        if (object == null) throw new InjectorException("cannot register null object");
        this.injectables.add(Injectable.of(name, object, type));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<? extends Injectable<T>> getInjectable(String name, Class<T> type) {

        Injectable<T> valueByNameAndType = this.injectables.stream()
                .filter(injectable -> name.equals(injectable.getName()))
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .findAny()
                .orElse(null);

        if (valueByNameAndType != null) {
            return Optional.of(valueByNameAndType);
        }

        Injectable<T> valueByType = this.injectables.stream()
                .filter(injectable -> type.isAssignableFrom(injectable.getType()))
                .findAny()
                .orElse(null);

        return Optional.ofNullable(valueByType);
    }

    @Override
    public <T> T createInstance(Class<T> clazz) throws InjectorException {

        // create instance
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new InjectorException("cannot initialize new instance of " + clazz, exception);
        }

        // inject fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }
            String name = inject.value();
            if (name.isEmpty()) {
                name = field.getName();
            }
            Optional<? extends Injectable<?>> injectableOptional = this.getInjectable(name, field.getType());
            if (!injectableOptional.isPresent()) {
                throw new InjectorException("cannot resolve " + inject + " " + field.getType() + " [" + field.getName() + "] in instace of " + clazz);
            }
            Injectable<?> injectable = injectableOptional.get();
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                field.set(instance, injectable.getObject());
            } catch (IllegalAccessException exception) {
                throw new InjectorException("cannot inject " + injectable + " to instace of " + clazz, exception);
            }
            field.setAccessible(accessible);
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
                throw new InjectorException("failed to invoke @PostConstruct for instace of " + clazz, exception);
            }
        }

        return instance;
    }
}
