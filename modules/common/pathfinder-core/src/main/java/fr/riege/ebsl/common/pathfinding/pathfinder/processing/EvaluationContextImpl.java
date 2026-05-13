package fr.riege.ebsl.common.pathfinding.pathfinder.processing;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class EvaluationContextImpl implements EvaluationContext {

    private SearchContext searchContext;
    private Node engineNode;
    private Node parentEngineNode;
    private IHeuristicStrategy heuristicStrategy;

    public EvaluationContextImpl(SearchContext searchContext, Node engineNode,
                                  Node parentEngineNode, IHeuristicStrategy heuristicStrategy) {
        this.searchContext = searchContext;
        this.engineNode = engineNode;
        this.parentEngineNode = parentEngineNode;
        this.heuristicStrategy = heuristicStrategy;
    }


    public void update(SearchContext searchContext, Node engineNode,
                       Node parentEngineNode, IHeuristicStrategy heuristicStrategy) {
        this.searchContext = searchContext;
        this.engineNode = engineNode;
        this.parentEngineNode = parentEngineNode;
        this.heuristicStrategy = heuristicStrategy;
    }

    @Override public PathPosition getCurrentPathPosition() { return engineNode.position; }
    @Override public PathPosition getPreviousPathPosition() { return parentEngineNode == null ? null : parentEngineNode.position; }
    @Override public int getCurrentNodeDepth() { return engineNode.depth; }
    @Override public double getCurrentNodeHeuristicValue() { return engineNode.heuristic; }
    @Override public double getPathCostToPreviousPosition() { return parentEngineNode == null ? 0.0 : parentEngineNode.gCost(); }
    @Override public SearchContext getSearchContext() { return searchContext; }
    @Override public Node.MoveType getCurrentMoveType() { return engineNode.moveType(); }

    @Override
    public PathPosition getGrandparentPathPosition() {
        return (parentEngineNode != null && parentEngineNode.parent() != null)
                ? parentEngineNode.parent().position
                : null;
    }

    @Override
    public PathPosition getGreatGrandparentPathPosition() {
        if (parentEngineNode == null) return null;
        Node gp = parentEngineNode.parent();
        return (gp != null && gp.parent() != null) ? gp.parent().position : null;
    }

    @Override
    public double getBaseTransitionCost() {
        if (parentEngineNode == null) return 0.0;
        double baseCost = heuristicStrategy.calculateTransitionCost(
                parentEngineNode.position, engineNode.position);
        if (Double.isNaN(baseCost) || Double.isInfinite(baseCost)) {
            throw new IllegalStateException(
                    "Heuristic transition cost produced an invalid numeric value: " + baseCost);
        }
        return Math.max(baseCost, 0.0);
    }
}
