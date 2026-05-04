package fr.riege.ebsl.terminal.goal;

import fr.riege.ebsl.terminal.GoalCommandSupport;
import fr.riege.ebsl.pathfinding.goal.GoalAxisX;
import fr.riege.ebsl.pathfinding.goal.GoalAxisZ;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.GoalChunk;
import fr.riege.ebsl.pathfinding.goal.GoalColumn;
import fr.riege.ebsl.pathfinding.goal.GoalGetToBlock;
import fr.riege.ebsl.pathfinding.goal.GoalNear;
import fr.riege.ebsl.pathfinding.goal.GoalRectangleXZ;
import fr.riege.ebsl.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.pathfinding.goal.GoalYLevel;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GoalUiCatalog {
    private static final List<GoalUiDefinition> GOALS = List.of(
        GoalUiDefinition.builder("walk", "Walk")
            .description("Walk to an exact block.")
            .currentXYZ()
            .executor(v -> start(new GoalBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("fly", "Fly")
            .description("Fly to an exact block.")
            .currentXYZ()
            .executor(v -> start(new GoalBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.FLY))
            .build(),
        GoalUiDefinition.builder("walkxz", "Walk XZ")
            .description("Walk to an X/Z column with long range support.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .executor(v -> start(new GoalXZ(v.get("x"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("near", "Near")
            .description("Walk near a block within a radius.")
            .currentXYZ()
            .parameter(GoalParameter.constant("radius", "Radius", 2))
            .executor(v -> start(new GoalNear(v.get("x"), v.get("y"), v.get("z"), v.get("radius")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("getto", "Get To Block")
            .description("Get to a reachable adjacent position for a block.")
            .currentXYZ()
            .executor(v -> start(new GoalGetToBlock(v.get("x"), v.get("y"), v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("y", "Y Level")
            .description("Walk until the target Y level is reached.")
            .parameter(GoalParameter.currentY())
            .executor(v -> start(new GoalYLevel(v.get("y")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("offset", "Offset")
            .description("Walk relative to the current position.")
            .parameter(GoalParameter.constant("dx", "dX", 0))
            .parameter(GoalParameter.constant("dy", "dY", 0))
            .parameter(GoalParameter.constant("dz", "dZ", 0))
            .executor(GoalUiCatalog::startOffset)
            .build(),
        GoalUiDefinition.builder("precise", "Precise")
            .description("Walk to a block with precise tolerance.")
            .currentXYZ()
            .executor(v -> GoalCommandSupport.startNavigation(NavigationRequest.builder(new GoalBlock(v.get("x"), v.get("y"), v.get("z")))
                .mode(NavigationModeType.WALK)
                .preciseGoalTolerance(0.1)
                .build()))
            .build(),
        GoalUiDefinition.builder("noreplan", "No Replan")
            .description("Walk without automatic replanning.")
            .currentXYZ()
            .executor(v -> GoalCommandSupport.startNavigation(NavigationRequest.builder(new GoalBlock(v.get("x"), v.get("y"), v.get("z")))
                .mode(NavigationModeType.WALK)
                .allowReplan(false)
                .build()))
            .build(),
        GoalUiDefinition.builder("axisx", "Axis X")
            .description("Reach a target X axis.")
            .parameter(GoalParameter.currentX())
            .executor(v -> start(new GoalAxisX(v.get("x")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("axisz", "Axis Z")
            .description("Reach a target Z axis.")
            .parameter(GoalParameter.currentZ())
            .executor(v -> start(new GoalAxisZ(v.get("z")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("column", "Column")
            .description("Reach an X/Z column.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .parameter(GoalParameter.constant("radius", "Radius", 1))
            .executor(v -> start(new GoalColumn(v.get("x"), v.get("z"), v.get("radius")), NavigationModeType.WALK))
            .build(),
        GoalUiDefinition.builder("rect", "Rectangle")
            .description("Reach any block inside an X/Z rectangle.")
            .parameter(GoalParameter.currentX())
            .parameter(GoalParameter.currentZ())
            .parameter(new GoalParameter("x2", "X2", minecraft -> currentBlockX(minecraft) + 8))
            .parameter(new GoalParameter("z2", "Z2", minecraft -> currentBlockZ(minecraft) + 8))
            .executor(GoalUiCatalog::startRectangle)
            .build(),
        GoalUiDefinition.builder("chunk", "Chunk")
            .description("Reach a chunk.")
            .parameter(new GoalParameter("chunkX", "Chunk X", minecraft -> currentBlockX(minecraft) >> 4))
            .parameter(new GoalParameter("chunkZ", "Chunk Z", minecraft -> currentBlockZ(minecraft) >> 4))
            .executor(v -> start(new GoalChunk(v.get("chunkX"), v.get("chunkZ")), NavigationModeType.WALK))
            .build()
    );
    private static final Map<String, GoalUiDefinition> BY_ID = GOALS.stream()
        .collect(Collectors.toUnmodifiableMap(GoalUiDefinition::id, Function.identity()));

    private GoalUiCatalog() {
    }

    public static List<GoalUiDefinition> all() {
        return GOALS;
    }

    public static GoalUiDefinition byId(String id) {
        GoalUiDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown goal UI definition: " + id);
        }
        return definition;
    }

    private static int start(fr.riege.ebsl.pathfinding.goal.Goal goal, NavigationModeType mode) {
        return GoalCommandSupport.startNavigation(NavigationRequest.builder(goal).mode(mode).build());
    }

    private static int startOffset(Map<String, Integer> v) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        return start(new GoalBlock(
            currentBlockX(minecraft) + v.get("dx"),
            currentBlockY(minecraft) + v.get("dy"),
            currentBlockZ(minecraft) + v.get("dz")), NavigationModeType.WALK);
    }

    private static int startRectangle(Map<String, Integer> v) {
        return start(new GoalRectangleXZ(
            Math.min(v.get("x"), v.get("x2")),
            Math.min(v.get("z"), v.get("z2")),
            Math.max(v.get("x"), v.get("x2")),
            Math.max(v.get("z"), v.get("z2"))), NavigationModeType.WALK);
    }

    private static int currentBlockX(Minecraft minecraft) {
        return minecraft.player != null ? (int) Math.floor(minecraft.player.getX()) : 0;
    }

    private static int currentBlockY(Minecraft minecraft) {
        return minecraft.player != null ? (int) Math.floor(minecraft.player.getY()) : 0;
    }

    private static int currentBlockZ(Minecraft minecraft) {
        return minecraft.player != null ? (int) Math.floor(minecraft.player.getZ()) : 0;
    }
}
