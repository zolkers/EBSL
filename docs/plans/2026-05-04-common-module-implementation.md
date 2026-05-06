# Common Module Refactor — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor the EBSL mod into a `common` module (zero MC imports, Java 21) and nested loader subprojects (e.g. `minecraft-1-21-11/fabric`) that implement 8 layer interfaces and wire everything together.

**Architecture:** Layered Platform + Composition Root. `common` defines all logic and 8 layer interfaces. Each loader subproject implements the interfaces and assembles `EbslPlatform` injected into `EbslCore`. Adding a new MC version = new `minecraft-X/fabric` subproject, implement 8 interfaces only.

**Physical layout:** Gradle project names stay short (`:common`, `:minecraft-1-21-11:common`, `:minecraft-1-21-11:fabric`), but all module directories live under `src/`: `src/common`, `src/minecraft-1-21-11/common`, and `src/minecraft-1-21-11/fabric`.

**Tech Stack:** Java 21, Gradle multi-project, Fabric Loom, imgui-java-binding, JUnit 5

**Design doc:** `docs/plans/2026-05-04-common-module-refactor-design.md`

---

## Status 2026-05-05

Implemented:

- Gradle modules live under `src/`.
- `src/common` compiles cleanly without Minecraft/Fabric/Mojang imports.
- `src/minecraft-1-21-11/common` is the version-scoped common loader layer: `ModloaderCommonBootstrap`, shared loader-neutral services/adapters, common mixins, `McWorldLayer`, `McPlayerLayer`, `McRenderLayer`, `McStorageLayer`, `MinecraftPhysicsLayer`, and shared MC resources.
- `src/minecraft-1-21-11/fabric` is physically reduced to Fabric metadata, `FabricEbslMod`, and the tiny `FabricCommandLayer` bridge needed to register client commands through Fabric API.
- `fabric.mod.json` points to `fr.riege.ebsl.fabric.FabricEbslMod`.
- `fabric.mod.json` loads common mixins from `ebsl.loader.mixins.json`, bundled from `src/minecraft-1-21-11/common`.
- Shared MC assets (`assets/ebsl/icon.png`, lang, and core shaders) live in `src/minecraft-1-21-11/common/src/main/resources` and are bundled into the Fabric jar from there.
- Fabric no longer compiles or contains old backend packages such as `pathfinding`, `general`, `terminal`, `ui`, `api`, `event`, or `settings`.
- Common owns the navigation backend via `CommonNavigationBackend`, `LayerNavigationPointProvider`, and `LayerPathProcessor`.
- `buildMod121Fabric` passes.

Remaining backend growth should happen only in `src/common`; version/common may fill vanilla MC data/resources/lifecycle adapters, and Fabric may fill loader-specific entrypoint or API hooks only.

---

## PHASE 1 — Gradle Skeleton

> Goal: two subprojects compile, mod still runs, nothing is moved yet.

---

### Task 1: Update settings.gradle

**Files:**
- Modify: `settings.gradle`

**Step 1: Replace contents**

```groovy
pluginManagement {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = 'ebsl'
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

**Step 2: Create subproject directories**

```bash
mkdir -p src/common/src/main/java
mkdir -p src/minecraft-1-21-11/common/src/main/java
mkdir -p src/minecraft-1-21-11/fabric/src/main/java
```

**Step 3: Commit**

```bash
git add settings.gradle
git commit -m "build: add common and minecraft-1-21-11:fabric subprojects to settings"
```

---

### Task 2: Create common/build.gradle

**Files:**
- Create: `common/build.gradle`
- Create: `common/gradle.properties`

**Step 1: Write common/build.gradle**

```groovy
plugins {
    id 'java-library'
}

group = 'fr.riege.ebsl'
version = rootProject.findProperty('mod_version') ?: '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // imgui-java binding (pure Java, no natives — provided at runtime by fabric-gui-imgui)
    compileOnly 'io.github.spair:imgui-java-binding:1.90.0'
    testImplementation 'io.github.spair:imgui-java-binding:1.90.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Fail build if any MC import leaks into common
tasks.named('compileJava') {
    doLast {
        def sources = fileTree('src/main/java').filter { it.name.endsWith('.java') }
        sources.each { file ->
            def content = file.text
            if (content.contains('net.minecraft.') || content.contains('net.fabricmc.')) {
                throw new GradleException("MC import detected in common: ${file.path}")
            }
        }
    }
}
```

**Step 2: Write common/gradle.properties**

```properties
# empty — values come from root gradle.properties
```

**Step 3: Commit**

```bash
git add common/build.gradle common/gradle.properties
git commit -m "build: add common subproject with java-library and MC import guard"
```

---

### Task 3: Create minecraft-1-21-11/fabric/build.gradle

**Files:**
- Create: `minecraft-1-21-11/gradle.properties`  (MC version shared by all loaders)
- Create: `minecraft-1-21-11/fabric/build.gradle`
- Create: `minecraft-1-21-11/fabric/gradle.properties`

**Step 1: Write minecraft-1-21-11/gradle.properties**

```properties
minecraft_version=1.21.11
```

**Step 2: Write minecraft-1-21-11/fabric/build.gradle**

```groovy
plugins {
    id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
}

version = project.mod_version
group = project.maven_group

repositories {
    mavenCentral()
}

dependencies {
    implementation project(':common')
    implementation project(':minecraft-1-21-11:common')

    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    modImplementation "cn.enaium:fabric-gui-imgui:${project.fabric_gui_imgui_version}"
    include "cn.enaium:fabric-gui-imgui:${project.fabric_gui_imgui_version}"
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": inputs.properties.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}

tasks.named('test') {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

jar {
    inputs.property "projectName", project.name
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}
```

**Step 3: Write minecraft-1-21-11/fabric/gradle.properties**

```properties
org.gradle.jvmargs=-Xmx1G
org.gradle.parallel=true
org.gradle.configuration-cache=false

loader_version=0.18.4
loom_version=1.16-SNAPSHOT

mod_version=1.0.0
maven_group=fr.riege.ebsl

fabric_api_version=0.141.3+1.21.11
fabric_gui_imgui_version=1.21.11-1.0.7+imgui.1.90.0
```

**Step 4: Commit**

```bash
git add minecraft-1-21-11/
git commit -m "build: add minecraft-1-21-11/fabric subproject with loom and common dependency"
```

---

### Task 4: Update root build.gradle to custom tasks only

**Files:**
- Modify: `build.gradle`

**Step 1: Replace root build.gradle**

```groovy
// Root project — coordination and convenience tasks only

tasks.register('runClient121Fabric') {
    group = 'ebsl'
    description = 'Run Minecraft 1.21.11 (Fabric) client'
    dependsOn ':minecraft-1-21-11:fabric:runClient'
}

tasks.register('buildMod121Fabric') {
    group = 'ebsl'
    description = 'Build Minecraft 1.21.11 (Fabric) mod jar'
    dependsOn ':minecraft-1-21-11:fabric:build'
}
// future: runClient121Forge, runClient122Fabric, etc.
```

**Step 2: Commit**

```bash
git add build.gradle
git commit -m "build: simplify root build.gradle to custom tasks only"
```

---

### Task 5: Move existing source into minecraft-1-21-11/fabric

**Files:**
- Move: `src/` → `minecraft-1-21-11/fabric/src/`

**Step 1: Move source tree**

```bash
cp -r src minecraft-1-21-11/fabric/src
```

**Step 2: Remove old root src**

```bash
git rm -r src/
```

**Step 3: Stage and commit**

```bash
git add minecraft-1-21-11/fabric/src/
git commit -m "build: move existing source into minecraft-1-21-11/fabric subproject"
```

---

### Task 6: Verify compilation

**Step 1: Run build with WSL gradle home workaround**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :minecraft-1-21-11:fabric:build --info 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

**Step 2: Verify common compiles (empty)**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
```

Expected: `BUILD SUCCESSFUL` (no sources yet, that's fine)

**Step 3: Commit if not already done**

```bash
git add .
git commit -m "build: phase 1 complete — gradle skeleton verified"
```

---

## PHASE 2 — Layer Interfaces + Value Types

> Goal: all layer interfaces exist in common, vanilla/shared MC adapter stubs live in minecraft-1-21-11/common, Fabric keeps only its entrypoint/metadata unless a Fabric API hook is required. EbslPlatform and EbslCore exist but do nothing. Mod still runs.

---

### Task 7: Value types in common

**Files:**
- Create: `common/src/main/java/fr/riege/ebsl/common/math/Vec3d.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/math/Vec3i.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/math/Vec2f.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/math/Vec2i.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/world/BlockId.java`

**Step 1: Vec3d**

```java
package fr.riege.ebsl.common.math;

public record Vec3d(double x, double y, double z) {
    public Vec3d add(double dx, double dy, double dz) {
        return new Vec3d(x + dx, y + dy, z + dz);
    }
    public double distanceTo(Vec3d other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public double distanceToSq(Vec3d other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
    public Vec3i toBlockPos() { return new Vec3i((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)); }
}
```

**Step 2: Vec3i**

```java
package fr.riege.ebsl.common.math;

public record Vec3i(int x, int y, int z) {
    public Vec3d center() { return new Vec3d(x + 0.5, y + 0.5, z + 0.5); }
    public Vec3i offset(int dx, int dy, int dz) { return new Vec3i(x + dx, y + dy, z + dz); }
    public double distanceTo(Vec3i other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
```

**Step 3: Vec2f**

```java
package fr.riege.ebsl.common.math;

public record Vec2f(float x, float y) {}
```

**Step 4: Vec2i**

```java
package fr.riege.ebsl.common.math;

public record Vec2i(int x, int y) {}
```

**Step 5: BlockId**

```java
package fr.riege.ebsl.common.world;

public record BlockId(String namespace, String path) {
    public static final BlockId AIR = new BlockId("minecraft", "air");

    public static BlockId of(String id) {
        int colon = id.indexOf(':');
        if (colon < 0) return new BlockId("minecraft", id);
        return new BlockId(id.substring(0, colon), id.substring(colon + 1));
    }

    @Override public String toString() { return namespace + ":" + path; }
}
```

**Step 6: Compile**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
```

Expected: `BUILD SUCCESSFUL`

**Step 7: Commit**

```bash
git add common/src/
git commit -m "feat(common): add value types Vec3d, Vec3i, Vec2f, Vec2i, BlockId"
```

---

### Task 8: Common event types

**Files:**
- Create: `common/src/main/java/fr/riege/ebsl/common/event/TickEvent.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/event/RenderWorldEvent.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/event/RenderHudEvent.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/event/KeyPressEvent.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/event/MouseButtonEvent.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/event/CharTypedEvent.java`

**Step 1: Write all event records**

```java
// TickEvent.java
package fr.riege.ebsl.common.event;
public record TickEvent(long tick) {}

// RenderWorldEvent.java
package fr.riege.ebsl.common.event;
// viewMatrix and projMatrix are column-major float[16]
public record RenderWorldEvent(float[] viewMatrix, float[] projMatrix, float tickDelta,
                                double camX, double camY, double camZ) {}

// RenderHudEvent.java
package fr.riege.ebsl.common.event;
public record RenderHudEvent(int screenWidth, int screenHeight, float tickDelta) {}

// KeyPressEvent.java
package fr.riege.ebsl.common.event;
// action: 0=release, 1=press, 2=repeat — GLFW constants
public record KeyPressEvent(int keyCode, int action, int modifiers) {}

// MouseButtonEvent.java
package fr.riege.ebsl.common.event;
public record MouseButtonEvent(int button, int action) {}

// CharTypedEvent.java
package fr.riege.ebsl.common.event;
public record CharTypedEvent(char character) {}
```

**Step 2: Compile and commit**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
git add common/src/
git commit -m "feat(common): add common event record types"
```

---

### Task 9: Layer interfaces

**Files:**
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IWorldLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IPlayerLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IPhysicsLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IEventBus.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IRenderLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/ICommandLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IStorageLayer.java`
- Create: `common/src/main/java/fr/riege/ebsl/common/layer/IImGuiLayer.java`

**Step 1: IWorldLayer.java**

```java
package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.world.BlockId;

public interface IWorldLayer {
    BlockId getBlock(int x, int y, int z);
    boolean isAir(int x, int y, int z);
    boolean isSolid(int x, int y, int z);
    boolean isWater(int x, int y, int z);
    boolean isLava(int x, int y, int z);
    boolean isLoaded(int x, int y, int z);
    boolean isChunkLoaded(int chunkX, int chunkZ);
    int getTopSolidY(int x, int z);
    double getBlockFriction(int x, int y, int z);
    boolean hasOpenTop(int x, int y, int z);   // e.g. slabs, stairs
    boolean hasOpenBottom(int x, int y, int z);
    double getBlockHeight(int x, int y, int z); // 1.0 for full block, 0.5 for slab
}
```

**Step 2: IPlayerLayer.java**

```java
package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.math.Vec2f;

public interface IPlayerLayer {
    Vec3d position();
    Vec2f rotation();       // yaw degrees, pitch degrees
    Vec3d velocity();
    boolean isOnGround();
    boolean isInWater();
    boolean isInLava();
    boolean isSprinting();
    boolean isSneaking();
    boolean isAlive();
    float getHealth();
    int getDimension();     // 0=overworld, -1=nether, 1=end
}
```

**Step 3: IPhysicsLayer.java**

```java
package fr.riege.ebsl.common.layer;

public interface IPhysicsLayer {
    void setForward(float value);    // -1 to 1
    void setSideways(float value);   // -1 to 1
    void setJump(boolean value);
    void setSprint(boolean value);
    void setSneak(boolean value);
    void setYaw(float yaw);
    void setPitch(float pitch);
    void clearInputs();
}
```

**Step 4: IEventBus.java**

```java
package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.event.*;
import java.util.function.Consumer;

public interface IEventBus {
    void onTick(Consumer<TickEvent> handler);
    void onRenderWorld(Consumer<RenderWorldEvent> handler);
    void onRenderHud(Consumer<RenderHudEvent> handler);
    void onKeyPress(Consumer<KeyPressEvent> handler);
    void onMouseButton(Consumer<MouseButtonEvent> handler);
    void onCharTyped(Consumer<CharTypedEvent> handler);
}
```

**Step 5: IRenderLayer.java**

```java
package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;

public interface IRenderLayer {
    void drawLine(Vec3d from, Vec3d to, int colorARGB, float width);
    void drawBox(Vec3d min, Vec3d max, int colorARGB, float width);
    void drawFilledBox(Vec3d min, Vec3d max, int colorARGB);
    void drawSphere(Vec3d center, float radius, int colorARGB, int segments);
    void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix);
    void endFrame();
}
```

**Step 6: ICommandLayer.java**

```java
package fr.riege.ebsl.common.layer;

import java.util.List;
import java.util.function.Consumer;

public interface ICommandLayer {
    @FunctionalInterface
    interface CommandHandler {
        void execute(String[] args, Consumer<String> output);
    }

    void register(String name, String description, CommandHandler handler);
    void print(String message);
    void printError(String message);
    void printSuccess(String message);
    List<String> getSuggestions(String input);
}
```

**Step 7: IStorageLayer.java**

```java
package fr.riege.ebsl.common.layer;

import java.util.Optional;

public interface IStorageLayer {
    void save(String key, String json);
    Optional<String> load(String key);
}
```

**Step 8: IImGuiLayer.java**

```java
package fr.riege.ebsl.common.layer;

public interface IImGuiLayer {
    void registerFrame(Runnable drawPanels);
    int getViewportWidth();
    int getViewportHeight();
}
```

**Step 9: Compile and commit**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
git add common/src/
git commit -m "feat(common): add 8 layer interfaces"
```

---

### Task 10: EbslPlatform record + builder

**Files:**
- Create: `common/src/main/java/fr/riege/ebsl/common/platform/EbslPlatform.java`

**Step 1: Write EbslPlatform**

```java
package fr.riege.ebsl.common.platform;

import fr.riege.ebsl.common.layer.*;

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
    public EbslPlatform {
        if (world == null)    throw new NullPointerException("world layer required");
        if (player == null)   throw new NullPointerException("player layer required");
        if (physics == null)  throw new NullPointerException("physics layer required");
        if (events == null)   throw new NullPointerException("events layer required");
        if (render == null)   throw new NullPointerException("render layer required");
        if (commands == null) throw new NullPointerException("commands layer required");
        if (storage == null)  throw new NullPointerException("storage layer required");
        if (imgui == null)    throw new NullPointerException("imgui layer required");
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private IWorldLayer world;
        private IPlayerLayer player;
        private IPhysicsLayer physics;
        private IEventBus events;
        private IRenderLayer render;
        private ICommandLayer commands;
        private IStorageLayer storage;
        private IImGuiLayer imgui;

        public Builder world(IWorldLayer v)      { this.world = v;    return this; }
        public Builder player(IPlayerLayer v)    { this.player = v;   return this; }
        public Builder physics(IPhysicsLayer v)  { this.physics = v;  return this; }
        public Builder events(IEventBus v)       { this.events = v;   return this; }
        public Builder render(IRenderLayer v)    { this.render = v;   return this; }
        public Builder commands(ICommandLayer v) { this.commands = v; return this; }
        public Builder storage(IStorageLayer v)  { this.storage = v;  return this; }
        public Builder imgui(IImGuiLayer v)      { this.imgui = v;    return this; }

        public EbslPlatform build() {
            return new EbslPlatform(world, player, physics, events, render, commands, storage, imgui);
        }
    }
}
```

**Step 2: Compile and commit**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
git add common/src/
git commit -m "feat(common): add EbslPlatform record with builder"
```

---

### Task 11: EbslCore stub

**Files:**
- Create: `common/src/main/java/fr/riege/ebsl/common/EbslCore.java`

**Step 1: Write stub EbslCore**

```java
package fr.riege.ebsl.common;

import fr.riege.ebsl.common.platform.EbslPlatform;

public class EbslCore {
    private final EbslPlatform platform;

    public EbslCore(EbslPlatform platform) {
        this.platform = platform;
        // subsystems will be wired here as they are migrated to common
    }
}
```

**Step 2: Compile and commit**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
git add common/src/
git commit -m "feat(common): add EbslCore stub composition root"
```

---

### Task 12: Stub layer implementations (split between mc-common and fabric)

**In minecraft-1-21-11/common** — loader-agnostic MC layers (package `fr.riege.ebsl.mc`):
- Create: `McWorldLayer.java` implements `IWorldLayer`
- Create: `McPlayerLayer.java` implements `IPlayerLayer`
- Create: `McRenderLayer.java` implements `IRenderLayer`
- Create: `McStorageLayer.java` implements `IStorageLayer`

**In minecraft-1-21-11/fabric** — Fabric-specific hooks:
- Keep only the entrypoint, loader metadata, and small Fabric API bridges such as client-command registration
- Put shared version-common implementations in `minecraft-1-21-11/common` when they only depend on vanilla MC APIs

**Step 1: Pattern for mc-common stub — use `McWorldLayer` as example**

```java
package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.world.BlockId;
import net.minecraft.client.Minecraft;

public class McWorldLayer implements IWorldLayer {
    private final Minecraft client;
    public McWorldLayer(Minecraft client) { this.client = client; }

    @Override public BlockId getBlock(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isAir(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isSolid(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isWater(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isLava(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isLoaded(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isChunkLoaded(int cx, int cz) { throw new UnsupportedOperationException("TODO"); }
    @Override public int getTopSolidY(int x, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public double getBlockFriction(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean hasOpenTop(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean hasOpenBottom(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public double getBlockHeight(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
}
```

Apply the same pattern for `McPlayerLayer`, `McRenderLayer`, `McStorageLayer` (takes `Path configDir`).  
Apply for loader-specific stubs only when a layer truly depends on the loader API.

**Step 2: Also create minecraft-1-21-11/common/build.gradle**

```groovy
plugins {
    id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
}

dependencies {
    implementation project(':common')
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings loom.officialMojangMappings()
    modCompileOnly "net.fabricmc:fabric-loader:${loader_version}"
}

tasks.withType(JavaCompile).configureEach { it.options.release = 21 }
```

And add `include('minecraft-1-21-11:common')` to `settings.gradle`.

**Step 3: Compile both**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :minecraft-1-21-11:common:compileJava :minecraft-1-21-11:fabric:compileJava
```

Expected: `BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
git add minecraft-1-21-11/
git commit -m "feat: add Mc*Layer stubs in mc-common and Fabric*Layer stubs in fabric"
```

---

### Task 13: FabricMod entry point (wires platform, coexists with old EbslMod)

**Files:**
- Create: `minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/fabric/FabricEbslMod.java`

**Step 1: Write FabricEbslMod**

```java
package fr.riege.ebsl.fabric;

import fr.riege.ebsl.common.EbslCore;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.fabric.layer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class FabricEbslMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Minecraft client = Minecraft.getInstance();

        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("ebsl");

        EbslPlatform platform = EbslPlatform.builder()
            // from minecraft-1-21-11/common — loader-agnostic
            .world(new McWorldLayer(client))
            .player(new McPlayerLayer(client))
            .render(new McRenderLayer())
            .storage(new McStorageLayer(configDir))
            // from minecraft-1-21-11/fabric — Fabric-specific
            .physics(new MinecraftPhysicsLayer(client))
            .events(new ModloaderEventBus())
            .commands(new FabricCommandLayer())
            .imgui(new MinecraftImGuiLayer(client))
            .build();

        new EbslCore(platform);
        // old EbslMod still runs — remove after full migration
    }
}
```

Note: do NOT add `FabricEbslMod` to `fabric.mod.json` entrypoints yet — `EbslMod` still handles everything. This class is wired in Phase 4 when each subsystem is migrated.

**Step 2: Compile and commit**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :minecraft-1-21-11:fabric:compileJava
git add minecraft-1-21-11/fabric/src/
git commit -m "feat(fabric): add FabricEbslMod entry point stub"
```

---

## PHASE 3 — Pure Subsystems (no MC to strip)

> Copy subsystems that have zero MC dependencies into common and fix package names. Delete from minecraft-1-21-11/fabric once the subsystem is wired into EbslCore.

> **Pattern for each task:** copy files → fix package → adjust imports → compile common → compile fabric → commit.

---

### Task 14: Migrate settings/ framework

**Files:**
- Copy: `minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/settings/` → `common/src/main/java/fr/riege/ebsl/common/settings/`
- Files: `AbstractSetting`, `BooleanSetting`, `ColorSetting`, `DoubleSetting`, `EnumSetting`, `IntSetting`, `Setting`, `Settingable`, `StringListSetting`, `StringSetting`

**Step 1: Copy files and update package declaration**

For each file, change:
```java
package fr.riege.ebsl.settings;
```
to:
```java
package fr.riege.ebsl.common.settings;
```

**Step 2: Verify zero MC imports** — these should be pure Java, no changes needed to code.

**Step 3: Compile**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
```

**Step 4: Update fabric references** — find all usages of `fr.riege.ebsl.settings` in minecraft-1-21-11/fabric and update imports to `fr.riege.ebsl.common.settings`.

```bash
grep -r "fr.riege.ebsl.settings" minecraft-1-21-11/fabric/src/ --include="*.java" -l
```

**Step 5: Compile fabric**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :minecraft-1-21-11:fabric:compileJava
```

**Step 6: Delete old settings from fabric** (keep only if it has MC-specific subclasses)

```bash
git rm -r minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/settings/
```

**Step 7: Commit**

```bash
git add common/src/ minecraft-1-21-11/fabric/src/
git commit -m "refactor: migrate settings framework to common"
```

---

### Task 15: Migrate registry/ framework

Same pattern as Task 14.

**Files:** `EnumRegistry`, `IRegistry`, `MapRegistry`
- Source: `fr.riege.ebsl.registry` → `fr.riege.ebsl.common.registry`

```bash
git commit -m "refactor: migrate registry framework to common"
```

---

### Task 16: Migrate analytics/

Same pattern.

**Files:** `AnalyticsEvent`, `AnalyticsEventLog`, `AnalyticsSnapshot`
- Source: `fr.riege.ebsl.analytics` → `fr.riege.ebsl.common.analytics`

Check for any MC imports in these files before moving.

```bash
grep -r "net.minecraft" minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/analytics/ --include="*.java"
```

```bash
git commit -m "refactor: migrate analytics to common"
```

---

### Task 17: Migrate event bus infrastructure

The existing event system (`EventBus`, `EventBusImpl`, `EventBridge`, etc.) uses MC types in its event records (e.g. `RenderWorldEvent` takes `Matrix4f`, `TickEvent` takes `MinecraftClient`). 

**Step 1: Move the bus machinery** — these are pure Java:
- `Event`, `EventBus`, `EventBusImpl`, `EventHandler`, `EventPhase`, `EventPriority`, `EventRegistry`, `Subscription`
- Target: `fr.riege.ebsl.common.event`

**Step 2: DO NOT move old event records** — `TickEvent`, `RenderWorldEvent`, etc. stay in fabric (they use MC types). Common already has its own event records from Task 8.

**Step 3: Wire** — `FabricEventBus` will use the internal event bus machinery to dispatch to common event consumers.

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava :minecraft-1-21-11:fabric:compileJava
git commit -m "refactor: migrate event bus infrastructure to common"
```

---

### Task 18: Migrate pathfinding/goal/ and pathfinding/result/

**Files in goal/:** `Goal`, `GoalAxisX`, `GoalAxisZ`, `GoalBlock`, `GoalChunk`, `GoalColumn`, `GoalCompositeAny`, `GoalGetToBlock`, `GoalNear`, `GoalRectangleXZ`, `GoalXZ`, `GoalYLevel`, `NavigationModeType`, `NavigationRequest`

Goal classes use `BlockPos` from MC — replace with `Vec3i` from common.

**Step 1: Check MC imports**

```bash
grep -r "net.minecraft" minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/goal/ --include="*.java"
```

**Step 2:** For each `BlockPos` usage, replace with `fr.riege.ebsl.common.math.Vec3i`.

**Step 3: Move to** `fr.riege.ebsl.common.pathfinding.goal`

**Step 4: Compile + commit**

```bash
git commit -m "refactor: migrate pathfinding goals to common, replace BlockPos with Vec3i"
```

---

### Task 19: Migrate pathfinding/pathfinder/ (A* engine)

**Files:** `AStarPathfinder`, `AbstractPathfinder`, `heap/PrimitiveMinHeap`, `processing/EvaluationContextImpl`, `processing/SearchContextImpl`

These call `IWorldLayer` equivalents — currently they use MC world directly via `EvaluationContext`/`SearchContext`. 

**Step 1:** Check what world data they access:

```bash
grep -r "net.minecraft" minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/pathfinder/ --include="*.java"
```

**Step 2:** Replace any direct MC world calls with `IWorldLayer` method calls. `EvaluationContextImpl` likely takes a world reference — replace with `IWorldLayer`.

**Step 3:** Move to `fr.riege.ebsl.common.pathfinding.pathfinder`

**Step 4: Compile + commit**

```bash
git commit -m "refactor: migrate A* pathfinder engine to common"
```

---

### Task 20: Migrate rotation/ and util/

**Files:** `AngleUtils`, `EasingType`, `IRotationStrategy`, `Rotation`, `RotationDebug`, `strategy/TimedEaseStrategy`  
Also: `PathPosition`, `PathVector`, `BlockPosUtil`, `RegionKey`

`BlockPosUtil` may reference MC `BlockPos` — replace with `Vec3i`. `PathPosition`/`PathVector` likely wrap `BlockPos` — replace with `Vec3i`/`Vec3d`.

```bash
grep -r "net.minecraft" minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/rotation/ minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/wrapper/ minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/util/ --include="*.java"
```

```bash
git commit -m "refactor: migrate rotation, path wrappers, and utils to common"
```

---

## PHASE 4 — Layer Implementations + Layer-Dependent Subsystems

> Fill in each `Mc*Layer` (in minecraft-1-21-11/common) and `Fabric*Layer` (in minecraft-1-21-11/fabric) one by one, then migrate subsystems that depend on them.

---

### Task 21: Implement McWorldLayer

**File:** `minecraft-1-21-11/common/src/main/java/fr/riege/ebsl/mc/McWorldLayer.java`

**Step 1: Implement each method using MC APIs**

```java
@Override
public BlockId getBlock(int x, int y, int z) {
    var level = client.level;
    if (level == null) return BlockId.AIR;
    var state = level.getBlockState(new BlockPos(x, y, z));
    var key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
    return BlockId.of(key.toString());
}

@Override
public boolean isAir(int x, int y, int z) {
    var level = client.level;
    return level == null || level.getBlockState(new BlockPos(x, y, z)).isAir();
}

@Override
public boolean isSolid(int x, int y, int z) {
    var level = client.level;
    if (level == null) return false;
    return level.getBlockState(new BlockPos(x, y, z)).isSolidRender(level, new BlockPos(x, y, z));
}

// ... implement remaining methods
```

**Step 2: Compile**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :minecraft-1-21-11:common:compileJava
```

**Step 3: Commit**

```bash
git commit -m "feat(mc-common): implement McWorldLayer"
```

---

### Task 22: Migrate WalkabilityChecker and movement evaluators

These files currently take an MC `Level` or `BlockPos`. Replace those parameters with `IWorldLayer`.

**Files to migrate to common:**
- `pathfinding/movement/WalkabilityChecker`
- `pathfinding/movement/types/evaluation/*` (all evaluators)
- `pathfinding/movement/geometry/StairEntryClassifier`

**Step 1: For each evaluator, find MC dependencies**

```bash
grep -rn "net.minecraft" minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/pathfinding/movement/ --include="*.java" | grep -v "//.*net.minecraft"
```

**Step 2:** Replace `Level level` / `BlockPos pos` parameters with `IWorldLayer world` / `Vec3i pos`. Update method calls accordingly.

**Step 3:** Move to `fr.riege.ebsl.common.pathfinding.movement`

**Step 4: Compile + commit**

```bash
git commit -m "refactor: migrate WalkabilityChecker and movement evaluators to common"
```

---

### Task 23: Implement McPlayerLayer + MinecraftPhysicsLayer

**File:** `minecraft-1-21-11/common/src/main/java/fr/riege/ebsl/mc/McPlayerLayer.java`

```java
@Override
public Vec3d position() {
    var player = client.player;
    if (player == null) return new Vec3d(0, 0, 0);
    return new Vec3d(player.getX(), player.getY(), player.getZ());
}

@Override
public Vec2f rotation() {
    var player = client.player;
    if (player == null) return new Vec2f(0, 0);
    return new Vec2f(player.getYRot(), player.getXRot());
}

@Override
public Vec3d velocity() {
    var player = client.player;
    if (player == null) return new Vec3d(0, 0, 0);
    return new Vec3d(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
}

// ... implement remaining
```

**MinecraftPhysicsLayer:**

```java
@Override
public void setForward(float value) {
    var input = client.player == null ? null : client.player.input;
    if (input instanceof KeyboardInput ki) ki.up = value > 0; // adapt to actual MC input API
}
// Note: check actual MC 1.21.11 input API — may use KeyMapping or direct field injection
```

**Step 2: Compile + commit**

```bash
git commit -m "feat(mc-common): implement McPlayerLayer and MinecraftPhysicsLayer"
```

---

### Task 24: Migrate movement executors and PathExecutor

**Files:** all `pathfinding/movement/types/execution/*`, `pathfinding/execution/PathExecutor`, `pathfinding/execution/WalkMovementController`, etc.

These take MC player/world references — replace with `IPlayerLayer`, `IPhysicsLayer`, `IWorldLayer`.

```bash
git commit -m "refactor: migrate movement executors to common"
```

---

### Task 25: Implement FabricEventBus

**File:** `minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/fabric/layer/FabricEventBus.java`

Wire Fabric events to common event consumers. Use the existing `EventBridge` as reference.

```java
public class FabricEventBus implements IEventBus {
    private final List<Consumer<TickEvent>> tickHandlers = new ArrayList<>();
    private final List<Consumer<RenderWorldEvent>> renderWorldHandlers = new ArrayList<>();
    // ...

    public void register() {
        // Wire fabric ClientTickEvents → dispatch to tickHandlers
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            var event = new fr.riege.ebsl.common.event.TickEvent(client.level != null ? client.level.getGameTime() : 0);
            tickHandlers.forEach(h -> h.accept(event));
        });
        // Wire render world events similarly (via mixin or Fabric render callback)
    }

    @Override public void onTick(Consumer<fr.riege.ebsl.common.event.TickEvent> handler) { tickHandlers.add(handler); }
    // ... etc
}
```

```bash
git commit -m "feat(fabric): implement FabricEventBus wired to Fabric events"
```

---

### Task 26: Implement McRenderLayer

**File:** `minecraft-1-21-11/common/src/main/java/fr/riege/ebsl/mc/McRenderLayer.java`

Port rendering from `EbslMeshRenderer` / `EbslRenderPipelines`. The layer stores the current frame's camera + matrices set by `beginFrame()`, then uses them in draw calls.

```java
public class McRenderLayer implements IRenderLayer {
    private double camX, camY, camZ;
    private float[] viewMatrix, projMatrix;

    @Override
    public void beginFrame(double cx, double cy, double cz, float[] view, float[] proj) {
        this.camX = cx; this.camY = cy; this.camZ = cz;
        this.viewMatrix = view; this.projMatrix = proj;
    }

    @Override
    public void drawLine(Vec3d from, Vec3d to, int colorARGB, float width) {
        // use existing EbslMeshRenderer logic, relative to camera
    }
    // ...
}
```

```bash
git commit -m "feat(mc-common): implement McRenderLayer"
```

---

### Task 27: Migrate PathVisualizer and visualization to common

```bash
git commit -m "refactor: migrate PathVisualizer to common using IRenderLayer"
```

---

### Task 28: Implement FabricImGuiLayer

**File:** `minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/fabric/layer/FabricImGuiLayer.java`

Port the existing `EbslImGuiService` / `EbslImGuiOverlay` logic. The layer calls `drawPanels.run()` inside the imgui frame loop.

```java
public class FabricImGuiLayer implements IImGuiLayer {
    private final Minecraft client;
    private Runnable drawPanels;

    public FabricImGuiLayer(Minecraft client) { this.client = client; }

    @Override
    public void registerFrame(Runnable drawPanels) {
        this.drawPanels = drawPanels;
        // Register with fabric-gui-imgui to call onFrame each tick
    }

    public void onFrame() {
        if (drawPanels != null) drawPanels.run();
    }

    @Override public int getViewportWidth() { return client.getWindow().getWidth(); }
    @Override public int getViewportHeight() { return client.getWindow().getHeight(); }
}
```

```bash
git commit -m "feat(fabric): implement FabricImGuiLayer"
```

---

### Task 29: Migrate ImGui panels to common

Move all `ui/imgui/panel/*` to `common`. Panels use only imgui-java API (`ImGui.*` calls) and data from common subsystems — zero MC imports.

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
# Verify no MC imports
git commit -m "refactor: migrate ImGui panels to common"
```

---

### Task 30: Implement FabricCommandLayer + migrate terminal

**FabricCommandLayer** wraps Brigadier (MC's command dispatcher) and the in-game chat terminal.

Migrate `terminal/` to `common` — strip any MC/Brigadier types from `Command`, `CommandHandler`, etc. Use `ICommandLayer.CommandHandler` as the function type.

```bash
git commit -m "feat(fabric): implement FabricCommandLayer; refactor: migrate terminal to common"
```

---

### Task 31: Implement McStorageLayer + migrate settings stores

```java
public class McStorageLayer implements IStorageLayer {
    private final Path dir;
    public McStorageLayer(Path dir) { this.dir = dir; }

    @Override
    public void save(String key, String json) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(key + ".json"), json);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<String> load(String key) {
        try {
            var file = dir.resolve(key + ".json");
            return Files.exists(file) ? Optional.of(Files.readString(file)) : Optional.empty();
        } catch (IOException e) { return Optional.empty(); }
    }
}
```

```bash
git commit -m "feat(mc-common): implement McStorageLayer; refactor: migrate settings stores to common"
```

---

### Task 32: Wire EbslCore + switch fabric.mod.json entrypoint

**Step 1: Fill in EbslCore with all subsystems**

```java
public class EbslCore {
    public EbslCore(EbslPlatform platform) {
        var pathfinding = new PathfindingManager(platform.world(), platform.player(), platform.physics());
        var terminal    = new TerminalManager(platform.commands(), pathfinding);
        var visualizer  = new PathVisualizationManager(platform.render());
        var ui          = new EbslImGuiManager(platform.imgui(), pathfinding, terminal);

        platform.events().onTick(e -> {
            pathfinding.tick();
        });
        platform.events().onRenderWorld(e -> {
            platform.render().beginFrame(e.camX(), e.camY(), e.camZ(), e.viewMatrix(), e.projMatrix());
            visualizer.renderWorld();
            platform.render().endFrame();
        });
        platform.imgui().registerFrame(ui::draw);
    }
}
```

**Step 2: Update fabric.mod.json** — replace old entrypoint with `FabricEbslMod`

```json
"entrypoints": {
    "client": [
        "fr.riege.ebsl.fabric.FabricEbslMod"
    ]
}
```

**Step 3: Remove old EbslMod.java and EventBridge**

```bash
git rm minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl/EbslMod.java
```

**Step 4: Build and run to verify**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew runClient121
```

**Step 5: Commit**

```bash
git commit -m "feat: wire EbslCore, switch entrypoint to FabricEbslMod, remove old EbslMod"
```

---

## PHASE 5 — Enforcement + Cleanup

---

### Task 33: Verify MC import guard

**Step 1: Intentionally add a test MC import to common**

```java
// in any common file, temporarily add:
import net.minecraft.client.Minecraft; // TEST
```

**Step 2: Run build — expect failure**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew :common:compileJava
```

Expected: `GradleException: MC import detected in common: ...`

**Step 3: Remove test import, commit**

```bash
git commit -m "build: verify MC import guard works in common"
```

---

### Task 34: Final cleanup

**Step 1: Find any remaining dead code in minecraft-1-21-11/fabric**

```bash
# Check for any logic packages that should have been moved
find minecraft-1-21-11/fabric/src/main/java/fr/riege/ebsl -name "*.java" ! -path "*/fabric/*" ! -path "*/mixin/*" | head -30
```

**Step 2: Verify common has no MC imports at all**

```bash
grep -r "net.minecraft\|net.fabricmc" common/src/main/java/ --include="*.java"
```

Expected: no output.

**Step 3: Final build**

```bash
GRADLE_USER_HOME=/home/riege/gradle-home-test ./gradlew buildMod121
```

Expected: `BUILD SUCCESSFUL`

**Step 4: Commit**

```bash
git commit -m "refactor: phase 5 complete — common module enforced, cleanup done"
```

---

## Adding a New MC Version (reference)

```bash
see design doc — adding a new MC version
# Update gradle.properties in fabric-1-22:
#   minecraft_version=1.22
#   fabric_api_version=...
# Fix compilation errors in Fabric*Layer classes (MC API changes)
# Add to root build.gradle:
#   tasks.register('runClient122') { dependsOn ':fabric-1-22:runClient' }
# Add to settings.gradle:
#   include('fabric-1-22')
```

Zero changes to `common/`.
