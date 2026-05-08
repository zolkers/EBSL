package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.Node.MoveType;
import fr.riege.ebsl.common.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.pathfinder.heap.PrimitiveMinHeap;
import fr.riege.ebsl.common.pathfinding.pathfinder.processing.EvaluationContextImpl;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.util.RegionKey;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public final class AStarPathfinder extends AbstractPathfinder {
    private static final double[] BEST_PATH_COEFFICIENTS = {1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0};
    private static final double MIN_FALLBACK_DISTANCE = 5.0;
    private static final double MIN_FALLBACK_IMPROVEMENT = 0.01;

    private final ThreadLocal<PathfindingSession> currentSession = new ThreadLocal<>();

    
    private LongSet lastClosedSet = new LongOpenHashSet();
    private long    exploredCount = 0;

    
    private long profNeighborCount     = 0;
    private long profIsValidNanos      = 0;
    private long profCostCalcNanos     = 0;
    private long profNodeCreateNanos   = 0;
    private long profHeapNanos         = 0;
    private long profIsValidRejects    = 0;
    private long profGCostRejects      = 0;
    private boolean profiling;
    private boolean captureClosedSet;

    public AStarPathfinder(PathfinderConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void insertStartNode(Node node, double fCost, PrimitiveMinHeap openSet) {
        PathfindingSession session = sessionOrThrow();
        long packedPos = RegionKey.pack(node.position);
        node.inOpen = true;
        node.cachedFCost = fCost;
        session.nodes.put(packedPos, node);
        session.initializeFallback(node);
        openSet.insertOrUpdate(packedPos, fCost);
    }

    @Override
    protected Node extractBestNode(PrimitiveMinHeap openSet) {
        PathfindingSession session = sessionOrThrow();
        long packedPos = openSet.extractMin();
        return session.nodes.get(packedPos);
    }

    @Override
    protected void initializeSearch() {
        currentSession.set(new PathfindingSession());
        profNeighborCount = 0;
        profIsValidNanos = 0;
        profCostCalcNanos = 0;
        profNodeCreateNanos = 0;
        profHeapNanos = 0;
        profIsValidRejects = 0;
        profGCostRejects = 0;
        profiling = pathfinderConfiguration.profiling;
        captureClosedSet = PathVisualizer.isEnabled();
    }

    @Override
    protected Node processSuccessors(PathPosition requestStart, PathPosition requestTarget,
                                     Node currentNode, PrimitiveMinHeap openSet,
                                     SearchContext searchContext) {
        PathfindingSession session = sessionOrThrow();
        Iterable<PathVector> offsets = neighborStrategy.getOffsets(currentNode.position);

        for (PathVector offset : offsets) {
            if (profiling) profNeighborCount++;
            PathPosition neighborPos = currentNode.position.add(offset);
            long          packedPos  = RegionKey.pack(neighborPos);

            Node candidate = session.nodes.get(packedPos);
            if (candidate == null) {
                long t0 = profiling ? System.nanoTime() : 0L;
                candidate = createNeighborNode(neighborPos, requestStart, requestTarget, currentNode);
                candidate.moveType = inferMoveType(offset);
                
                candidate.gCost = Double.POSITIVE_INFINITY;
                session.nodes.put(packedPos, candidate);
                if (profiling) profNodeCreateNanos += System.nanoTime() - t0;
            }

            
            if (candidate.inClosed) continue;

            
            session.reusableContext.update(searchContext, candidate, currentNode,
                    pathfinderConfiguration.heuristicStrategy);

            long t1 = profiling ? System.nanoTime() : 0L;
            boolean valid = isValidByProcessors(session.reusableContext);
            if (profiling) profIsValidNanos += System.nanoTime() - t1;
            if (!valid) {
                if (profiling) profIsValidRejects++;
                continue;
            }

            long t2 = profiling ? System.nanoTime() : 0L;
            double gCost = calculateGCost(session.reusableContext);
            if (profiling) profCostCalcNanos += System.nanoTime() - t2;

            
            if (Double.isFinite(candidate.gCost)
                    && gCost + gTolerance(gCost, candidate.gCost) >= candidate.gCost) {
                if (profiling) profGCostRejects++;
                continue;
            }

            candidate.parent = currentNode;
            candidate.gCost = gCost;
            candidate.moveType = inferMoveType(offset);
            session.recordFallbackCandidate(candidate);
            if (candidate.isTarget(requestTarget)) {
                return candidate;
            }
            double fCost   = candidate.fCost();
            candidate.cachedFCost = fCost;
            double heapKey = calculateHeapKey(candidate, fCost);

            long t3 = profiling ? System.nanoTime() : 0L;
            openSet.insertOrUpdate(packedPos, heapKey);
            candidate.inOpen = true;
            if (profiling) profHeapNanos += System.nanoTime() - t3;
        }
        return null;
    }

    @Override
    protected Node bestFallbackNode(Node defaultFallback) {
        PathfindingSession session = currentSession.get();
        return session == null ? defaultFallback : session.bestFallback(defaultFallback);
    }

    private static double gTolerance(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) return 0.0;
        return Math.ulp(Math.max(Math.abs(a), Math.abs(b)));
    }

    private Node createNeighborNode(PathPosition position, PathPosition start,
                                     PathPosition target, Node parent) {
        return new Node(position, start, target,
                pathfinderConfiguration.heuristicWeights,
                pathfinderConfiguration.heuristicStrategy,
                parent.depth + 1);
    }

    private boolean isValidByProcessors(EvaluationContext context) {
        for (var p : processors) {
            if (!p.isValid(context)) return false;
        }
        return true;
    }

    private double calculateGCost(EvaluationContext context) {
        double baseCost       = context.getBaseTransitionCost();
        
        double additionalCost = 0.0;
        for (var p : processors) {
            additionalCost += p.calculateCostContribution(context).value;
        }
        double transitionCost = Math.max(0.0, baseCost + additionalCost);
        return context.getPathCostToPreviousPosition() + transitionCost;
    }

    @Override
    protected void markNodeAsExpanded(Node node) {
        PathfindingSession session = sessionOrThrow();
        node.inOpen   = false;
        node.inClosed = true;
        session.expandedCount++;
        if (captureClosedSet) {
            session.closedSet.add(RegionKey.pack(node.position));
        }
    }

    @Override
    protected void performAlgorithmCleanup() {
        PathfindingSession session = currentSession.get();
        if (session != null) {
            exploredCount = session.expandedCount;
            if (captureClosedSet) {
                lastClosedSet = new LongOpenHashSet(session.closedSet);
            } else {
                lastClosedSet = new LongOpenHashSet();
            }
        }
        currentSession.remove();
    }

    
    public LongSet getClosedSet()    { return lastClosedSet; }
    public long    getExploredCount(){ return exploredCount; }

    
    public String getProfilingReport() {
        if (!profiling) return "profiling disabled";
        return String.format(
                "neighbors=%d | isValid=%.0fms (rejects=%d) | costCalc=%.0fms | nodeCreate=%.0fms | heap=%.0fms | gRejects=%d",
                profNeighborCount,
                profIsValidNanos / 1e6, profIsValidRejects,
                profCostCalcNanos / 1e6,
                profNodeCreateNanos / 1e6,
                profHeapNanos / 1e6,
                profGCostRejects);
    }

    private PathfindingSession sessionOrThrow() {
        PathfindingSession s = currentSession.get();
        if (s == null) throw new IllegalStateException(
                "Pathfinding session not initialized. Call initializeSearch() first.");
        return s;
    }

    
    private static MoveType inferMoveType(PathVector offset) {
        int dy = (int) offset.y;
        int dx = Math.abs((int) offset.x);
        int dz = Math.abs((int) offset.z);

        if (ParkourGeometry.isCandidateOffset((int) offset.x, (int) offset.z)) return MoveType.PARKOUR;
        if (dy > 0)           return MoveType.STEP_UP;
        if (dy < -1)          return MoveType.FALL;
        if (dy < 0)           return MoveType.FALL;
        if (dx + dz >= 2)     return MoveType.WALK_DIAGONAL;
        return MoveType.WALK;
    }

    





    private static final class PathfindingSession {
        final Long2ObjectOpenHashMap<Node> nodes      = new Long2ObjectOpenHashMap<>();
        final LongSet                      closedSet  = new LongOpenHashSet();
        final EvaluationContextImpl        reusableContext;
        final Node[]                       bestFallbackNodes = new Node[BEST_PATH_COEFFICIENTS.length];
        final double[]                     bestFallbackScores = new double[BEST_PATH_COEFFICIENTS.length];
        int                                expandedCount = 0;

        PathfindingSession() {
            
            reusableContext = new EvaluationContextImpl(null, null, null, null);
        }

        void initializeFallback(Node start) {
            for (int i = 0; i < BEST_PATH_COEFFICIENTS.length; i++) {
                bestFallbackNodes[i] = start;
                bestFallbackScores[i] = fallbackScore(start, BEST_PATH_COEFFICIENTS[i]);
            }
        }

        void recordFallbackCandidate(Node node) {
            if (node == null || !Double.isFinite(node.gCost)) {
                return;
            }
            for (int i = 0; i < BEST_PATH_COEFFICIENTS.length; i++) {
                double score = fallbackScore(node, BEST_PATH_COEFFICIENTS[i]);
                if (bestFallbackNodes[i] == null || bestFallbackScores[i] - score > MIN_FALLBACK_IMPROVEMENT) {
                    bestFallbackNodes[i] = node;
                    bestFallbackScores[i] = score;
                }
            }
        }

        Node bestFallback(Node defaultFallback) {
            Node best = defaultFallback;
            double bestDistance = distanceFromRootSquared(defaultFallback);
            double minDistanceSquared = MIN_FALLBACK_DISTANCE * MIN_FALLBACK_DISTANCE;
            for (Node candidate : bestFallbackNodes) {
                double distance = distanceFromRootSquared(candidate);
                if (distance >= minDistanceSquared) {
                    return candidate;
                }
                if (distance > bestDistance) {
                    best = candidate;
                    bestDistance = distance;
                }
            }
            return best;
        }

        private static double fallbackScore(Node node, double coefficient) {
            return node.heuristic + node.gCost / coefficient;
        }

        private static double distanceFromRootSquared(Node node) {
            if (node == null) {
                return 0.0;
            }
            Node root = node;
            while (root.parent != null) {
                root = root.parent;
            }
            double dx = node.position.x - root.position.x;
            double dy = node.position.y - root.position.y;
            double dz = node.position.z - root.position.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
