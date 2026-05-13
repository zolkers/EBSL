# API Surface

EBSL is moving toward a contract-first public surface. Public code should describe a stable capability first, then hide the concrete implementation behind that capability whenever the implementation is not the extension point itself.

## Public API

These packages are intended to be consumed directly by application code, modloader integrations, tests, and future external contributors:

- `fr.riege.ebsl.common.platform.layer`
- `fr.riege.ebsl.common.world.layer`
- `fr.riege.ebsl.common.platform.render`
- `fr.riege.ebsl.common.pathfinding.pathing`
- `fr.riege.ebsl.common.pathfinding.pathing.result`
- `fr.riege.ebsl.common.pathfinding.provider`
- `fr.riege.ebsl.common.pathfinding.goal`
- `fr.riege.ebsl.common.navigation`
- `fr.riege.ebsl.common.platform.service`

Public types in these packages should usually be interfaces, value objects, enums, annotations, or small factory classes.

## SPI And Extension Points

The pathfinder extension surface is contract-first:

- `Pathfinder` is the planning contract.
- `InspectablePathfinder` adds diagnostics without exposing the algorithm class.
- `Pathfinders` creates default algorithm instances.
- `NavigationPointProvider` describes point lookup.
- `WorldNavigationPointProvider` exposes world-backed cache/checker behavior without exposing the concrete provider.
- `NavigationPointProviders` creates provider implementations.
- `Path`, `PathfinderResult`, `Paths`, and `PathfinderResults` keep result consumers on contracts.
- `EventBus` and `EventBuses` keep event dispatch consumers on the event contract.
- `McPlatformLayers` exposes Minecraft-backed platform adapters through platform/world contracts.

Movement classification, movement cost, quality scoring, heuristics, neighbor expansion, and node processing remain explicit contracts:

- `MovementTypeClassifier`
- `MovementCostModel`
- `PathQualityMetric`
- `IHeuristicStrategy`
- `INeighborStrategy`
- `NodeProcessor`

## Implementation Packages

Implementation packages should not be used as extension points unless a type is deliberately documented as one:

- `fr.riege.ebsl.common.pathfinding.pathfinder`
- `fr.riege.ebsl.common.pathfinding.pathfinder.processing`
- `fr.riege.ebsl.common.pathfinding.pathfinder.heap`
- `fr.riege.ebsl.common.pathfinding.pathing.processing.impl`
- Minecraft loader packages under `fr.riege.ebsl.loader` and `fr.riege.ebsl.mc`

Concrete algorithm classes such as `AStarPathfinder` are package-private. Consumers must use `Pathfinders`.

Concrete world-backed navigation point providers are package-private. Consumers must use `NavigationPointProviders`.

Concrete event bus implementations are package-private. Consumers must use `EventBuses`.

Concrete Minecraft layer adapters are package-private. Loader bootstrap code must use `McPlatformLayers`.

## Rules For New Code

1. Add a public interface first when a behavior may have multiple implementations, may be mocked, or crosses a module boundary.
2. Keep concrete classes package-private when they exist only to satisfy a public contract.
3. Expose factories for default implementations instead of public constructors on implementation classes.
4. Keep records and enums public only when they are value objects in the domain language.
5. Avoid static service access in new code unless the type is explicitly an application facade.
6. Do not expose third-party implementation types in public contracts unless that type is already part of the public domain model.
