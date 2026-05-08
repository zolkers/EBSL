package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.aim.BlockAimTarget;
import fr.riege.ebsl.common.feature.aim.BlockAimTargeting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;

@EbslNodeDefinition(value = EbslNodeType.GOAL_NEAREST_BLOCK, aliases = {"nearest_block", "find_block"})
public final class GoalNearestBlockNode extends NavigationNode {
    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("block_id", "Block", "minecraft:oak_leaves"));
        registerSetting(new IntSetting("search_radius", "Search Radius", 32, 1, 128));
        registerSetting(new IntSetting("reach_radius", "Reach Radius", 4, 1, 12));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return 0;
        }
        String target = invocation.arg(0);
        int searchRadius = (int) invocation.runtime().argNumber(invocation.args(), 1, 32.0);
        int reachRadius = (int) invocation.runtime().argNumber(invocation.args(), 2, 4.0);
        BlockAimTarget block = BlockAimTargeting.nearest(invocation.runtime().platform(), target, Math.max(1, searchRadius), false);
        if (block == null) {
            return 0;
        }
        invocation.runtime().navigation().startNavigation(
            NavigationRequest.builder(new GoalNear(block.x(), block.y(), block.z(), Math.max(1, reachRadius)))
                .mode(NavigationModeType.WALK)
                .allowReplan(true)
                .build());
        return 0;
    }
}
