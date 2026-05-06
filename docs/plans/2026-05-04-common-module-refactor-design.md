# Common Module Refactor Design

**Date:** 2026-05-04  
**Scope:** Full architectural refactor — zero MC imports in backend, multi-version support

---

## Current Implementation Status

As of 2026-05-05, the module split is enforced in code:

- `src/common` owns backend logic and compiles with no `net.minecraft`, `net.fabricmc`, or `com.mojang` imports.
- `src/minecraft-1-21-11/common` owns the version-scoped modloader-common layer: MC-facing bootstrap, shared loader-neutral services, chat/ImGui/physics adapters, common mixins, vanilla adapters, and shared MC resources such as lang, icon, and shaders.
- `src/minecraft-1-21-11/fabric` has been reduced to Fabric metadata plus the Fabric entrypoint that feeds loader lifecycle events into the common bootstrap.
- The Fabric entrypoint is now `fr.riege.ebsl.fabric.FabricEbslMod`.
- The Fabric entrypoint calls `fr.riege.ebsl.loader.ModloaderCommonBootstrap` from `minecraft-1-21-11/common`; common mixins are declared by `ebsl.loader.mixins.json`.
- The old Fabric backend packages (`pathfinding`, `general`, `terminal`, `ui`, `api`, etc.) were removed from the Fabric source tree.
- Navigation now goes through `CommonNavigationBackend`, backed by the common A* pathfinder, `LayerNavigationPointProvider`, and `LayerPathProcessor`.
- Validation command: `./gradlew buildMod121Fabric`.

Known follow-up: the common backend currently owns path calculation and service state; richer live movement execution/visualization can continue to grow inside `src/common` against `IPhysicsLayer` and `IRenderLayer`, without moving logic back into version or loader modules.

---

## Goal

Extract all mod logic into a `common` module with zero Minecraft dependencies. Each MC version has its own parent module (e.g. `minecraft-1-21-11`) containing one submodule per mod loader (e.g. `fabric`, `forge`). Adding support for a new MC version or loader means only implementing 8 interfaces — all logic stays in `common`.

---

## Gradle Structure

Three-level mono-repo (inspired by Architectury), with all Gradle modules physically
grouped under `src/`:

```
ebsl/
├── settings.gradle
├── build.gradle                           # custom tasks only
└── src/
    ├── common/                            # pure Java 21, zero MC
    │   ├── build.gradle
    │   └── src/main/java/fr/riege/ebsl/
    │       ├── core/EbslCore.java         # composition root
    │       ├── platform/EbslPlatform.java # record + Builder
    │       ├── layer/                     # 8 layer interfaces
    │       ├── pathfinding/
    │       ├── terminal/
    │       ├── event/
    │       ├── settings/
    │       ├── analytics/
    │       ├── ui/                        # ImGui panels
    │       ├── general/
    │       └── registry/
    └── minecraft-1-21-11/
        ├── gradle.properties              # minecraft_version=1.21.11
        ├── common/                        # version-scoped common for all modloaders
        │   ├── build.gradle               # loom (MC jars only, no loader API)
        │   └── src/main/java/fr/riege/ebsl/
        │       ├── loader/                # bootstrap, shared services, common mixins
        │       └── mc/                    # vanilla MC adapters
        └── fabric/                        # Fabric-specific only
            ├── build.gradle               # loom + fabric-api + fabric-gui-imgui
            └── src/main/java/fr/riege/ebsl/fabric/
                └── FabricEbslMod.java     # entry point, calls ModloaderCommonBootstrap
```

**Layer split between common modules and Fabric:**

| Layer | Where | Why |
|---|---|---|
| `IWorldLayer` | `minecraft-1-21-11/common` | vanilla MC API, same across loaders |
| `IPlayerLayer` | `minecraft-1-21-11/common` | vanilla MC API |
| `IRenderLayer` | `minecraft-1-21-11/common` | MC render pipeline, loader-agnostic |
| `IStorageLayer` | `minecraft-1-21-11/common` | java.nio + config path injection |
| `IPhysicsLayer` | `minecraft-1-21-11/common` | vanilla key mapping injection, shared across loaders until a loader-specific hook is truly needed |
| `IEventBus` | `minecraft-1-21-11/common` | common dispatch; loader entrypoint feeds ticks/events |
| `ICommandLayer` | `minecraft-1-21-11/fabric` for now | Fabric client-command registration is a loader API hook; command logic still stays in `src/common` |
| `IImGuiLayer` | `minecraft-1-21-11/common` | MC window/viewport adapter shared by loaders |

In `settings.gradle`:
```groovy
include('common')
include('minecraft-1-21-11:common')
include('minecraft-1-21-11:fabric')
// future: include('minecraft-1-21-11:forge')
// future: include('minecraft-1-22:common')
// future: include('minecraft-1-22:fabric')

project(':common').projectDir = file('src/common')
project(':minecraft-1-21-11').projectDir = file('src/minecraft-1-21-11')
project(':minecraft-1-21-11:common').projectDir = file('src/minecraft-1-21-11/common')
project(':minecraft-1-21-11:fabric').projectDir = file('src/minecraft-1-21-11/fabric')
```

### build.gradle — common

```groovy
plugins { id 'java-library' }

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories { mavenCentral() }

dependencies {
    compileOnly 'io.github.spair:imgui-java-binding:1.90.0'
    testImplementation 'io.github.spair:imgui-java-binding:1.90.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### build.gradle — minecraft-1-21-11/common

```groovy
plugins {
    id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
}

dependencies {
    implementation project(':common')
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings loom.officialMojangMappings()
    // Loom requires fabric-loader for MC jar setup — not used in code
    modCompileOnly "net.fabricmc:fabric-loader:${loader_version}"
}
```

### build.gradle — minecraft-1-21-11/fabric

```groovy
plugins {
    id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
}

dependencies {
    implementation project(':common')
    implementation project(':minecraft-1-21-11:common')
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_api_version}"
    modImplementation "cn.enaium:fabric-gui-imgui:${fabric_gui_imgui_version}"
    include "cn.enaium:fabric-gui-imgui:${fabric_gui_imgui_version}"
}
```

### Custom Gradle Tasks

```groovy
// root build.gradle
tasks.register('runClient121Fabric') {
    group = 'ebsl'
    description = 'Run Minecraft 1.21.11 (Fabric) client'
    dependsOn ':minecraft-1-21-11:fabric:runClient'
}

tasks.register('buildMod121Fabric') {
    group = 'ebsl'
    description = 'Build Minecraft 1.21.11 (Fabric) jar'
    dependsOn ':minecraft-1-21-11:fabric:build'
}
// future: runClient121Forge, runClient122Fabric, etc.
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

### FabricMod (minecraft-1-21-11/fabric — entry point)

```java
public class FabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Minecraft client = Minecraft.getInstance();
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("ebsl");

        EbslPlatform platform = EbslPlatform.builder()
            // from minecraft-1-21-11/common — loader-agnostic MC implementations
            .world(new McWorldLayer(client))
            .player(new McPlayerLayer(client))
            .render(new McRenderLayer())
            .storage(new McStorageLayer(configDir))
            // from minecraft-1-21-11/common — loader-agnostic MC implementations
            .physics(new MinecraftPhysicsLayer(client))
            .events(new ModloaderEventBus())
            .commands(new FabricCommandLayer())
            .imgui(new MinecraftImGuiLayer(client))
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
- Convert root to multi-project: `common`, `minecraft-1-21-11:common`, `minecraft-1-21-11:fabric`
- Move all existing `src/` into `minecraft-1-21-11/fabric/src/` unchanged
- `common/` and `minecraft-1-21-11/common/` are empty — mod still compiles and runs

### Phase 2 — Layer interfaces + stubs
- Define all 8 interfaces + value types in `common`
- Create vanilla/shared MC layers in `minecraft-1-21-11/common`
- Keep `minecraft-1-21-11/fabric` to Fabric entrypoint and loader metadata unless a Fabric API hook is required
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
Fill in version-common MC layer implementations one by one, migrate subsystems as their layers are ready:

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

To support e.g. MC 1.22 with Fabric:
1. Create `minecraft-1-22/gradle.properties` with `minecraft_version=1.22`
2. Copy `minecraft-1-21-11/common/` → `minecraft-1-22/common/`
3. Copy `minecraft-1-21-11/fabric/` → `minecraft-1-22/fabric/`
4. Fix compilation errors in version-common MC adapter classes (MC API changes only)
5. Add to `settings.gradle`: `include('minecraft-1-22:common')`, `include('minecraft-1-22:fabric')`
6. Add `runClient122Fabric` / `buildMod122Fabric` tasks to root `build.gradle`
7. Zero changes to `common/`

## Adding a New Loader (e.g. Forge on 1.21.11)

1. Create `minecraft-1-21-11/forge/` with a ForgeGradle build
2. Implement only the genuinely loader-specific entrypoint/hooks against the Forge API.
3. Reuse `minecraft-1-21-11:common` for loader-agnostic MC implementations, common mixins, and shared MC resources
4. Add `include('minecraft-1-21-11:forge')` to `settings.gradle`
5. Add `runClient121Forge` task to root
6. Zero changes to `common/` or `minecraft-1-21-11/common/`
