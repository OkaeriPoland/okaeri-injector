package eu.okaeri.injectortest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;

public class Worker {

    @Inject
    private Api api;

    @PostConstruct
    private void initialize() {
        System.out.println("running api call");
        this.api.test();
    }
}
