# Goal NavigationTarget Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Éliminer le if-else de 10 branches dans `CommonNavigationBackend.startNavigation` en ajoutant `NavigationTarget` sealed interface et `Goal.resolve()`.

**Architecture:** Chaque `Goal` sait maintenant comment il se résout en une cible de navigation (`Block(x,y,z)` ou `Column(x,z)`). `CommonNavigationBackend.startNavigation` devient un switch à 2 cas. On en profite pour corriger l'incohérence `onFinished` (seul `GoalBlock` passait le callback — maintenant tous les goals `Block` le font).

**Tech Stack:** Java 21 (sealed interfaces + record patterns), Gradle multi-module (`:common`)

---

## Fichiers

| Action | Chemin |
|---|---|
| Créer | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/NavigationTarget.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/Goal.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalBlock.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalNear.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalGetToBlock.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalYLevel.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalXZ.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalColumn.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalAxisX.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalAxisZ.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalRectangleXZ.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalChunk.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/GoalCompositeAny.java` |
| Modifier | `src/common/src/main/java/fr/riege/ebsl/common/navigation/CommonNavigationBackend.java` |

---

## Task 1 : Créer `NavigationTarget`

**Files:**
- Create: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/NavigationTarget.java`

- [ ] **Step 1 : Créer le fichier**

```java
package fr.riege.ebsl.common.pathfinding.goal;

public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z)       implements NavigationTarget {}
}
```

- [ ] **Step 2 : Vérifier la compilation**

```bash
./gradlew :common:compileJava
```

Attendu : `BUILD SUCCESSFUL`

- [ ] **Step 3 : Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/NavigationTarget.java
git commit -m "feat: add NavigationTarget sealed interface (Block / Column)"
```

---

## Task 2 : Ajouter `Goal.resolve()` + implémenter sur chaque goal

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/Goal.java`
- Modify: les 11 fichiers goal listés dans la table ci-dessus

- [ ] **Step 1 : Ajouter la méthode à l'interface `Goal`**

Dans `Goal.java`, ajouter après `String debugName();` :

```java
NavigationTarget resolve(int px, int py, int pz);
```

Le fichier complet doit ressembler à :

```java
package fr.riege.ebsl.common.pathfinding.goal;

public interface Goal {
    boolean isInGoal(int x, int y, int z);

    double heuristic(int x, int y, int z);

    String debugName();

    NavigationTarget resolve(int px, int py, int pz);
}
```

- [ ] **Step 2 : Implémenter `resolve()` dans `GoalBlock`**

Ajouter à la fin du record, avant la dernière `}` :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Block(x, y, z);
}
```

- [ ] **Step 3 : Implémenter `resolve()` dans `GoalNear`**

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Block(x, y, z);
}
```

- [ ] **Step 4 : Implémenter `resolve()` dans `GoalGetToBlock`**

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Block(x, y, z);
}
```

- [ ] **Step 5 : Implémenter `resolve()` dans `GoalYLevel`**

`GoalYLevel` n'a que le `y` — il utilise la position joueur pour `x` et `z` :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Block(px, y, pz);
}
```

- [ ] **Step 6 : Implémenter `resolve()` dans `GoalXZ`**

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column(x, z);
}
```

- [ ] **Step 7 : Implémenter `resolve()` dans `GoalColumn`**

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column(x, z);
}
```

- [ ] **Step 8 : Implémenter `resolve()` dans `GoalAxisX`**

`GoalAxisX` n'a que `x` — utilise `pz` du joueur :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column(x, pz);
}
```

- [ ] **Step 9 : Implémenter `resolve()` dans `GoalAxisZ`**

`GoalAxisZ` n'a que `z` — utilise `px` du joueur :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column(px, z);
}
```

- [ ] **Step 10 : Implémenter `resolve()` dans `GoalRectangleXZ`**

Centre du rectangle :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column((minX + maxX) / 2, (minZ + maxZ) / 2);
}
```

- [ ] **Step 11 : Implémenter `resolve()` dans `GoalChunk`**

Centre du chunk :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Column(chunkX * 16 + 8, chunkZ * 16 + 8);
}
```

- [ ] **Step 12 : Implémenter `resolve()` dans `GoalCompositeAny`**

Conserve le comportement actuel (else branch → position joueur) :

```java
@Override
public NavigationTarget resolve(int px, int py, int pz) {
    return new NavigationTarget.Block(px, py, pz);
}
```

- [ ] **Step 13 : Vérifier la compilation**

```bash
./gradlew :common:compileJava
```

Attendu : `BUILD SUCCESSFUL` — si erreur, c'est un goal qui n'a pas encore son `resolve()`.

- [ ] **Step 14 : Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/goal/
git commit -m "feat: implement Goal.resolve() on all goal types"
```

---

## Task 3 : Mettre à jour `CommonNavigationBackend`

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/navigation/CommonNavigationBackend.java`

- [ ] **Step 1 : Ajouter `this.onFinished` dans `configureOptions`**

Dans la méthode `configureOptions` (ligne ~636), ajouter `this.onFinished = request.onFinished();` comme première ligne du corps :

```java
private void configureOptions(NavigationRequest request) {
    this.onFinished = request.onFinished();   // <-- ajout
    this.onFailed = request.onFailed();
    this.allowParkour = request.allowParkour();
    this.allowJump = request.allowJump();
    this.allowFall = request.allowFall();
    this.allowWalkDiagonal = request.allowWalkDiagonal();
    this.preciseExecution = request.preciseGoalTolerance() != ExecutionOptions.DEFAULT_TOLERANCE;
    double stickySneakDistance = preciseExecution && request.allowSneak() ? 5.0 : -1.0;
    this.executionOptions = new ExecutionOptions(
        request.allowReplan(),
        request.allowJump(),
        request.allowRotation(),
        request.allowSneak(),
        preciseExecution,
        stickySneakDistance,
        request.allowSneak() && executor.isSneakLatched(),
        0.5,
        0.5,
        request.preciseGoalTolerance());
}
```

- [ ] **Step 2 : Ajouter `this.onFinished = null` dans `configureDefaults`**

```java
private void configureDefaults() {
    this.onFinished = null;   // <-- ajout
    this.onFailed = null;
    this.executionOptions = ExecutionOptions.defaults();
    this.preciseExecution = false;
    this.allowParkour = true;
    this.allowJump = true;
    this.allowFall = true;
    this.allowWalkDiagonal = true;
}
```

- [ ] **Step 3 : Modifier `startBlockGoalConfigured` pour passer `this.onFinished`**

```java
private void startBlockGoalConfigured(int x, int y, int z) {
    longRangeSession.clear();
    startPathTo(new PathPosition(x, y, z), this.onFinished, true, true);
}
```

- [ ] **Step 4 : Remplacer `startNavigation` par le switch**

Remplacer la méthode complète `startNavigation` :

```java
@Override public void startNavigation(NavigationRequest request) {
    configureOptions(request);
    Vec3d pos = player.position();
    int px = (int) Math.floor(pos.x());
    int py = (int) Math.floor(pos.y());
    int pz = (int) Math.floor(pos.z());
    switch (request.goal().resolve(px, py, pz)) {
        case NavigationTarget.Block(int x, int y, int z) -> startBlockGoalConfigured(x, y, z);
        case NavigationTarget.Column(int x, int z)       -> startColumnGoalConfigured(x, z);
    }
}
```

Ajouter l'import si nécessaire (dans le bloc d'imports `pathfinding.goal.*`, il devrait déjà être couvert par le wildcard).

- [ ] **Step 5 : Vérifier la compilation complète**

```bash
./gradlew :common:compileJava :minecraft-1-21-11:common:compileJava
```

Attendu : `BUILD SUCCESSFUL`

- [ ] **Step 6 : Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/navigation/CommonNavigationBackend.java
git commit -m "refactor: replace startNavigation if-else with Goal.resolve() switch"
```
