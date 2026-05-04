# Common Module Refactor Design

**Date:** 2026-05-04  
**Scope:** Full architectural refactor — zero MC imports in backend, multi-version support

---

## Goal

Extract all mod logic into a `common` module with zero Minecraft dependencies. Each MC version (e.g. `fabric-1-21-11`) implements the layer interfaces and wires everything together. Adding support for a new MC version means only implementing 8 interfaces — all logic stays in `common`.

---

## Gradle Structure

Mono-repo, two Gradle subprojects:

```
ebsl/
├── settings.gradle              # includes common, fabric-1-21-11
├── common/
│   ├── build.gradle             # java-library, Java 21, imgui-java-binding
│   └── src/main/java/fr/riege/ebsl/
│       ├── core/
│       │   └── EbslCore.java    # composition root
│       ├── platform/
│       │   ├── EbslPlatform.java  # record + Builder
│       │   └── layer/           # 8 layer interfaces
│       ├── pathfinding/
│       ├── terminal/
│       ├── event/
│       ├── settings/
│       ├── analytics/
│       ├── ui/                  # ImGui panels
│       ├── general/
│       └── registry/
└── fabric-1-21-11/
    ├── build.gradle             # fabric loom, depends on common
    └── src/main/java/fr/riege/ebsl/fabric/
        ├── FabricMod.java       # entry point, builds EbslPlatform
        ├── layer/               # Fabric*Layer implementations
        ├── mixin/               # all mixins stay here
        └── render/              # MC render pipeline bootstrap
```

### build.gradle — common

```groovy
plugins {
    id 'java-library'
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation 'io.github.spair:imgui-java-binding:1.90.0'
    implementation 'io.github.spair:imgui-java-natives-windows:1.90.0'
}
```

### build.gradle — fabric-1-21-11

```groovy
plugins {
    id 'fabric-loom'
}

dependencies {
    implementation project(':common')
    minecraft "com.mojang:minecraft:1.21.11"
    modImplementation "net.fabricmc:fabric-loader:${loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_version}"
    modImplementation "fr.ycx:fabric-gui-imgui:1.21.11-1.0.7+imgui.1.90.0"
}
```

### Custom Gradle Tasks

```groovy
// root build.gradle
tasks.register('runClient121') {
    group = 'ebsl'
    description = 'Run Minecraft 1.21.11 client'
    dependsOn ':fabric-1-21-11:runClient'
}

tasks.register('buildMod121') {
    group = 'ebsl'
    description = 'Build fabric-1-21-11 jar'
    dependsOn ':fabric-1-21-11:build'
}
```

---

## Layer Interfaces

All defined in `common` under `fr.riege.ebsl.platform.layer`. Zero MC imports.

### IWorldLayer
```java
public interface IWorldLayer {
    BlockId getBlock(int x, int y, int z);
    boolean isAir(int x, int y, int z);
    boolean isSolid(int x, int y, int z);
    boolean isWater(int x, int y, int z);
    boolean isLoaded(int x, int y, int z);
    int getTopSolidY(int x, int z);
    double getBlockFriction(int x, int y, int z);
}
```

### IPlayerLayer
```java
public interface IPlayerLayer {
    Vec3d position();
    Vec2f rotation();        // yaw, pitch
    Vec3d velocity();
    boolean isOnGround();
    boolean isInWater();
    boolean isAlive();
    int dimension();
}
```

### IPhysicsLayer
```java
public interface IPhysicsLayer {
    void setForward(float value);   // -1 to 1
    void setSideways(float value);  // -1 to 1
    void setJump(boolean value);
    void setSprint(boolean value);
    void setSneak(boolean value);
    void setYaw(float yaw);
    void setPitch(float pitch);
}
```

### IEventBus
```java
public interface IEventBus {
    void onTick(Consumer<TickEvent> handler);
    void onRenderWorld(Consumer<RenderWorldEvent> handler);
    void onRenderHud(Consumer<RenderHudEvent> handler);
    void onKeyPress(Consumer<KeyPressEvent> handler);
    void onMouseButton(Consumer<MouseButtonEvent> handler);
    void onCharTyped(Consumer<CharTypedEvent> handler);
}
```

Events are value types in `common` with no MC data:
```java
public record TickEvent(long tick) {}
public record RenderWorldEvent(float[] viewMatrix, float[] projMatrix, float tickDelta) {}
public record RenderHudEvent(int screenWidth, int screenHeight, float tickDelta) {}
public record KeyPressEvent(int keyCode, int action, int modifiers) {}
public record MouseButtonEvent(int button, int action) {}
public record CharTypedEvent(char character) {}
```

### IRenderLayer
```java
public interface IRenderLayer {
    void drawLine(Vec3d from, Vec3d to, int colorARGB, float width);
    void drawBox(Vec3d min, Vec3d max, int colorARGB, float width);
    void drawFilledBox(Vec3d min, Vec3d max, int colorARGB);
    void drawSphere(Vec3d center, float radius, int colorARGB);
    void submit();   // flush draw calls for current frame
}
```

### ICommandLayer
```java
public interface ICommandLayer {
    void register(String name, String description, CommandHandler handler);
    void print(String message);
    void printError(String message);
    void printSuccess(String message);
    List<String> getSuggestions(String input);
}
```

### IStorageLayer
```java
public interface IStorageLayer {
    void save(String key, String json);
    Optional<String> load(String key);
    Path getStorageDir();
}
```

### IImGuiLayer
```java
public interface IImGuiLayer {
    void registerFrame(Runnable drawPanels);
    int getViewportWidth();
    int getViewportHeight();
}
```

---

## Platform Assembly

### EbslPlatform (common)

```java
public record EbslPlatform(
    IWorldLayer world,
    IPlayerLayer player,
    IPhysicsLayer physics,
    IEventBus events,
    IRenderLayer render,
    ICommandLayer commands,
    IStorageLayer storage,
    IImGuiLayer imgui
) {
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        // one setter per layer, returns Builder
        public EbslPlatform build() { /* validate all non-null */ }
    }
}
```

### EbslCore (common — composition root)

```java
public class EbslCore {
    public EbslCore(EbslPlatform platform) {
        var pathfinding = new PathfindingManager(platform.world(), platform.player(), platform.physics());
        var terminal    = new TerminalManager(platform.commands());
        var renderer    = new VisualizationManager(platform.render());
        var ui          = new EbslImGuiManager(platform.imgui(), pathfinding, terminal);
        var analytics   = new AnalyticsManager(platform.events());

        platform.events().onTick(e -> pathfinding.tick());
        platform.events().onRenderWorld(e -> renderer.renderWorld(e));
        platform.imgui().registerFrame(ui::draw);
    }
}
```

### FabricMod (fabric-1-21-11 — entry point)

```java
public class FabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();

        EbslPlatform platform = EbslPlatform.builder()
            .world(new FabricWorldLayer(client))
            .player(new FabricPlayerLayer(client))
            .physics(new FabricPhysicsLayer(client))
            .events(new FabricEventBus())
            .render(new FabricRenderLayer())
            .commands(new FabricCommandLayer())
            .storage(new FabricStorageLayer(FabricLoader.getInstance().getConfigDir()))
            .imgui(new FabricImGuiLayer(client))
            .build();

        new EbslCore(platform);
    }
}
```

---

## Value Types in common

No MC imports. Defined once, used everywhere.

```java
public record Vec3d(double x, double y, double z) {}
public record Vec3i(int x, int y, int z) {}
public record Vec2f(float x, float y) {}
public record Vec2i(int x, int y) {}
public record BlockId(String namespace, String path) {
    public static BlockId of(String id) { /* parse "minecraft:stone" */ }
}
```

---

## Gradle Safety Check

Fail the build if any `net.minecraft` or `net.fabricmc` import appears in `common`:

```groovy
// common/build.gradle
tasks.named('compileJava') {
    doLast {
        def sources = fileTree('src/main/java').filter { it.name.endsWith('.java') }
        sources.each { file ->
            def content = file.text
            if (content.contains('net.minecraft.') || content.contains('net.fabricmc.')) {
                throw new GradleException("MC import in common: ${file.path}")
            }
        }
    }
}
```

---

## Migration Phases

### Phase 1 — Gradle skeleton
- Convert root to multi-project (`settings.gradle` includes both subprojects)
- Move all existing `src/` into `fabric-1-21-11/src/` unchanged
- `common/` is empty — mod still compiles and runs

### Phase 2 — Layer interfaces + stubs
- Define all 8 interfaces + value types in `common`
- Create `Fabric*Layer` stub classes in `fabric-1-21-11` (throw `UnsupportedOperationException`)
- `EbslPlatform` + `EbslCore` exist but do nothing
- `FabricMod` builds the platform and creates `EbslCore`

### Phase 3 — Pure subsystems (no MC to strip)
Move to `common` in order:
1. `settings/` — pure Java framework
2. `registry/` — pure Java
3. `analytics/` — pure data
4. `event/` bus infrastructure — strip MC data from event records
5. `pathfinding/goal/` — goal types, pure logic
6. `pathfinding/pathfinder/` — A\* engine (abstracts world via IWorldLayer)
7. `pathfinding/rotation/` — angle math, pure

### Phase 4 — Layer-dependent subsystems
Fill in `Fabric*Layer` implementations one by one, migrate subsystems as their layers are ready:

| Layer ready | Subsystems migrated |
|---|---|
| `IWorldLayer` | WalkabilityChecker, movement evaluators, PathSmoother |
| `IPlayerLayer` + `IPhysicsLayer` | movement executors, PathExecutor, rotation controller |
| `IEventBus` | tick loop wiring, input handling |
| `IRenderLayer` | PathVisualizer, VisualizationManager, PathClosedSetVisualizer |
| `IImGuiLayer` | all ImGui panels, EbslImGuiService → EbslImGuiManager |
| `ICommandLayer` | terminal, all commands, GoalCommandSupport |
| `IStorageLayer` | PathfinderSettingsStore, BotModuleSettingsStore |

### Phase 5 — Enforcement + cleanup
- Enable the Gradle import check on `common`
- Delete any dead code in `fabric-1-21-11`
- Verify `fabric-1-21-11/src` has zero pathfinding/terminal logic

---

## Adding a New MC Version

To support e.g. `fabric-1-22`:
1. Copy `fabric-1-21-11/` → `fabric-1-22/`
2. Update `minecraft_version`, `loader_version`, `fabric_version` in its `gradle.properties`
3. Fix compilation errors in the 8 `Fabric*Layer` classes (API changes between MC versions)
4. Add `runClient122` / `buildMod122` tasks to root
5. Zero changes to `common/`
