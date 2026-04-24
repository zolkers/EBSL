package fr.riege.ebsl.pathfinding.pathfinder;

import fr.riege.ebsl.pathfinding.pathing.INeighborStrategy;
import fr.riege.ebsl.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.pathfinding.wrapper.PathVector;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public final class FlyPathfinder {

    private static final List<PathVector> OFFSETS_26;

    private static final int[][] FACE_OFFSETS = {
        { 1, 0, 0}, {-1, 0, 0},
        { 0, 1, 0}, { 0,-1, 0},
        { 0, 0, 1}, { 0, 0,-1}
    };

    private static final int[][] EDGE_OFFSETS = {
        { 1, 1, 0}, { 1,-1, 0}, {-1, 1, 0}, {-1,-1, 0},
        { 1, 0, 1}, { 1, 0,-1}, {-1, 0, 1}, {-1, 0,-1},
        { 0, 1, 1}, { 0, 1,-1}, { 0,-1, 1}, { 0,-1,-1}
    };

    static {
        List<PathVector> list = new ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        list.add(new PathVector(x, y, z));
                    }
                }
            }
        }
        OFFSETS_26 = List.copyOf(list);
    }

    private static final INeighborStrategy FLY_NEIGHBOUR_STRATEGY = () -> OFFSETS_26;

    private static final NavigationPoint AIR_BLOCK = new NavigationPoint() {
        @Override public boolean isTraversable() { return true;  }
        @Override public boolean hasFloor()      { return true;  }
        @Override public double  getFloorLevel() { return 0.0;  }
        @Override public boolean isClimbable()   { return false; }
        @Override public boolean isLiquid()      { return false; }
    };

    private static final NavigationPoint SOLID_BLOCK = new NavigationPoint() {
        @Override public boolean isTraversable() { return false; }
        @Override public boolean hasFloor()      { return false; }
        @Override public double  getFloorLevel() { return 0.0;  }
        @Override public boolean isClimbable()   { return false; }
        @Override public boolean isLiquid()      { return false; }
    };

    public CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        var mc = Minecraft.getInstance();
        var level = mc.level;

        Long2BooleanOpenHashMap blockCache = new Long2BooleanOpenHashMap(8192);

        BiFunction<BlockPos, Boolean, Boolean> checkSolid = (bp, dummy) -> {
            if (level == null) {
                return true;
            }
            long key = bp.asLong();
            if (blockCache.containsKey(key)) {
                return blockCache.get(key);
            }
            boolean solid = !level.getBlockState(bp).getCollisionShape(level, bp).isEmpty();
            blockCache.put(key, solid);
            return solid;
        };

        NavigationPointProvider flyProvider = (pos, env) -> {
            int x = pos.flooredX();
            int y = pos.flooredY();
            int z = pos.flooredZ();
            if (checkSolid.apply(new BlockPos(x, y, z), true)
                || checkSolid.apply(new BlockPos(x, y + 1, z), true)) {
                return SOLID_BLOCK;
            }
            return AIR_BLOCK;
        };

        NodeProcessor flyProcessor = new NodeProcessor() {
            @Override
            public boolean isValid(EvaluationContext context) {
                return context.getSearchContext().getNavigationPointProvider()
                    .getNavigationPoint(context.getCurrentPathPosition())
                    .isTraversable();
            }

            @Override
            public Cost calculateCostContribution(EvaluationContext context) {
                if (level == null) {
                    return Cost.ZERO;
                }
                PathPosition pos = context.getCurrentPathPosition();
                int cx = pos.flooredX();
                int cy = pos.flooredY();
                int cz = pos.flooredZ();
                double penalty = 0.0;

                for (int[] d : FACE_OFFSETS) {
                    if (checkSolid.apply(new BlockPos(cx + d[0], cy + d[1], cz + d[2]), true)) {
                        penalty += 8.0;
                    }
                }
                for (int[] d : EDGE_OFFSETS) {
                    if (checkSolid.apply(new BlockPos(cx + d[0], cy + d[1], cz + d[2]), true)) {
                        penalty += 3.0;
                    }
                }

                return penalty == 0.0 ? Cost.ZERO : Cost.of(Math.min(penalty, 60.0));
            }
        };

        PathfinderConfiguration config = PathfinderConfiguration.builder()
            .provider(flyProvider)
            .processors(List.of(flyProcessor))
            .neighborStrategy(FLY_NEIGHBOUR_STRATEGY)
            .heuristicWeights(new HeuristicWeights(0.0, 1.0, 0.0, 0.0))
            .maxIterations(25000)
            .maxLength(3000)
            .async(true)
            .fallback(true)
            .build();

        return new AStarPathfinder(config).findPath(start, target);
    }
}
