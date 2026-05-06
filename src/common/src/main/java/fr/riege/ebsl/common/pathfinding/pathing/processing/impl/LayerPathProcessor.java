package fr.riege.ebsl.common.pathfinding.pathing.processing.impl;

import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;

public final class LayerPathProcessor implements NodeProcessor {
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;

    @Override
    public boolean isValid(EvaluationContext context) {
        PathPosition current = context.getCurrentPathPosition();
        PathPosition previous = context.getPreviousPathPosition();
        NavigationPoint currentPoint = context.getNavigationPointProvider()
            .getNavigationPoint(current, context.getEnvironmentContext());

        if (!currentPoint.isTraversable()) return false;
        if (previous == null) return true;

        NavigationPoint previousPoint = context.getNavigationPointProvider()
            .getNavigationPoint(previous, context.getEnvironmentContext());

        double dy = currentPoint.getFloorLevel() - previousPoint.getFloorLevel();
        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();

        if (dy > Math.max(DEFAULT_MOB_JUMP_HEIGHT, PathfinderSettings.instance().maxJumpHeight.value())) {
            return false;
        }
        if (dy > DEFAULT_MOB_JUMP_HEIGHT && (previousPoint.isLiquid() || currentPoint.isLiquid())) {
            return false;
        }
        if (Math.abs(dx) == 1 && Math.abs(dz) == 1 && !hasOpenDiagonalCorners(context, previous, dx, dz)) {
            return false;
        }

        if (dy < -0.5) {
            return currentPoint.hasFloor() || currentPoint.isLiquid() || currentPoint.isClimbable();
        }
        if (dy > 0.5) {
            return hasJumpSupport(previousPoint) || currentPoint.isClimbable();
        }
        return currentPoint.hasFloor()
            || previousPoint.hasFloor()
            || currentPoint.isClimbable()
            || previousPoint.isClimbable();
    }

    @Override
    public Cost calculateCostContribution(EvaluationContext context) {
        PathPosition current = context.getCurrentPathPosition();
        PathPosition previous = context.getPreviousPathPosition();
        if (previous == null) return Cost.ZERO;

        NavigationPoint currentPoint = context.getNavigationPointProvider()
            .getNavigationPoint(current, context.getEnvironmentContext());
        NavigationPoint previousPoint = context.getNavigationPointProvider()
            .getNavigationPoint(previous, context.getEnvironmentContext());

        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();
        double dy = currentPoint.getFloorLevel() - previousPoint.getFloorLevel();
        double cost = Math.abs(dx) == 1 && Math.abs(dz) == 1
            ? PathfinderSettings.instance().diagonalCost.value()
            : PathfinderSettings.instance().walkCost.value();

        if (currentPoint.isLiquid() || previousPoint.isLiquid()) {
            cost += PathfinderSettings.instance().swimCost.value();
        }
        if (currentPoint.isClimbable() || previousPoint.isClimbable()) {
            cost += PathfinderSettings.instance().climbCost.value();
        }
        if (dy > 0.5) {
            cost += PathfinderSettings.instance().jumpCost.value();
        } else if (dy < -0.1) {
            cost += PathfinderSettings.instance().fallDyCost.value() * Math.abs(dy);
        }
        return Cost.of(Math.max(0.0, cost));
    }

    private static boolean hasOpenDiagonalCorners(EvaluationContext context, PathPosition previous, int dx, int dz) {
        NavigationPoint cornerX = context.getNavigationPointProvider()
            .getNavigationPoint(previous.add(dx, 0.0, 0.0), context.getEnvironmentContext());
        NavigationPoint cornerZ = context.getNavigationPointProvider()
            .getNavigationPoint(previous.add(0.0, 0.0, dz), context.getEnvironmentContext());
        return cornerX.isTraversable() && cornerZ.isTraversable();
    }

    private static boolean hasJumpSupport(NavigationPoint point) {
        return point.hasFloor() && !point.isLiquid();
    }
}
