package fr.riege.ebsl.pathfinding.goal;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import net.minecraft.client.Minecraft;

public final class GoalRequestHandlers {
    private GoalRequestHandlers() {
    }

    public static void bootstrap() {
        if (!GoalRequestHandlerRegistry.isEmpty()) {
            return;
        }

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalBlock.class,
            (mc, goal, request) -> PathfindingManager.startBlockGoal(mc, goal, request)));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalXZ.class,
            (mc, goal, request) -> PathfindingManager.startXZGoal(mc, goal, request)));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalNear.class,
            (mc, goal, request) -> PathfindingManager.startBlockGoal(
                mc,
                new GoalBlock(goal.x(), goal.y(), goal.z()),
                translated(request, new GoalBlock(goal.x(), goal.y(), goal.z()))
                    .preciseGoalTolerance(Math.max(goal.radius(), request.preciseGoalTolerance()))
                    .build())));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalYLevel.class,
            (mc, goal, request) -> {
                if (mc.player == null) {
                    return;
                }
                GoalBlock resolved = new GoalBlock(
                    (int) Math.floor(mc.player.getX()),
                    goal.y(),
                    (int) Math.floor(mc.player.getZ()));
                PathfindingManager.startBlockGoal(mc, resolved, translated(request, resolved).build());
            }));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalGetToBlock.class,
            (mc, goal, request) -> GoalRequestHandlerRegistry.start(
                mc,
                translated(request, new GoalNear(goal.x(), goal.y() + 1, goal.z(), 1.25))
                    .preciseGoalTolerance(Math.max(1.25, request.preciseGoalTolerance()))
                    .build())));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalAxisX.class,
            (mc, goal, request) -> {
                if (mc.player == null) {
                    return;
                }
                GoalRequestHandlerRegistry.start(
                    mc,
                    translated(request, new GoalXZ(goal.x(), (int) Math.floor(mc.player.getZ()))).build());
            }));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalAxisZ.class,
            (mc, goal, request) -> {
                if (mc.player == null) {
                    return;
                }
                GoalRequestHandlerRegistry.start(
                    mc,
                    translated(request, new GoalXZ((int) Math.floor(mc.player.getX()), goal.z())).build());
            }));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalColumn.class,
            (mc, goal, request) -> GoalRequestHandlerRegistry.start(
                mc,
                translated(request, new GoalXZ(goal.x(), goal.z()))
                    .preciseGoalTolerance(Math.max(goal.radius(), request.preciseGoalTolerance()))
                    .build())));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalRectangleXZ.class,
            (mc, goal, request) -> {
                if (mc.player == null) {
                    return;
                }
                int resolvedX = clamp((int) Math.floor(mc.player.getX()), goal.minX(), goal.maxX());
                int resolvedZ = clamp((int) Math.floor(mc.player.getZ()), goal.minZ(), goal.maxZ());
                GoalRequestHandlerRegistry.start(mc, translated(request, new GoalXZ(resolvedX, resolvedZ)).build());
            }));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalChunk.class,
            (mc, goal, request) -> GoalRequestHandlerRegistry.start(mc, translated(request, goal.asRectangle()).build())));

        GoalRequestHandlerRegistry.register(new SimpleGoalRequestHandler<>(GoalCompositeAny.class,
            (mc, composite, request) -> {
                if (mc.player == null) {
                    return;
                }
                Goal best = composite.goals().stream()
                    .min(java.util.Comparator.comparingDouble(goal ->
                        goal.heuristic(
                            (int) Math.floor(mc.player.getX()),
                            (int) Math.floor(mc.player.getY()),
                            (int) Math.floor(mc.player.getZ()))))
                    .orElseThrow();
                GoalRequestHandlerRegistry.start(mc, translated(request, best).build());
            }));
    }

    private static NavigationRequest.Builder translated(NavigationRequest request, Goal goal) {
        return NavigationRequest.builder(goal)
            .mode(request.mode())
            .allowReplan(request.allowReplan())
            .allowParkour(request.allowParkour())
            .allowRotation(request.allowRotation())
            .allowSneak(request.allowSneak())
            .preciseGoalTolerance(request.preciseGoalTolerance())
            .onFinished(request.onFinished())
            .onFailed(request.onFailed());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
