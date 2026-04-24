package fr.riege.ebsl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.riege.ebsl.command.goal.GoalUiCatalog;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.GoalGetToBlock;
import fr.riege.ebsl.pathfinding.goal.GoalNear;
import fr.riege.ebsl.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.pathfinding.goal.GoalYLevel;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class NavigationGoalCommands {
    private NavigationGoalCommands() {
    }

    public static void register() {
        register("walk", NavigationGoalCommands::buildWalkCommand);
        register("fly", NavigationGoalCommands::buildFlyCommand);
        register("walkxz", NavigationGoalCommands::buildWalkXzCommand);
        register("near", NavigationGoalCommands::buildNearCommand);
        register("getto", NavigationGoalCommands::buildGetToCommand);
        register("y", NavigationGoalCommands::buildYCommand);
        register("offset", NavigationGoalCommands::buildOffsetCommand);
        register("precise", NavigationGoalCommands::buildPreciseCommand);
        register("noreplan", NavigationGoalCommands::buildNoReplanCommand);
    }

    private static void register(String id, java.util.function.Supplier<LiteralArgumentBuilder<FabricClientCommandSource>> command) {
        GoalRegistry.register(new SimpleGoalCommandDefinition(id, command, GoalUiCatalog.byId(id)));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildWalkCommand() {
        return ClientCommandManager.literal("walk")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("y")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> startBlock(ctx, NavigationModeType.WALK)))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildFlyCommand() {
        return ClientCommandManager.literal("fly")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("y")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> startBlock(ctx, NavigationModeType.FLY)))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildWalkXzCommand() {
        return ClientCommandManager.literal("walkxz")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalXZ(
                        GoalCommandSupport.getInt(ctx, "x"),
                        GoalCommandSupport.getInt(ctx, "z")))
                    .mode(NavigationModeType.WALK)
                    .build()))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildNearCommand() {
        return ClientCommandManager.literal("near")
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
                    .build()))))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildGetToCommand() {
        return ClientCommandManager.literal("getto")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("y")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalGetToBlock(
                        GoalCommandSupport.getInt(ctx, "x"),
                        GoalCommandSupport.getInt(ctx, "y"),
                        GoalCommandSupport.getInt(ctx, "z")))
                    .mode(NavigationModeType.WALK)
                    .build())))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildYCommand() {
        return ClientCommandManager.literal("y")
            .then(GoalCommandSupport.intArg("targetY")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(new GoalYLevel(GoalCommandSupport.getInt(ctx, "targetY")))
                    .mode(NavigationModeType.WALK)
                    .build())));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildOffsetCommand() {
        return ClientCommandManager.literal("offset")
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
            }))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildPreciseCommand() {
        return ClientCommandManager.literal("precise")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("y")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(blockFromContext(ctx))
                    .mode(NavigationModeType.WALK)
                    .preciseGoalTolerance(0.1)
                    .build())))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildNoReplanCommand() {
        return ClientCommandManager.literal("noreplan")
            .then(GoalCommandSupport.intArg("x")
            .then(GoalCommandSupport.intArg("y")
            .then(GoalCommandSupport.intArg("z")
            .executes(ctx -> GoalCommandSupport.startNavigation(
                NavigationRequest.builder(blockFromContext(ctx))
                    .mode(NavigationModeType.WALK)
                    .allowReplan(false)
                    .build())))));
    }

    private static int startBlock(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> ctx,
                                  NavigationModeType mode) {
        return GoalCommandSupport.startNavigation(
            NavigationRequest.builder(blockFromContext(ctx))
                .mode(mode)
                .build());
    }

    private static GoalBlock blockFromContext(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> ctx) {
        return new GoalBlock(
            GoalCommandSupport.getInt(ctx, "x"),
            GoalCommandSupport.getInt(ctx, "y"),
            GoalCommandSupport.getInt(ctx, "z"));
    }
}
