package fr.riege.ebsl.pathfinding.goal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

public final class AreaGoalCommands {
    private AreaGoalCommands() {
    }

    public static void register() {
        GoalRegistry.register(new SimpleGoalCommandDefinition("axisx", () ->
            ClientCommandManager.literal("axisx")
                .then(GoalCommandSupport.intArg("x")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalAxisX(GoalCommandSupport.getInt(ctx, "x")))
                        .mode(NavigationModeType.WALK)
                        .build())))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("axisz", () ->
            ClientCommandManager.literal("axisz")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalAxisZ(GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .build())))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("column", () ->
            ClientCommandManager.literal("column")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalColumn(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "z"),
                            0.5))
                        .mode(NavigationModeType.WALK)
                        .build()))
                .then(GoalCommandSupport.boundedIntArg("radius", 1, 8)
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalColumn(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "z"),
                            GoalCommandSupport.getInt(ctx, "radius")))
                        .mode(NavigationModeType.WALK)
                        .build())))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("rect", () ->
            ClientCommandManager.literal("rect")
                .then(GoalCommandSupport.intArg("x1")
                .then(GoalCommandSupport.intArg("z1")
                .then(GoalCommandSupport.intArg("x2")
                .then(GoalCommandSupport.intArg("z2")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalRectangleXZ(
                            Math.min(GoalCommandSupport.getInt(ctx, "x1"), GoalCommandSupport.getInt(ctx, "x2")),
                            Math.min(GoalCommandSupport.getInt(ctx, "z1"), GoalCommandSupport.getInt(ctx, "z2")),
                            Math.max(GoalCommandSupport.getInt(ctx, "x1"), GoalCommandSupport.getInt(ctx, "x2")),
                            Math.max(GoalCommandSupport.getInt(ctx, "z1"), GoalCommandSupport.getInt(ctx, "z2"))))
                        .mode(NavigationModeType.WALK)
                        .build()))))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("chunk", () ->
            ClientCommandManager.literal("chunk")
                .then(GoalCommandSupport.intArg("chunkX")
                .then(GoalCommandSupport.intArg("chunkZ")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalChunk(
                            GoalCommandSupport.getInt(ctx, "chunkX"),
                            GoalCommandSupport.getInt(ctx, "chunkZ")))
                        .mode(NavigationModeType.WALK)
                        .build()))))));
    }
}
