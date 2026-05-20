# Java Toolchains

EBSL uses explicit Gradle Java toolchains per module family. Docker does not decide which Java version compiles or runs Minecraft modules.

## Current Target

The current supported target is:

- Minecraft `1.21.11`
- Java `21`

Shared modules, `modules/app`, and `modules/mc/1-21-11/*` compile with Java 21 today. This keeps the current mod target aligned with its Minecraft runtime.

## Minecraft 1.26.1+ And Java 25

When a Minecraft target requires Java 25, add it as a separate target module instead of upgrading the whole repository in one move.

Expected shape:

```text
modules/mc/1-26-1/common
modules/mc/1-26-1/fabric
```

That target should set this in `modules/mc/1-26-1/gradle.properties`:

```properties
minecraft_java_version=25
```

Shared modules should stay on the lowest Java version needed by all active Minecraft targets unless a deliberate architecture decision says otherwise. That keeps common pathfinding, navigation, rendering contracts, and simulator code reusable across targets.

## Rule Of Thumb

- Common modules: lowest shared Java baseline.
- Minecraft adapter modules: Java version required by that Minecraft version.
- Minecraft module paths: `:mc:<version>:common`, `:mc:<version>:fabric`, and future loaders with the same shape.
- Fabric run tasks: host JVM/toolchain, not Docker.
- SonarQube: Docker service, independent from Minecraft Java runtime.

If Java 25 APIs are needed in common code, split the API behind a version-specific adapter first. Do not leak Java 25-only APIs into common modules while Java 21 targets still exist.
