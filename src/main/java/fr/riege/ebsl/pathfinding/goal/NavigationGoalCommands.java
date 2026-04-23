package fr.riege.ebsl.pathfinding.goal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

public final class NavigationGoalCommands {
    private NavigationGoalCommands() {
    }

    public static void register() {
        GoalRegistry.register(new SimpleGoalCommandDefinition("walk", () ->
            ClientCommandManager.literal("walk")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalBlock(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .build())))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("fly", () ->
            ClientCommandManager.literal("fly")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalBlock(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.FLY)
                        .build())))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("walkxz", () ->
            ClientCommandManager.literal("walkxz")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalXZ(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .build()))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("near", () ->
            ClientCommandManager.literal("near")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .then(GoalCommandSupport.boundedIntArg("radius", 1, 8)
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalNear(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z"),
                            GoalCommandSupport.getInt(ctx, "radius")))
                        .mode(NavigationModeType.WALK)
                        .build()))))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("getto", () ->
            ClientCommandManager.literal("getto")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalGetToBlock(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .build())))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("y", () ->
            ClientCommandManager.literal("y")
                .then(GoalCommandSupport.intArg("targetY")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalYLevel(GoalCommandSupport.getInt(ctx, "targetY")))
                        .mode(NavigationModeType.WALK)
                        .build())))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("offset", () ->
            ClientCommandManager.literal("offset")
                .then(GoalCommandSupport.intArg("dx")
                .then(GoalCommandSupport.intArg("dy")
                .then(GoalCommandSupport.intArg("dz")
                .executes(ctx -> {
                    if (GoalCommandSupport.minecraft().player == null) {
                        return 0;
                    }
                    return GoalCommandSupport.startNavigation(
                        NavigationRequest.builder(new GoalBlock(
                                GoalCommandSupport.currentBlockX() + GoalCommandSupport.getInt(ctx, "dx"),
                                GoalCommandSupport.currentBlockY() + GoalCommandSupport.getInt(ctx, "dy"),
                                GoalCommandSupport.currentBlockZ() + GoalCommandSupport.getInt(ctx, "dz")))
                            .mode(NavigationModeType.WALK)
                            .build());
                }))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("precise", () ->
            ClientCommandManager.literal("precise")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalBlock(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .preciseGoalTolerance(0.1)
                        .build())))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("noreplan", () ->
            ClientCommandManager.literal("noreplan")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> GoalCommandSupport.startNavigation(
                    NavigationRequest.builder(new GoalBlock(
                            GoalCommandSupport.getInt(ctx, "x"),
                            GoalCommandSupport.getInt(ctx, "y"),
                            GoalCommandSupport.getInt(ctx, "z")))
                        .mode(NavigationModeType.WALK)
                        .allowReplan(false)
                        .build())))))));
    }
}
