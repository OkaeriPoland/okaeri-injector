# Okaeri Injector

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-injector)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-injector)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-injector)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-injector)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Probably the most basic and simple DI possible with just ~9kB in size.

## Installation
### Maven
Add repository to the `repositories` section:
```xml
<repository>
    <id>okaeri-repo</id>
    <url>https://storehouse.okaeri.eu/repository/maven-public/</url>
</repository>
```
Add dependency to the `dependencies` section:
```xml
<dependency>
  <groupId>eu.okaeri</groupId>
  <artifactId>okaeri-injector</artifactId>
  <version>1.0.0</version>
</dependency>
```
### Gradle
Add repository to the `repositories` section:
```groovy
maven { url "https://storehouse.okaeri.eu/repository/maven-public/" }
```
Add dependency to the `maven` section:
```groovy
implementation 'eu.okaeri:okaeri-injector:1.0.0'
```

## Example

```java
public class Worker {

    @Inject
    // @Inject("byName")
    private Api api;

    @PostConstruct
    private void initialize() {
        System.out.println("running api call");
        this.api.test();
    }
}
```

```java
public final class TestInjector {

    public static void main(String[] args) {

        Injector injector = OkaeriInjector.create()
                // .registerInjectable("byName", new Api())
                .registerInjectable(new Api());

        Worker worker = injector.createInstance(Worker.class);
        System.out.println(worker);
    }
}
```

```java
public class Api {

    public void test() {
        System.out.println("Hello World!");
    }
}
```