package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.*;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GoalUiCatalog {
    private static final List<GoalUiDefinition> GOALS = List.of(
        GoalUiDefinition.builder("walk", "Walk")
            .description("Walk to an exact block.")
            .currentXYZ()
            .executor((nav, v) -> start(nav, new GoalBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("fly", "Fly")
            .description("Fly to an exact block.")
            .currentXYZ()
            .executor((nav, v) -> start(nav, new GoalBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.FLY))
            .build(),
        GoalUiDefinition.builder("walkxz", "Walk XZ")
            .description("Walk to an X/Z column with long range support.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .executor((nav, v) -> start(nav, new GoalXZ(v.get("x"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("near", "Near")
            .description("Walk near a block within a radius.")
            .currentXYZ()
            .parameter(GoalParameter.constant("radius", "Radius", 2))
            .executor((nav, v) -> start(nav, new GoalNear(v.get("x"), v.get("y"), v.get("z"), v.get("radius")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("getto", "Get To Block")
            .description("Get to a reachable adjacent position for a block.")
            .currentXYZ()
            .executor((nav, v) -> start(nav, new GoalGetToBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("y", "Y Level")
            .description("Walk until the target Y level is reached.")
            .parameter(GoalParameter.currentY())
            .executor((nav, v) -> start(nav, new GoalYLevel(v.get("y")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("offset", "Offset")
            .description("Walk relative to the current position.")
            .parameter(GoalParameter.constant("dx", "dX", 0))
            .parameter(GoalParameter.constant("dy", "dY", 0))
            .parameter(GoalParameter.constant("dz", "dZ", 0))
            .executor(GoalUiCatalog::startOffset)
            .build(),
        GoalUiDefinition.builder("axisx", "Axis X")
            .description("Reach a target X axis.")
            .parameter(GoalParameter.currentX())
            .executor((nav, v) -> start(nav, new GoalAxisX(v.get("x")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("axisz", "Axis Z")
            .description("Reach a target Z axis.")
            .parameter(GoalParameter.currentZ())
            .executor((nav, v) -> start(nav, new GoalAxisZ(v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("column", "Column")
            .description("Reach an X/Z column.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .parameter(GoalParameter.constant("radius", "Radius", 1))
            .executor((nav, v) -> start(nav, new GoalColumn(v.get("x"), v.get("z"), v.get("radius")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("rect", "Rectangle")
            .description("Reach any block inside an X/Z rectangle.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .parameter(GoalParameter.constant("x2", "X2", 0))
            .parameter(GoalParameter.constant("z2", "Z2", 0))
            .executor(GoalUiCatalog::startRectangle)
            .build(),
        GoalUiDefinition.builder("chunk", "Chunk")
            .description("Reach a chunk.")
            .parameter(GoalParameter.constant("chunkX", "Chunk X", 0))
            .parameter(GoalParameter.constant("chunkZ", "Chunk Z", 0))
            .executor((nav, v) -> start(nav, new GoalChunk(v.get("chunkX"), v.get("chunkZ")), NavigationModeType.WALK))
            .build()
    );

    private static final Map<String, GoalUiDefinition> BY_ID = GOALS.stream()
        .collect(Collectors.toUnmodifiableMap(GoalUiDefinition::id, Function.identity()));

    private GoalUiCatalog() {}

    public static List<GoalUiDefinition> all() { return GOALS; }

    public static GoalUiDefinition byId(String id) {
        GoalUiDefinition def = BY_ID.get(id);
        if (def == null) throw new IllegalArgumentException("Unknown goal: " + id);
        return def;
    }

    private static int start(NavigationService nav, fr.riege.ebsl.common.pathfinding.goal.Goal goal, NavigationModeType mode) {
        nav.startNavigation(NavigationRequest.builder(goal).mode(mode).build());
        return 1;
    }

    private static int startOffset(NavigationService nav, Map<String, Integer> v) {
        Vec3d pos = EbslServices.platform().player().position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        return start(nav, new GoalBlock(
            px + v.getOrDefault("dx", 0),
            py + v.getOrDefault("dy", 0),
            pz + v.getOrDefault("dz", 0)), NavigationModeType.WALK);
    }

    private static int startRectangle(NavigationService nav, Map<String, Integer> v) {
        return start(nav, new GoalRectangleXZ(
            Math.min(v.get("x"), v.get("x2")), Math.min(v.get("z"), v.get("z2")),
            Math.max(v.get("x"), v.get("x2")), Math.max(v.get("z"), v.get("z2"))), NavigationModeType.WALK);
    }
}
