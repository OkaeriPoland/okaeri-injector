package eu.okaeri.injectortest.element;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import lombok.Getter;

@Getter
public class Worker {

    @Inject
    private Api api;

    @PostConstruct
    private void initialize() {
        System.out.println("running api call");
        this.api.test();
    }
}
