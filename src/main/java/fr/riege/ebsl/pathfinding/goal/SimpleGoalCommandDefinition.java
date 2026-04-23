package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.function.Supplier;

public record SimpleGoalCommandDefinition(
    String id,
    Supplier<LiteralArgumentBuilder<FabricClientCommandSource>> factory
) implements GoalCommandDefinition {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> command() {
        return factory.get();
    }
}
