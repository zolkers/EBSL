package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicContext;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class Node implements Comparable<Node> {

    public final PathPosition position;
    public final int          depth;
    public final double       heuristic;

    public double   gCost       = 0.0;
    public Node     parent      = null;
    
    public MoveType moveType    = MoveType.WALK;
    
    public boolean  isKeynode   = false;
    
    public boolean  inOpen      = false;
    
    public boolean  inClosed    = false;
    
    public double   cachedFCost = Double.NaN;

    public enum MoveType {
        WALK, WALK_DIAGONAL, STEP_UP, STEP_DOWN, JUMP, PARKOUR, FALL, SWIM, CLIMB, FLY
    }

    
    public Node(PathPosition position) {
        this.position  = position;
        this.depth     = 0;
        this.heuristic = 0.0;
    }

    public Node(PathPosition position, PathPosition start, PathPosition target,
                HeuristicWeights heuristicWeights, IHeuristicStrategy heuristicStrategy,
                int depth) {
        this.position  = position;
        this.depth     = depth;
        this.heuristic = heuristicStrategy.calculate(
                new HeuristicContext(position, start, target, heuristicWeights));
    }

    public double fCost() { return gCost + heuristic; }

    public boolean isTarget(PathPosition target) { return position.equals(target); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node n)) return false;
        return position.equals(n.position);
    }

    @Override
    public int hashCode() { return position.hashCode(); }

    @Override
    public int compareTo(Node other) {
        int fc = Double.compare(fCost(), other.fCost());
        if (fc != 0) return fc;
        int hc = Double.compare(heuristic, other.heuristic);
        if (hc != 0) return hc;
        return Integer.compare(depth, other.depth);
    }
}
