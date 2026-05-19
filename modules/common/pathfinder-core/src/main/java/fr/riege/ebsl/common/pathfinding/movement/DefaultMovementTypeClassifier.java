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

package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class DefaultMovementTypeClassifier implements MovementTypeClassifier {
    private static final double STEP_DOWN_DY_THRESHOLD = -1.1;

    @Override
    public Node.MoveType classify(MovementClassificationContext context) {
        if (context == null || context.previous() == null || context.current() == null) {
            return Node.MoveType.WALK;
        }

        PathPosition previous = context.previous();
        PathPosition current = context.current();
        NavigationPoint previousPoint = context.provider() == null
            ? null
            : context.provider().getNavigationPoint(previous, context.environmentContext());
        NavigationPoint currentPoint = context.provider() == null
            ? null
            : context.provider().getNavigationPoint(current, context.environmentContext());
        MovementTerrain checker = context.checker();

        if (isSwim(previous, current, previousPoint, currentPoint, checker)) {
            return Node.MoveType.SWIM;
        }
        if (isClimb(previous, current, previousPoint, currentPoint, checker)) {
            return Node.MoveType.CLIMB;
        }

        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();
        double dy = floorLevel(current, currentPoint, checker) - floorLevel(previous, previousPoint, checker);

        if (isParkourMove(previous, current, checker, dx, dz)) {
            return Node.MoveType.PARKOUR;
        }
        if (checker != null && checker.world().requiresJumpForStep(
            current.flooredX(), current.flooredY(), current.flooredZ(), Integer.signum(dx), Integer.signum(dz))) {
            return Node.MoveType.JUMP;
        }
        return classifyGroundMove(dx, dz, dy);
    }

    private static boolean isSwim(PathPosition previous, PathPosition current,
                                  NavigationPoint previousPoint, NavigationPoint currentPoint,
                                  MovementTerrain checker) {
        if ((currentPoint != null && currentPoint.isLiquid()) || (previousPoint != null && previousPoint.isLiquid())) {
            return true;
        }
        if (checker == null) {
            return false;
        }
        return isSwimPosition(checker, previous) || isSwimPosition(checker, current);
    }

    private static boolean isClimb(PathPosition previous, PathPosition current,
                                   NavigationPoint previousPoint, NavigationPoint currentPoint,
                                   MovementTerrain checker) {
        if (isClimbable(previousPoint) || isClimbable(currentPoint)) {
            return true;
        }
        if (checker == null) {
            return false;
        }
        return isClimbable(checker, previous) || isClimbable(checker, current);
    }

    private static boolean isClimbable(NavigationPoint point) {
        return point != null && point.isClimbable();
    }

    private static boolean isClimbable(MovementTerrain checker, PathPosition position) {
        return checker.isClimbable(position.flooredX(), position.flooredY(), position.flooredZ());
    }

    private static Node.MoveType classifyGroundMove(int dx, int dz, double dy) {
        PathfinderSettings settings = PathfinderSettings.instance();
        if (dy > settings.partialAscentThreshold.value()) {
            return Node.MoveType.STEP_UP;
        }
        if (dy < settings.descentThreshold.value()) {
            return classifyDescent(dy);
        }
        return Math.abs(dx) + Math.abs(dz) >= 2 ? Node.MoveType.WALK_DIAGONAL : Node.MoveType.WALK;
    }

    private static Node.MoveType classifyDescent(double dy) {
        return dy >= STEP_DOWN_DY_THRESHOLD ? Node.MoveType.STEP_DOWN : Node.MoveType.FALL;
    }

    private static boolean isParkourMove(PathPosition previous, PathPosition current,
                                         MovementTerrain checker, int dx, int dz) {
        if (!ParkourGeometry.isCandidateOffset(dx, dz)) {
            return false;
        }
        if (checker == null) {
            return true;
        }
        int distance = ParkourGeometry.distanceBlocks(dx, dz);
        if (distance <= 1 || !hasWalkableSupport(checker, previous) || !hasWalkableSupport(checker, current)) {
            return false;
        }
        int checks = Math.max(2, distance * 2);
        for (int step = 1; step < checks; step++) {
            double t = (double) step / checks;
            int x = (int) Math.floor(previous.centeredX() + (current.centeredX() - previous.centeredX()) * t);
            int z = (int) Math.floor(previous.centeredZ() + (current.centeredZ() - previous.centeredZ()) * t);
            if (!checker.isWalkable(x, Math.min(previous.flooredY(), current.flooredY()), z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWalkableSupport(MovementTerrain checker, PathPosition position) {
        return checker.isWalkable(position.flooredX(), position.flooredY(), position.flooredZ());
    }

    private static boolean isSwimPosition(MovementTerrain checker, PathPosition position) {
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        return checker.isWater(x, y, z)
            || (checker.isPassable(x, y, z) && checker.isWater(x, y - 1, z));
    }

    private static double floorLevel(PathPosition position, NavigationPoint point, MovementTerrain checker) {
        if (point != null) {
            return point.getFloorLevel();
        }
        if (checker == null) {
            return position.flooredY();
        }
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        if (checker.isWater(x, y, z)) {
            return y + 0.5;
        }
        if (checker.isLowPartialSupport(x, y, z)) {
            return y + checker.getTopY(x, y, z);
        }
        double topY = checker.getTopY(x, y - 1, z);
        return topY <= 0.0 ? y - 1.0 : y - 1.0 + topY;
    }
}
