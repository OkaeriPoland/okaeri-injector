package eu.okaeri.injectortest;

import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injectortest.element.Api;
import eu.okaeri.injectortest.element.Worker;
import eu.okaeri.injectortest.element.Worker2;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestInjector {

    @Test
    public void test_simple_injector() {

        Api api = new Api("test-api");
        Injector injector = OkaeriInjector.create()
                .registerInjectable(api);

        Worker worker = injector.createInstance(Worker.class);
        assertEquals(api, worker.getApi());
        assertEquals(3, worker.getCounter());
    }

    @Test
    public void test_double_type_injector() {

        Api api = new Api("test-api1");
        Api api2 = new Api("test-api2");

        Injector injector = OkaeriInjector.create()
                .registerInjectable("api", api)
                .registerInjectable("api2", api2);

        Worker worker = injector.createInstance(Worker.class);
        assertEquals(api, worker.getApi());
        assertEquals(3, worker.getCounter());

        Worker2 worker2 = injector.createInstance(Worker2.class);
        assertEquals(api2, worker2.getApi());
    }

    @Test
    public void test_double_type_injector_2() {

        Api api = new Api("api-abc");
        Api api2 = new Api("api");

        Injector injector = OkaeriInjector.create()
                .registerInjectable("api-abc", api)
                .registerInjectable("api", api2);

        Worker worker = injector.createInstance(Worker.class);
        assertEquals(api2, worker.getApi());
        assertEquals(3, worker.getCounter());
    }

    @Test
    public void test_always_provide_last() {

        String string1 = "some value";
        String string2 = "new value";

        Injector injector = OkaeriInjector.create()
                .registerInjectable("test1", string1)
                .registerInjectable("test1", string2);

        Optional<? extends Injectable<String>> injectable = injector.getExact("test1", String.class);
        assertEquals(true, injectable.isPresent());
        assertEquals(string2, injectable.get().getObject());
    }

    @Test
    public void test_exclusive_register() {

        String string1 = "some value";
        String string2 = "new value";

        Injector injector = OkaeriInjector.create()
                .registerInjectable("test1", string1)
                .registerInjectable("test1", string2)
                .registerExclusive("test1", string1);

        Optional<? extends Injectable<String>> injectable = injector.getExact("test1", String.class);
        assertEquals(true, injectable.isPresent());
        assertEquals(string1, injectable.get().getObject());
        assertEquals(1, injector.allOf(String.class).size());
    }
}

