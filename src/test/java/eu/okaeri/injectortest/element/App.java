package eu.okaeri.injectortest.element;

import eu.okaeri.injector.annotation.Inject;
import lombok.Data;

@Data
public class App {

    private final String name;

    @Inject
    public App(String name) {
        this.name = name;
    }
}