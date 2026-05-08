# EBSL

EBSL is a tiny task scripting layer for EBSL bot workflows. It maps Pathmind-style node names to line-based script tasks and runs through the normal `BotTaskRegistry`.

Scripts use the `.ebsl` extension and can be loaded with:

```text
ebsl run main
```

The Minecraft storage layer resolves that as `scripts/main.ebsl` inside the mod storage directory. The task can also run inline text through:

```text
ebsl inline message "hello"
```

Multiple scripts can run at the same time. Each run gets a handle such as `main#1`; stop a specific run by handle, stop every run of a file by script name, or stop everything:

```text
ebsl status
ebsl stop main#1
ebsl stop main
ebsl stop all
```

## Basics

```ebsl
start
message "hello from ebsl"
goto 120 64 -40
wait 1s
stop
```

Comments start with `#`. Braces create nested blocks.

## Flow

```ebsl
repeat 3 {
  jump 4t
  wait 10t
}

if sensor_health_below 8 {
  stop_all
} else {
  message "still fine"
}

event_function done {
  message "done"
}

event_call done
```

## Data

```ebsl
set count 0
change count 1
operator_random roll 1 6
create_list targets
add_to_list targets "minecraft:zombie"
list_length target_count targets
```

Variables are referenced with `$name`.

## Player And Navigation

```ebsl
walk forward 20t
press_key sneak 10t
look 180 20
travel 120 64 -40
come
```

`goto`, `travel`, `goal`, `path`, and `come` start navigation through `NavigationService` and block the script until navigation finishes.

Every goal from the EBSL goal catalogue is also exposed as a script node. The canonical form is `goal_<id>` so it never collides with player action nodes like `walk`:

```ebsl
goal_walk 120 64 -40
goal_near 120 64 -40 3
goal_walkxz 120 -40
```

## Leaf Mining Loop

This pattern keeps searching for oak leaves, aims smoothly at the nearest loaded leaf, waits until the crosshair actually targets a leaf, then breaks it and loops:

```ebsl
start

forever {
  goal_nearest_block minecraft:oak_leaves 32 4

  repeat_until sensor_targeted_block minecraft:oak_leaves {
    aim_at_block minecraft:oak_leaves 32 6t
    wait 1t
  }

  break_block minecraft:oak_leaves 5s
  wait 3t
}
```

## Existing Tasks

Existing bot tasks can also be script nodes. `space_mob` drives the existing Space Mob task instead of duplicating its behavior:

```ebsl
space_mob on closest 3 0.35 32
wait 10s
space_mob off

space_mob on name Zombie 4 0.5 48 track
```

## Catalogue

`ebsl tasks` prints the full Pathmind-inspired task catalogue. Entries marked with `*` are reserved for compatibility and parsing, but do not yet have a backend action in EBSL.
