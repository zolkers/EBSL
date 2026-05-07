# Design : Refactor `CommonNavigationBackend` — `NavigationTarget` + `Goal.resolve()`

**Date:** 2026-05-07  
**Scope:** `pathfinding/goal/` + `navigation/CommonNavigationBackend.java`

---

## Problème

`CommonNavigationBackend.startNavigation` contient un if-else chain de 10 branches qui pattern-matche sur les types concrets de `Goal` pour router vers `startBlockGoalConfigured(x,y,z)` ou `startColumnGoalConfigured(x,z)`. Chaque nouveau type de goal oblige à modifier cette classe.

---

## Solution

### 1. `NavigationTarget` (sealed interface, nouveau fichier)

```
pathfinding/goal/NavigationTarget.java
```

```java
public sealed interface NavigationTarget {
    record Block(int x, int y, int z) implements NavigationTarget {}
    record Column(int x, int z)       implements NavigationTarget {}
}
```

### 2. `Goal.resolve(int px, int py, int pz)` (nouvelle méthode sur l'interface)

Chaque goal implémente sa propre résolution. `px/py/pz` = position floored du joueur, fournie par le caller.

| Goal | Retourne |
|---|---|
| `GoalBlock(x,y,z)` | `Block(x,y,z)` |
| `GoalNear(x,y,z)` | `Block(x,y,z)` |
| `GoalGetToBlock(x,y,z)` | `Block(x,y,z)` |
| `GoalYLevel(y)` | `Block(px, y, pz)` |
| `GoalXZ(x,z)` | `Column(x,z)` |
| `GoalColumn(x,z)` | `Column(x,z)` |
| `GoalAxisX(x)` | `Column(x, pz)` |
| `GoalAxisZ(z)` | `Column(px, z)` |
| `GoalRectangleXZ` | `Column((minX+maxX)/2, (minZ+maxZ)/2)` |
| `GoalChunk` | `Column(chunkX*16+8, chunkZ*16+8)` |
| `GoalCompositeAny` | `Block(px, py, pz)` (comportement actuel conservé) |

### 3. `CommonNavigationBackend.startNavigation` — switch à 2 cas

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

**Fix inclus (`onFinished` unifié) :**  
Actuellement seul `GoalBlock` passait `request.onFinished()` à `startPathTo` — tous les autres goals `Block` perdaient le callback.  
Correction :
- `configureOptions(request)` stocke `this.onFinished = request.onFinished()`
- `configureDefaults()` met `this.onFinished = null`
- `startBlockGoalConfigured` passe `this.onFinished` (au lieu de `null`) à `startPathTo`

Ainsi tous les goals `Block` (GoalBlock, GoalNear, GoalGetToBlock, GoalYLevel, GoalCompositeAny) honorent le callback.

---

## Fichiers modifiés

- **Nouveau :** `pathfinding/goal/NavigationTarget.java`
- **Modifié :** `pathfinding/goal/Goal.java` — ajout de `resolve()`
- **Modifié :** chaque goal (11 fichiers) — implémentation de `resolve()`
- **Modifié :** `navigation/CommonNavigationBackend.java` — `startNavigation` + `configureOptions`

---

## Ce qui ne change pas

- `LongRangePathSession`, `PathExecutor`, `AStarPathfinder` — aucun changement
- Les méthodes `startBlockGoalConfigured` / `startColumnGoalConfigured` — inchangées
- `NavigationRequest`, `NavigationService` — inchangés
