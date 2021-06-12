package eu.okaeri.injectortest.element;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Api {

    private final String name;

    public void test() {
        System.out.println("Hello World!");
        System.out.println("-=-=-=-=-=-=-");
    }
}
