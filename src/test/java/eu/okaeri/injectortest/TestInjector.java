package eu.okaeri.injectortest;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;

public final class TestInjector {

    public static void main(String[] args) {

        Injector injector = OkaeriInjector.create()
                .registerInjectable(new Api());

        Worker worker = injector.createInstance(Worker.class);
        System.out.println(worker);
    }
}

