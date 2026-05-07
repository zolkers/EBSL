# Common Module Architecture

`common` is the platform-neutral core. Minecraft loaders, Bukkit/Paper adapters,
headless simulations, and tests should depend inward on these packages instead of
adding platform logic here.

## Package Map

- `fr.riege.ebsl.common.api`: public facade for integrations and tooling.
- `fr.riege.ebsl.common.navigation.runtime`: reusable runtime adapters that are
  not tied to a Minecraft client. `navigation.runtime.headless` is for
  tests/simulations, and `navigation.runtime.entity` is the server-side
  actor/motor contract for Bukkit/Paper-like adapters.
- `fr.riege.ebsl.common.navigation`: navigation orchestration and path planning
  services that compose the pathfinder with a runtime.
- `fr.riege.ebsl.common.pathfinding`: the actual pathfinding engine, movement
  evaluation, execution primitives, goals, checks, and settings.
- `fr.riege.ebsl.common.platform.layer`: low-level platform ports implemented by loaders
  or server adapters.
- `fr.riege.ebsl.common.platform` and `fr.riege.ebsl.common.platform.service`: installed
  platform/service composition roots.
- `fr.riege.ebsl.common.feature`: application-facing modules, tasks, terminal
  commands, and UI.
- `fr.riege.ebsl.common.domain`: shared domain records such as analytics,
  entities, packets, and world ids.
- `fr.riege.ebsl.common.core`: infrastructure such as events, settings,
  registries, and logging.
- `fr.riege.ebsl.common.math`: shared math value types.

## Dependency Direction

`api` may depend on common services and runtime factories. `navigation.runtime`
may depend on `navigation`, `platform.layer`, `math`, and `pathfinding` value
types. `navigation` may compose `pathfinding`, layers, and runtime contracts.
`pathfinding` should remain independent from loader/server packages and must not
import Minecraft classes.
