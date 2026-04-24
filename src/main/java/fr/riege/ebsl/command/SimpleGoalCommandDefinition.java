package fr.riege.ebsl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.riege.ebsl.command.goal.GoalUiDefinition;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Optional;
import java.util.function.Supplier;

public record SimpleGoalCommandDefinition(
    String id,
    Supplier<LiteralArgumentBuilder<FabricClientCommandSource>> factory,
    GoalUiDefinition uiDefinitionValue
) implements GoalCommandDefinition {
    public SimpleGoalCommandDefinition(String id, Supplier<LiteralArgumentBuilder<FabricClientCommandSource>> factory) {
        this(id, factory, null);
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> command() {
        return factory.get();
    }

    @Override
    public Optional<GoalUiDefinition> uiDefinition() {
        return Optional.ofNullable(uiDefinitionValue);
    }
}
