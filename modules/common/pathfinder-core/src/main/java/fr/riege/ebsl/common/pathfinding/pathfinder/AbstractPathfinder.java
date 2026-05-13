package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.core.threading.EbslThreading;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathfinder.heap.PrimitiveMinHeap;
import fr.riege.ebsl.common.pathfinding.pathfinder.processing.EvaluationContextImpl;
import fr.riege.ebsl.common.pathfinding.pathfinder.processing.SearchContextImpl;
import fr.riege.ebsl.common.pathfinding.pathing.INeighborStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.Pathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.pathing.result.Path;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityContext;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityRegistry;
import fr.riege.ebsl.common.pathfinding.result.PathImpl;
import fr.riege.ebsl.common.pathfinding.result.PathfinderResultImpl;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractPathfinder implements Pathfinder {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-pathfinder");

    private static final Set<PathPosition> EMPTY_PATH_POSITIONS = LinkedHashSet.newLinkedHashSet(0);
    private static final double TIE_BREAKER_WEIGHT = 1e-6;

    protected final PathfinderConfiguration pathfinderConfiguration;
    protected final NavigationPointProvider  navigationPointProvider;
    protected final List<NodeProcessor>      processors;
    protected final INeighborStrategy        neighborStrategy;

    private final AtomicBoolean abortRequested = new AtomicBoolean(false);

    protected AbstractPathfinder(PathfinderConfiguration configuration) {
        this.pathfinderConfiguration = configuration;
        this.navigationPointProvider = configuration.provider;
        this.processors              = configuration.processors;
        this.neighborStrategy        = configuration.neighborStrategy;
    }

    @Override
    public CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                                       EnvironmentContext context) {
        abortRequested.set(false);
        return initiatePathing(start, target, context);
    }

    @Override
    public void abort() {
        abortRequested.set(true);
    }

    private CompletionStage<PathfinderResult> initiatePathing(PathPosition start, PathPosition target,
                                                               EnvironmentContext environmentContext) {
        PathPosition effectiveStart  = start.floor();
        PathPosition effectiveTarget = target.floor();

        if (pathfinderConfiguration.async) {
            return EbslThreading.pathfinding()
                    .supply("path:" + effectiveStart + "->" + effectiveTarget,
                            () -> executePathingAlgorithm(effectiveStart, effectiveTarget, environmentContext))
                    .exceptionally(t -> handlePathingException(start, target, t));
        } else {
            try {
                return CompletableFuture.completedFuture(
                        executePathingAlgorithm(effectiveStart, effectiveTarget, environmentContext));
            } catch (Exception e) {
                return CompletableFuture.completedFuture(handlePathingException(start, target, e));
            }
        }
    }

    @SuppressWarnings({"java:S3776", "java:S6541"})
    private PathfinderResult executePathingAlgorithm(PathPosition start, PathPosition target,
                                                      EnvironmentContext environmentContext) {
        initializeSearch();
        long startedAtNanos = System.nanoTime();

        SearchContext searchContext = new SearchContextImpl(
                start, target, pathfinderConfiguration, navigationPointProvider, environmentContext);

        try {
            for (NodeProcessor p : processors) p.initializeSearch(searchContext);

            Node startNode = createStartNode(start, target);
            var  startCtx  = new EvaluationContextImpl(searchContext, startNode, null,
                    pathfinderConfiguration.heuristicStrategy);

            boolean startValid = true;
            for (NodeProcessor processor : processors) {
                if (!processor.isValid(startCtx)) {
                    startValid = false;
                    break;
                }
            }
            if (!startValid) {
                return quality(new PathfinderResultImpl(PathState.FAILED,
                        new PathImpl(start, target, EMPTY_PATH_POSITIONS)));
            }

            PrimitiveMinHeap openSet = new PrimitiveMinHeap(1024);
            double startKey = calculateHeapKey(startNode, startNode.fCost());
            insertStartNode(startNode, startKey, openSet);

            int  currentDepth    = 0;
            Node bestFallbackNode = startNode;
            int earlyFallbackCheckEvery = earlyFallbackCheckInterval();

            while (!openSet.isEmpty() && currentDepth < pathfinderConfiguration.maxIterations) {
                currentDepth++;

                if (abortRequested.get()) {
                    return createAbortedResult(start, target, bestFallbackNode);
                }

                Node currentNode = extractBestNode(openSet);
                markNodeAsExpanded(currentNode);

                if (currentNode.heuristic < bestFallbackNode.heuristic) {
                    bestFallbackNode = currentNode;
                }

                if (hasReachedPathLengthLimit(currentNode)) {
                    return quality(new PathfinderResultImpl(PathState.LENGTH_LIMITED,
                            reconstructPath(start, target, currentNode)));
                }

                if (currentNode.isTarget(target)) {
                    return quality(new PathfinderResultImpl(PathState.FOUND,
                            reconstructPath(start, target, currentNode)));
                }

                Node reachedTarget = processSuccessors(start, target, currentNode, openSet, searchContext);
                if (reachedTarget != null) {
                    return quality(new PathfinderResultImpl(PathState.FOUND,
                            reconstructPath(start, target, reachedTarget)));
                }

                Node earlyFallbackNode = bestFallbackNode(bestFallbackNode);
                if (shouldReturnEarlyFallback(currentDepth, earlyFallbackCheckEvery, startedAtNanos, startNode,
                        earlyFallbackNode)) {
                    return quality(new PathfinderResultImpl(PathState.FALLBACK,
                            reconstructPath(start, target, earlyFallbackNode)));
                }
                if (shouldReturnTimeBudgetFallback(startedAtNanos, startNode, earlyFallbackNode)) {
                    return quality(new PathfinderResultImpl(PathState.FALLBACK,
                            reconstructPath(start, target, earlyFallbackNode)));
                }
            }

            return determinePostLoopResult(currentDepth, start, target, bestFallbackNode(bestFallbackNode));

        } catch (Exception e) {
            LOGGER.error("Pathfinding failed from {} to {}", start, target, e);
            return quality(new PathfinderResultImpl(PathState.FAILED,
                    new PathImpl(start, target, EMPTY_PATH_POSITIONS)));
        } finally {
            for (NodeProcessor p : processors) {
                try {
                    p.finalizeSearch(searchContext);
                } catch (Exception exception) {
                    LOGGER.debug("Node processor cleanup failed for {}", p.getClass().getSimpleName(), exception);
                }
            }
            performAlgorithmCleanup();
        }
    }

    protected double calculateHeapKey(Node neighbor, double fCost) {
        double heuristic  = neighbor.heuristic;
        double tieBreaker = TIE_BREAKER_WEIGHT * (heuristic / (Math.abs(fCost) + 1));
        double heapKey    = fCost - tieBreaker;
        if (Double.isNaN(heapKey) || Double.isInfinite(heapKey)) heapKey = fCost;
        return heapKey;
    }

    private PathfinderResult createAbortedResult(PathPosition start, PathPosition target, Node fallbackNode) {
        abortRequested.set(false);
        return quality(new PathfinderResultImpl(PathState.ABORTED, reconstructPath(start, target, fallbackNode)));
    }

    private PathfinderResult handlePathingException(PathPosition start, PathPosition target,
                                                     Throwable throwable) {
        LOGGER.error("Pathfinding task failed from {} to {}", start, target, throwable);
        return quality(new PathfinderResultImpl(PathState.FAILED,
                new PathImpl(start, target, EMPTY_PATH_POSITIONS)));
    }

    protected Node createStartNode(PathPosition startPos, PathPosition targetPos) {
        return new Node(startPos, startPos, targetPos,
                pathfinderConfiguration.heuristicWeights,
                pathfinderConfiguration.heuristicStrategy, 0);
    }

    private boolean hasReachedPathLengthLimit(Node node) {
        return pathfinderConfiguration.maxLength > 0
                && node.depth >= pathfinderConfiguration.maxLength;
    }

    private int earlyFallbackCheckInterval() {
        int configured = pathfinderConfiguration.earlyFallbackIterations;
        if (configured <= 0) return 32;
        return Math.max(8, configured / 4);
    }

    private boolean shouldReturnEarlyFallback(int currentDepth, int checkEvery, long startedAtNanos,
                                              Node startNode, Node fallbackNode) {
        if (!pathfinderConfiguration.fallback || !pathfinderConfiguration.earlyFallback || fallbackNode == null) {
            return false;
        }
        if (currentDepth % checkEvery != 0 && !hasExceededCalculationTime(startedAtNanos)) {
            return false;
        }
        if (currentDepth < pathfinderConfiguration.earlyFallbackIterations
                && !hasExceededCalculationTime(startedAtNanos)) {
            return false;
        }
        return isUsableEarlyFallback(startNode, fallbackNode);
    }

    private boolean hasExceededCalculationTime(long startedAtNanos) {
        long budgetMs = pathfinderConfiguration.maxCalculationTimeMs;
        return budgetMs > 0L && (System.nanoTime() - startedAtNanos) >= budgetMs * 1_000_000L;
    }

    private boolean shouldReturnTimeBudgetFallback(long startedAtNanos, Node startNode, Node fallbackNode) {
        return pathfinderConfiguration.fallback
                && hasExceededCalculationTime(startedAtNanos)
                && fallbackNode != null
                && fallbackNode != startNode
                && fallbackNode.depth > 0;
    }

    private boolean isUsableEarlyFallback(Node startNode, Node fallbackNode) {
        if (fallbackNode == null || fallbackNode == startNode || fallbackNode.depth <= 0) {
            return false;
        }
        int minNodes = Math.max(2, pathfinderConfiguration.earlyFallbackMinPathNodes);
        if (fallbackNode.depth + 1 < minNodes) {
            return false;
        }
        double startHeuristic = Math.max(0.0, startNode.heuristic);
        if (startHeuristic <= 0.0) {
            return true;
        }
        double progressRatio = (startHeuristic - Math.max(0.0, fallbackNode.heuristic)) / startHeuristic;
        return progressRatio >= pathfinderConfiguration.earlyFallbackMinProgressRatio;
    }

    private PathfinderResult determinePostLoopResult(int depthReached, PathPosition start,
                                                      PathPosition target, Node fallbackNode) {
        if (depthReached >= pathfinderConfiguration.maxIterations) {
            return quality(new PathfinderResultImpl(PathState.MAX_ITERATIONS_REACHED,
                    reconstructPath(start, target, fallbackNode)));
        }
        if (pathfinderConfiguration.fallback) {
            return quality(new PathfinderResultImpl(PathState.FALLBACK,
                    reconstructPath(start, target, fallbackNode)));
        }
        return quality(new PathfinderResultImpl(PathState.FAILED,
                new PathImpl(start, target, EMPTY_PATH_POSITIONS)));
    }

    private PathfinderResult quality(PathfinderResultImpl result) {
        Collection<PathPosition> positions = result.getPath() == null ? List.of() : result.getPath().collect();
        return result.withQuality(PathQualityRegistry.evaluate(PathQualityContext.of(
            result,
            pathfinderConfiguration,
            positions
        )));
    }

    protected Path reconstructPath(PathPosition start, PathPosition target, Node endNode) {
        if (endNode.parent() == null && endNode.depth == 0) {
            return new PathImpl(start, target, List.of(endNode.position));
        }
        List<PathPosition> positions = tracePathPositions(endNode);
        return new PathImpl(start, target, positions);
    }

    protected Node bestFallbackNode(Node defaultFallback) {
        return defaultFallback;
    }

    private List<PathPosition> tracePathPositions(Node leafNode) {
        List<PathPosition> positions = new ArrayList<>();
        Node current = leafNode;
        while (current != null) {
            positions.add(current.position);
            current = current.parent();
        }
        Collections.reverse(positions);
        return positions;
    }

    

    protected abstract void insertStartNode(Node node, double fCost, PrimitiveMinHeap openSet);
    protected abstract Node extractBestNode(PrimitiveMinHeap openSet);
    protected abstract void initializeSearch();
    protected abstract void markNodeAsExpanded(Node node);
    protected abstract void performAlgorithmCleanup();
    protected abstract Node processSuccessors(PathPosition requestStart, PathPosition requestTarget,
                                              Node currentNode, PrimitiveMinHeap openSet,
                                              SearchContext searchContext);
}
