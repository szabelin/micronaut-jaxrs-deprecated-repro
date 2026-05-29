# micronaut-jaxrs-deprecated-repro

Minimal reproducer for a runtime crash in `micronaut-jaxrs-server` when a
JAX-RS controller class is annotated with `@Deprecated`.

Filed at: https://github.com/micronaut-projects/micronaut-jaxrs/issues/_TODO_

## Resolved versions

- micronaut-jaxrs-server: **4.10.0** (latest 4.x)
- micronaut-core: **4.10.23** (latest 4.x)
- micronaut-serde-jackson: 2.16.2
- jakarta.ws.rs-api: 3.1.0
- Java: 21
- Gradle: 8.14.3
- Micronaut Gradle plugin: 4.6.2

## How to reproduce

```bash
./gradlew run
# in another shell:
curl -i http://localhost:18080/api/v1/things
```

The first request crashes with:

```
java.lang.IllegalArgumentException: io.micronaut.core.annotation.AnnotationValueProvider
    referenced from a method is not visible from class loader: null
    at java.base/java.lang.reflect.Proxy$ProxyBuilder.ensureVisible(Proxy.java:881)
    ...
    at io.micronaut.inject.annotation.AnnotationMetadataSupport.lambda$getProxyClass$1(AnnotationMetadataSupport.java:474)
    ...
    at io.micronaut.jaxrs.common.JaxRsContainerMessageBodyHandlerRegistry.findJaxRsBodyWriter(...)
```

The full captured trace is in [`STACKTRACE.txt`](./STACKTRACE.txt).

## Toggle behavior

| Controller                                                | `@Deprecated`? | Result                  |
| --------------------------------------------------------- | -------------- | ----------------------- |
| `ThingsController` (JAX-RS `@Path`)                       | yes            | crash on first request  |
| `ThingsController` (JAX-RS `@Path`)                       | no             | `200 OK` JSON           |
| `NativeThingsController` (Micronaut native `@Controller`) | yes            | `200 OK` JSON           |

The native-controller row proves this is **JAX-RS-specific**: the failing
code path goes through `JaxRsContainerMessageBodyHandlerRegistry.findJaxRsBodyWriter`,
which calls `type.getAnnotationMetadata().synthesizeAll()` over every
annotation including bootstrap-loaded JDK ones like `@Deprecated`.

## Root cause (one-paragraph version)

`AnnotationMetadataSupport.getProxyClass` builds a JDK dynamic proxy
implementing both the source annotation and Micronaut's
`AnnotationValueProvider`, defining it with the *annotation's* classloader.
`java.lang.Deprecated` is loaded by the bootstrap classloader (`null`),
which cannot see Micronaut classes, so `Proxy.ensureVisible` rejects
`AnnotationValueProvider` and the request fails. Any bootstrap-loaded
annotation reaching the JAX-RS body-writer path would trigger the same
crash; `@Deprecated` is the most likely to hit users in the wild.
