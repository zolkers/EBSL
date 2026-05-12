package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.aim.BlockAimTarget;
import fr.riege.ebsl.common.feature.aim.BlockAimTargeting;
import fr.riege.ebsl.common.feature.aim.BlockInteractionTarget;
import fr.riege.ebsl.common.feature.aim.BlockInteractionTargeting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.math.Vec3d;

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
        String target = invocation.runtime().value(invocation.arg(0)).toString();
        int searchRadius = (int) invocation.runtime().argNumber(invocation.args(), 1, 32.0);
        int reachRadius = (int) invocation.runtime().argNumber(invocation.args(), 2, 4.0);
        var targeted = invocation.runtime().platform().player().targetedBlockHit();
        if (targeted != null
            && !invocation.runtime().platform().world().isAir(targeted.x(), targeted.y(), targeted.z())
            && BlockAimTargeting.matches(targeted.block(), target)) {
            return 0;
        }
        BlockInteractionTarget interactionTarget = BlockInteractionTargeting.nearestReachable(
            invocation.runtime().platform(),
            target,
            Math.max(1, searchRadius),
            Math.max(1, reachRadius)
        );
        if (interactionTarget != null) {
            PathPosition standing = interactionTarget.standingPosition();
            if (isAlreadyAt(invocation.runtime().platform().player().position(), standing)) {
                return 0;
            }
            invocation.runtime().navigation().startNavigation(
                NavigationRequest.builder(new GoalBlock(standing.flooredX(), standing.flooredY(), standing.flooredZ()))
                    .mode(NavigationModeType.WALK)
                    .allowReplan(false)
                    .build());
            return 0;
        }
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

    private static boolean isAlreadyAt(Vec3d position, PathPosition standing) {
        return (int) Math.floor(position.x()) == standing.flooredX()
            && (int) Math.floor(position.y()) == standing.flooredY()
            && (int) Math.floor(position.z()) == standing.flooredZ();
    }
}
