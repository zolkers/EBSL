/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.pathfinding.parkour;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class ParkourJumpPlanner {
    private static final double PLAYER_WIDTH_MARGIN = 0.60;
    private static final double BASE_STANDSTILL_REACH = 2.35;
    private static final double APPROACH_BLOCK_BONUS = 0.42;
    private static final double MAX_SPRINT_REACH = 3.62;
    private static final double UPWARD_REACH_PENALTY = 1.25;
    private static final double DOWNWARD_REACH_BONUS = 0.18;
    private static final double SAFETY_MARGIN = 0.12;
    private static final int MAX_APPROACH_SCAN = 4;

    private final MovementTerrain checker;
    private final NavigationPointProvider provider;
    private final EnvironmentContext env;

    public ParkourJumpPlanner(MovementTerrain checker, NavigationPointProvider provider, EnvironmentContext env) {
        this.checker = checker;
        this.provider = provider;
        this.env = env;
    }

    public ParkourJumpPlan plan(PathPosition from, PathPosition to, NavigationPoint fromPoint, NavigationPoint toPoint) {
        int dx = to.flooredX() - from.flooredX();
        int dz = to.flooredZ() - from.flooredZ();
        int distanceBlocks = ParkourGeometry.distanceBlocks(dx, dz);
        int gapBlocks = ParkourGeometry.gapBlocks(dx, dz);
        double verticalDelta = toPoint.getFloorLevel() - fromPoint.getFloorLevel();

        ParkourJumpRules.RuleResult rule = ParkourJumpRules.evaluate(dx, dz, verticalDelta);
        ParkourJumpPlan rejection = validateJumpShape(from, to, fromPoint, toPoint, distanceBlocks, verticalDelta, rule);
        if (rejection != null) {
            return rejection;
        }

        int stepX = Integer.signum(dx);
        int stepZ = Integer.signum(dz);
        int approachBlocks = countApproachBlocks(from, -stepX, -stepZ);
        double horizontalSpan = Math.sqrt(
            Math.pow(to.centeredX() - from.centeredX(), 2.0)
                + Math.pow(to.centeredZ() - from.centeredZ(), 2.0));
        double requiredReach = Math.max(0.0, horizontalSpan - PLAYER_WIDTH_MARGIN);
        double estimatedReach = estimateReach(approachBlocks, verticalDelta, gapBlocks);
        if (rule.forceReach()) {
            estimatedReach = Math.max(estimatedReach, requiredReach);
        }
        boolean feasible = (!rule.requiresApproach() || approachBlocks > 0)
            && estimatedReach + SAFETY_MARGIN >= requiredReach;
        return new ParkourJumpPlan(
            feasible,
            approachBlocks,
            distanceBlocks,
            requiredReach,
            estimatedReach,
            verticalDelta,
            feasibilityReason(feasible, rule, approachBlocks),
            "");
    }

    private ParkourJumpPlan validateJumpShape(PathPosition from,
                                              PathPosition to,
                                              NavigationPoint fromPoint,
                                              NavigationPoint toPoint,
                                              int distanceBlocks,
                                              double verticalDelta,
                                              ParkourJumpRules.RuleResult rule) {
        if (!rule.accepted()) {
            return ParkourJumpPlan.rejected(rule.reason());
        }
        if (!hasJumpSupport(fromPoint)) {
            return ParkourJumpPlan.rejected("missing takeoff support");
        }
        if (!hasLandingSupport(to, toPoint)) {
            return ParkourJumpPlan.rejected("missing landing support", landingSupportDetail(to));
        }
        if (fromPoint.isLiquid() || toPoint.isLiquid()) {
            return ParkourJumpPlan.rejected("parkour cannot start or land in liquid");
        }
        return validateJumpSpace(from, to, distanceBlocks, verticalDelta);
    }

    private ParkourJumpPlan validateJumpSpace(PathPosition from, PathPosition to, int distanceBlocks, double verticalDelta) {
        if (!hasColumnHeadroom(from.flooredX(), from.flooredY(), from.flooredZ())
            || !hasColumnHeadroom(to.flooredX(), to.flooredY(), to.flooredZ())) {
            return ParkourJumpPlan.rejected("missing takeoff or landing headroom");
        }
        if (!hasArcClearance(from, to, verticalDelta)) {
            return ParkourJumpPlan.rejected("jump arc is blocked");
        }
        if (!hasActualGap(from, to, distanceBlocks)) {
            return ParkourJumpPlan.rejected("no gap - all intermediates are walkable");
        }
        return null;
    }

    private int countApproachBlocks(PathPosition from, int backX, int backZ) {
        int approach = 0;
        boolean blocked = false;
        for (int i = 1; i <= MAX_APPROACH_SCAN && !blocked; i++) {
            PathPosition approachPos = from.add((double) backX * i, 0.0, (double) backZ * i);
            NavigationPoint point = provider.getNavigationPoint(approachPos, env);
            if (point.isTraversable()
                && hasJumpSupport(point)
                && hasColumnHeadroom(approachPos.flooredX(), approachPos.flooredY(), approachPos.flooredZ())) {
                approach++;
            } else {
                blocked = true;
            }
        }
        return approach;
    }

    private double estimateReach(int approachBlocks, double verticalDelta, int gapBlocks) {
        double reach = BASE_STANDSTILL_REACH + Math.min(MAX_APPROACH_SCAN, approachBlocks) * APPROACH_BLOCK_BONUS;
        reach = Math.min(MAX_SPRINT_REACH, reach);
        if (verticalDelta > 0.0) {
            double upwardPenalty = gapBlocks <= 2 ? UPWARD_REACH_PENALTY * 0.05 : UPWARD_REACH_PENALTY;
            reach -= verticalDelta * upwardPenalty;
        } else if (verticalDelta < 0.0) {
            reach += Math.min(0.45, -verticalDelta * DOWNWARD_REACH_BONUS);
        }
        return reach;
    }

    private static String feasibilityReason(boolean feasible, ParkourJumpRules.RuleResult rule, int approachBlocks) {
        if (feasible) {
            return "ok";
        }
        return rule.requiresApproach() && approachBlocks == 0 ? "approach required" : "not enough momentum";
    }

    private boolean hasArcClearance(PathPosition from, PathPosition to, double verticalDelta) {
        double dx = to.centeredX() - from.centeredX();
        double dz = to.centeredZ() - from.centeredZ();
        double horizontalLength = Math.sqrt(dx * dx + dz * dz);
        int checks = Math.max(2, (int) Math.ceil(horizontalLength * 2.0));
        for (int i = 1; i < checks; i++) {
            double t = (double) i / checks;
            int x = (int) Math.floor(from.centeredX() + (to.centeredX() - from.centeredX()) * t);
            int z = (int) Math.floor(from.centeredZ() + (to.centeredZ() - from.centeredZ()) * t);
            int baseY = from.flooredY();
            if (!isLandingColumn(to, x, z) && !hasColumnHeadroom(x, baseY, z)) {
                return false;
            }
            if (verticalDelta > 0.25 && !isPassable(x, baseY + 2, z)) {
                return false;
            }
        }

        for (int i = 1; i < checks; i++) {
            double t = (double) i / checks;
            int x = (int) Math.floor(from.centeredX() + (to.centeredX() - from.centeredX()) * t);
            int z = (int) Math.floor(from.centeredZ() + (to.centeredZ() - from.centeredZ()) * t);
            if (!isLandingColumn(to, x, z)
                && (!isPassable(x, from.flooredY(), z) || !isPassable(x, from.flooredY() + 1, z))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLandingColumn(PathPosition to, int x, int z) {
        return x == to.flooredX() && z == to.flooredZ();
    }

    private boolean hasActualGap(PathPosition from, PathPosition to, int distanceBlocks) {
        int floorY = Math.min(from.flooredY(), to.flooredY()) - 1;
        int checks = Math.max(2, distanceBlocks * 2);
        for (int step = 1; step < checks; step++) {
            double t = (double) step / checks;
            int x = (int) Math.floor(from.centeredX() + (to.centeredX() - from.centeredX()) * t);
            int z = (int) Math.floor(from.centeredZ() + (to.centeredZ() - from.centeredZ()) * t);
            if (isPassable(x, floorY, z)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasColumnHeadroom(int x, int y, int z) {
        return isPassable(x, y, z) && isPassable(x, y + 1, z) && isPassable(x, y + 2, z);
    }

    private boolean isPassable(int x, int y, int z) {
        return checker != null && checker.isPassable(x, y, z);
    }

    private static boolean hasJumpSupport(NavigationPoint point) {
        return point.hasFloor() && !point.isLiquid();
    }

    private boolean hasLandingSupport(PathPosition to, NavigationPoint point) {
        if (hasJumpSupport(point)) {
            return true;
        }
        if (checker == null) {
            return false;
        }
        int x = to.flooredX();
        int y = to.flooredY();
        int z = to.flooredZ();
        return checker.isWalkable(x, y + 1, z) || checker.isWalkable(x, y - 1, z);
    }

    private String landingSupportDetail(PathPosition to) {
        if (checker == null) {
            return "no-checker";
        }
        int x = to.flooredX();
        int y = to.flooredY();
        int z = to.flooredZ();
        return "landingWalkable[y-1=" + checker.isWalkable(x, y - 1, z)
            + ",y=" + checker.isWalkable(x, y, z)
            + ",y+1=" + checker.isWalkable(x, y + 1, z)
            + "] topBelow=" + checker.getTopY(x, y - 1, z)
            + " topAt=" + checker.getTopY(x, y, z);
    }
}
