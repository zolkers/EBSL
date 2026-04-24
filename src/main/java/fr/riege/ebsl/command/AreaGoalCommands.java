package fr.riege.ebsl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.riege.ebsl.command.goal.GoalUiCatalog;
import fr.riege.ebsl.pathfinding.goal.GoalAxisX;
import fr.riege.ebsl.pathfinding.goal.GoalAxisZ;
import fr.riege.ebsl.pathfinding.goal.GoalChunk;
import fr.riege.ebsl.pathfinding.goal.GoalColumn;
import fr.riege.ebsl.pathfinding.goal.GoalRectangleXZ;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class AreaGoalCommands {
    private AreaGoalCommands() {
    }

    public static void register() {
        register("axisx", AreaGoalCommands::buildAxisXCommand);
        register("axisz", AreaGoalCommands::buildAxisZCommand);
        register("column", AreaGoalCommands::buildColumnCommand);
        register("rect", AreaGoalCommands::buildRectCommand);
        register("chunk", AreaGoalCommands::buildChunkCommand);
    }

    private static void register(String id, java.util.function.Supplier<LiteralArgumentBuilder<FabricClientCommandSource>> command) {
        GoalRegistry.register(new SimpleGoalCommandDefinition(id, command, GoalUiCatalog.byId(id)));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildAxisXCommand() {
        return ClientCommandManager.literal("axisx")
            .then(GoalCommandSupport.intArg("x")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalAxisX(GoalCommandSupport.getInt(ctx, "x")))
                    .mode(NavigationModeType.WALK)
                    .build())));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildAxisZCommand() {
        return ClientCommandManager.literal("axisz")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalAxisZ(GoalCommandSupport.getInt(ctx, "z")))
                    .mode(NavigationModeType.WALK)
                    .build())));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildColumnCommand() {
        return ClientCommandManager.literal("column")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> startColumn(ctx, 0.5))
            .then(GoalCommandSupport.boundedIntArg("radius", 1, 8)
            .executes(ctx -> startColumn(ctx, GoalCommandSupport.getInt(ctx, "radius"))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRectCommand() {
        return ClientCommandManager.literal("rect")
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
                    .build()))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildChunkCommand() {
        return ClientCommandManager.literal("chunk")
            .then(GoalCommandSupport.intArg("chunkX")
            .then(GoalCommandSupport.intArg("chunkZ")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalChunk(
                        GoalCommandSupport.getInt(ctx, "chunkX"),
                        GoalCommandSupport.getInt(ctx, "chunkZ")))
                    .mode(NavigationModeType.WALK)
                    .build()))));
    }

    private static int startColumn(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> ctx,
                                   double radius) {
        return GoalCommandSupport.startNavigation(
            NavigationRequest.builder(new GoalColumn(
                    GoalCommandSupport.getInt(ctx, "x"),
                    GoalCommandSupport.getInt(ctx, "z"),
                    radius))
                .mode(NavigationModeType.WALK)
                .build());
    }
}
