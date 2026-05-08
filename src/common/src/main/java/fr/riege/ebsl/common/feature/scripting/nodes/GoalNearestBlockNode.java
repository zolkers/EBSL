package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.GoalNear;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;

@EbslNodeDefinition(value = EbslNodeType.GOAL_NEAREST_BLOCK, aliases = {"nearest_block", "find_block"})
public final class GoalNearestBlockNode extends NavigationNode {
    private final StringSetting blockId = registerSetting(new StringSetting("block_id", "Block", "minecraft:oak_leaves"));
    private final IntSetting searchRadius = registerSetting(new IntSetting("search_radius", "Search Radius", 32, 1, 128));
    private final IntSetting reachRadius = registerSetting(new IntSetting("reach_radius", "Reach Radius", 2, 1, 12));

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return 0;
        }
        String target = invocation.arg(0);
        int searchRadius = (int) invocation.runtime().argNumber(invocation.args(), 1, 32.0);
        int reachRadius = (int) invocation.runtime().argNumber(invocation.args(), 2, 2.0);
        FoundBlock block = nearest(invocation, target, Math.max(1, searchRadius));
        if (block == null) {
            return 0;
        }
        invocation.runtime().navigation().startNavigation(
            NavigationRequest.builder(new GoalNear(block.x(), block.y() + 1, block.z(), Math.max(1, reachRadius)))
                .mode(NavigationModeType.WALK)
                .allowReplan(true)
                .build());
        return 0;
    }

    private FoundBlock nearest(EbslNodeInvocation invocation, String target, int radius) {
        IWorldLayer world = invocation.runtime().platform().world();
        Vec3d pos = invocation.runtime().platform().player().position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        FoundBlock best = null;
        double bestDistance = Double.MAX_VALUE;
        int radiusSquared = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            int y = py + dy;
            for (int dx = -radius; dx <= radius; dx++) {
                int x = px + dx;
                for (int dz = -radius; dz <= radius; dz++) {
                    int distanceSquared = dx * dx + dy * dy + dz * dz;
                    if (distanceSquared > radiusSquared) {
                        continue;
                    }
                    int z = pz + dz;
                    if (!world.isLoaded(x, y, z)) {
                        continue;
                    }
                    BlockId id = world.getBlock(x, y, z);
                    if (!matches(id, target)) {
                        continue;
                    }
                    if (distanceSquared < bestDistance) {
                        bestDistance = distanceSquared;
                        best = new FoundBlock(x, y, z);
                    }
                }
            }
        }
        return best;
    }

    private boolean matches(BlockId id, String target) {
        if (id == null || target == null || target.isBlank()) {
            return false;
        }
        String normalized = target.toLowerCase().replace('\\', '/');
        String exact = id.toString().toLowerCase();
        if (exact.equals(normalized)) {
            return true;
        }
        String path = id.path().toLowerCase();
        return !normalized.contains(":") && (path.equals(normalized) || path.endsWith("_" + normalized));
    }

    private record FoundBlock(int x, int y, int z) {
    }
}
