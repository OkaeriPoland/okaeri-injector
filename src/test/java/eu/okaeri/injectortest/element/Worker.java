package eu.okaeri.injectortest.element;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;

@Getter
public class Worker {

    public int counter = 0;

    @Inject
    private Api api;

    @PostConstruct // method order or last (default MAX_VALUE)
    private void initialize() {
        Assertions.assertEquals(2, this.counter++);
        System.out.println("running api call");
        this.api.test();
    }

    @PostConstruct(order = 1)
    private void initializeAfterFirst() {
        Assertions.assertEquals(1, this.counter++);
        System.out.println("hi again");
    }

    @PostConstruct(order = 0)
    private void initializeFirst() {
        Assertions.assertEquals(0, this.counter++);
        System.out.println("hi");
    }
}
