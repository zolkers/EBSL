/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicContext;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class Node implements Comparable<Node> {

    public final PathPosition position;
    public int depth;
    public final double heuristic;

    private double gCost = 0.0;
    private Node parent;

    private MoveType moveType = MoveType.WALK;

    private boolean keynode;

    private boolean inOpen;

    private boolean inClosed;

    private double cachedFCost = Double.NaN;

    public enum MoveType {
        WALK, WALK_DIAGONAL, STEP_UP, STEP_DOWN, JUMP, PARKOUR, FALL, SWIM, CLIMB, FLY
    }


    public Node(PathPosition position) {
        this.position = position;
        this.depth = 0;
        this.heuristic = 0.0;
    }

    public Node(PathPosition position, PathPosition start, PathPosition target,
                HeuristicWeights heuristicWeights, IHeuristicStrategy heuristicStrategy,
                int depth) {
        this.position = position;
        this.depth = depth;
        this.heuristic = heuristicStrategy.calculate(
                new HeuristicContext(position, start, target, heuristicWeights));
    }

    public double gCost() { return gCost; }

    public void setGCost(double gCost) { this.gCost = gCost; }

    public Node parent() { return parent; }

    public void setParent(Node parent) { this.parent = parent; }

    public void setDepth(int depth) { this.depth = Math.max(0, depth); }

    public MoveType moveType() { return moveType; }

    public void setMoveType(MoveType moveType) { this.moveType = moveType == null ? MoveType.WALK : moveType; }

    public boolean isKeynode() { return keynode; }

    public void setKeynode(boolean keynode) { this.keynode = keynode; }

    public boolean inOpen() { return inOpen; }

    public void setInOpen(boolean inOpen) { this.inOpen = inOpen; }

    public boolean inClosed() { return inClosed; }

    public void setInClosed(boolean inClosed) { this.inClosed = inClosed; }

    public double cachedFCost() { return cachedFCost; }

    public void setCachedFCost(double cachedFCost) { this.cachedFCost = cachedFCost; }

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
