package fr.riege.ebsl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.riege.ebsl.command.goal.GoalUiDefinition;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Optional;

public interface GoalCommandDefinition {
    String id();

    LiteralArgumentBuilder<FabricClientCommandSource> command();

    default Optional<GoalUiDefinition> uiDefinition() {
        return Optional.empty();
    }
}
