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

package fr.riege.ebsl.common.pathfinding.pathfinder.processing;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.pathing.action.MovementAction;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class EvaluationContextImpl implements EvaluationContext {

    private SearchContext searchContext;
    private Node engineNode;
    private Node parentEngineNode;
    private IHeuristicStrategy heuristicStrategy;
    private MovementAction movementAction;

    public EvaluationContextImpl(SearchContext searchContext, Node engineNode,
                                  Node parentEngineNode, IHeuristicStrategy heuristicStrategy) {
        this.searchContext = searchContext;
        this.engineNode = engineNode;
        this.parentEngineNode = parentEngineNode;
        this.heuristicStrategy = heuristicStrategy;
        this.movementAction = null;
    }


    public void update(SearchContext searchContext, Node engineNode,
                       Node parentEngineNode, IHeuristicStrategy heuristicStrategy) {
        update(searchContext, engineNode, parentEngineNode, heuristicStrategy, null);
    }

    public void update(SearchContext searchContext, Node engineNode,
                       Node parentEngineNode, IHeuristicStrategy heuristicStrategy,
                       MovementAction movementAction) {
        this.searchContext = searchContext;
        this.engineNode = engineNode;
        this.parentEngineNode = parentEngineNode;
        this.heuristicStrategy = heuristicStrategy;
        this.movementAction = movementAction;
    }

    @Override public PathPosition getCurrentPathPosition() { return engineNode.position; }
    @Override public PathPosition getPreviousPathPosition() { return parentEngineNode == null ? null : parentEngineNode.position; }
    @Override public int getCurrentNodeDepth() { return engineNode.depth; }
    @Override public double getCurrentNodeHeuristicValue() { return engineNode.heuristic; }
    @Override public double getPathCostToPreviousPosition() { return parentEngineNode == null ? 0.0 : parentEngineNode.gCost(); }
    @Override public SearchContext getSearchContext() { return searchContext; }
    @Override public Node.MoveType getCurrentMoveType() { return engineNode.moveType(); }
    @Override public MovementAction getCurrentMovementAction() { return movementAction; }

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
        if (movementAction != null) {
            baseCost = baseCost * movementAction.costMultiplier() + movementAction.extraCost();
        }
        if (Double.isNaN(baseCost) || Double.isInfinite(baseCost)) {
            throw new IllegalStateException(
                    "Heuristic transition cost produced an invalid numeric value: " + baseCost);
        }
        return Math.max(baseCost, 0.0);
    }
}
