package eu.okaeri.injectortest;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injectortest.element.Api;
import eu.okaeri.injectortest.element.Worker;
import eu.okaeri.injectortest.element.Worker2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestInjector {

    @Test
    public void test_simple_injector() {

        Api api = new Api("test-api");
        Injector injector = OkaeriInjector.create()
                .registerInjectable(api);

        Worker worker = injector.createInstance(Worker.class);
        assertEquals(api, worker.getApi());
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

        Worker2 worker2 = injector.createInstance(Worker2.class);
        assertEquals(api2, worker2.getApi());
    }
}

