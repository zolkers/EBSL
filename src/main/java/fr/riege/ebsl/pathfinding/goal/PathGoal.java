package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface PathGoal {
    LiteralArgumentBuilder<FabricClientCommandSource> command();
}
