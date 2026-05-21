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

package fr.riege.ebsl.common.pathfinding.pathing.processing.impl;

import fr.riege.ebsl.common.pathfinding.parkour.ParkourEvaluationTelemetry;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourGeometry;
import fr.riege.ebsl.common.pathfinding.parkour.ParkourJumpPlanner;
import fr.riege.ebsl.common.pathfinding.pathing.processing.Cost;
import fr.riege.ebsl.common.pathfinding.pathing.processing.NodeProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;
import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPoint;
import fr.riege.ebsl.common.pathfinding.provider.WorldNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

@SuppressWarnings("java:S107")
public final class LayerPathProcessor implements NodeProcessor {
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
    private static final double ASCENT_DY_THRESHOLD = 0.5;
    private static final double PARTIAL_ASCENT_DY_THRESHOLD = 0.2;
    private static final double DESCENT_DY_THRESHOLD = -0.1;
    private static final double PARTIAL_ASCENT_DIAGONAL_EDGE_PENALTY = 0.12;
    private static final int MIN_APPROACH_LOOKAHEAD_DIST = 3;
    private static final double APPROACH_MULTIPLIER_DIST_1 = 0.85;
    private static final double APPROACH_MULTIPLIER_DIST_2 = 0.55;
    private static final double APPROACH_MULTIPLIER_DIST_3 = 0.30;
    private static final double APPROACH_OPENING_IMBALANCE_PENALTY = 0.18;

    private final ThreadLocal<ParkourJumpPlanner> parkourPlanner = new ThreadLocal<>();
    private PathPosition lastCheckedPrev;
    private boolean lastPrevInsideSolid;
    private double maxJumpHeight = DEFAULT_MOB_JUMP_HEIGHT;
    private double walkCost;
    private double diagonalCost;
    private double stepUpCost;
    private double stepDownCost;
    private double swimCost;
    private double climbCost;
    private double partialAscentCost;
    private double jumpCost;
    private double fullStepAscentDyCost;
    private double fullStepAscentBaseCost;
    private double fallDyCost;
    private double parkourCost;
    private double constrainedParkourLandingCost;
    private double cardinalWallCost;
    private double diagonalWallCost;
    private double partialAscentEdgeCost;
    private double partialAscentEntrySideCost;
    private double openingEntryImbalanceCost;
    private double directionTurnCost;
    private double directionSharpTurnCost;
    private double directionStraightBonus;
    private int corridorLookaheadBlocks;

    public LayerPathProcessor() {
        captureSettings();
    }

    @Override
    public void initializeSearch(SearchContext context) {
        captureSettings();
        if (context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider) {
            parkourPlanner.set(new ParkourJumpPlanner(layerProvider.checker(), context.getNavigationPointProvider(), context.getEnvironmentContext()));
        }
    }

    @Override
    public void finalizeSearch(SearchContext context) {
        parkourPlanner.remove();
        lastCheckedPrev = null;
        lastPrevInsideSolid = false;
    }

    @Override
    public boolean isValid(EvaluationContext context) {
        PathPosition current = context.getCurrentPathPosition();
        PathPosition previous = context.getPreviousPathPosition();
        NavigationPoint currentPoint = context.getNavigationPointProvider()
            .getNavigationPoint(current, context.getEnvironmentContext());

        if (previous != null && transitionStartsOrEndsInsideSolid(context, previous, current)) {
            return false;
        }

        if (!currentPoint.isTraversable()) {
            return false;
        }
        if (previous == null) {
            return true;
        }

        NavigationPoint previousPoint = context.getNavigationPointProvider()
            .getNavigationPoint(previous, context.getEnvironmentContext());

        double dy = currentPoint.getFloorLevel() - previousPoint.getFloorLevel();
        int dx = current.flooredX() - previous.flooredX();
        int dz = current.flooredZ() - previous.flooredZ();

        if (!hasValidElevationTransition(context, previous, current, previousPoint, currentPoint, dx, dz, dy)) {
            return false;
        }

        if (dy < -0.5) {
            return currentPoint.hasFloor() || currentPoint.isLiquid() || currentPoint.isClimbable();
        }
        if (dy > 0.5) {
            return hasJumpSupport(previousPoint) || previousPoint.isClimbable() || currentPoint.isClimbable();
        }
        return currentPoint.hasFloor()
            || currentPoint.isLiquid()
            || previousPoint.isLiquid()
            || currentPoint.isClimbable()
            || previousPoint.isClimbable();
    }

    private boolean transitionStartsOrEndsInsideSolid(EvaluationContext context, PathPosition previous, PathPosition current) {
        if (previous != lastCheckedPrev) {
            lastCheckedPrev = previous;
            lastPrevInsideSolid = isInsideSolidSpace(context, previous);
        }
        return lastPrevInsideSolid || isInsideSolidSpace(context, current);
    }

    private boolean hasValidElevationTransition(EvaluationContext context,
                                                PathPosition previous,
                                                PathPosition current,
                                                NavigationPoint previousPoint,
                                                NavigationPoint currentPoint,
                                                int dx,
                                                int dz,
                                                double dy) {
        if (dy > maxJumpHeight || isLiquidAscentTooHigh(previousPoint, currentPoint, dy)) {
            return false;
        }
        if (dy > ASCENT_DY_THRESHOLD && hasJumpSupport(previousPoint) && !hasJumpHeadroom(context, previous)) {
            return false;
        }
        if (isParkourOffset(dx, dz) && !isValidParkourMove(context, previous, current, previousPoint, currentPoint)) {
            return false;
        }
        if (isBlockedDiagonalTransition(context, previous, current, dx, dz)) {
            return false;
        }
        return dy < -0.5
            || !requiresJumpForStep(context, current, dx, dz)
            || hasJumpSupport(previousPoint) && hasJumpHeadroom(context, previous);
    }

    private static boolean isLiquidAscentTooHigh(NavigationPoint previousPoint, NavigationPoint currentPoint, double dy) {
        return dy > DEFAULT_MOB_JUMP_HEIGHT && (previousPoint.isLiquid() || currentPoint.isLiquid());
    }

    private boolean isBlockedDiagonalTransition(EvaluationContext context,
                                                PathPosition previous,
                                                PathPosition current,
                                                int dx,
                                                int dz) {
        if (Math.abs(dx) != 1 || Math.abs(dz) != 1) {
            return false;
        }
        return !hasOpenDiagonalCorners(context, previous, dx, dz)
            || hasPartialSupportCornerClip(context, previous, current, dx, dz);
    }

    @Override
    @SuppressWarnings("java:S3776")
    public Cost calculateCostContribution(EvaluationContext context) {
        PathPosition current = context.getCurrentPathPosition();
        PathPosition previous = context.getPreviousPathPosition();
        if (previous == null) return Cost.ZERO;

        NavigationPoint currentPoint = context.getNavigationPointProvider()
            .getNavigationPoint(current, context.getEnvironmentContext());
        NavigationPoint previousPoint = context.getNavigationPointProvider()
            .getNavigationPoint(previous, context.getEnvironmentContext());

        double dy = currentPoint.getFloorLevel() - previousPoint.getFloorLevel();
        int cx = current.flooredX();
        int cy = current.flooredY();
        int cz = current.flooredZ();
        int px = previous.flooredX();
        int py = previous.flooredY();
        int pz = previous.flooredZ();
        boolean fullStepSupport = isFullStepSupport(context, cx, cy - 1, cz);
        boolean partialAscent = dy > PARTIAL_ASCENT_DY_THRESHOLD && !fullStepSupport;
        boolean diagonalMove = Math.abs(cx - px) == 1 && Math.abs(cz - pz) == 1;
        double additionalCost = movementTypeCost(currentPoint, previousPoint, dy, diagonalMove, partialAscent);

        if (dy > ASCENT_DY_THRESHOLD) {
            additionalCost += stepUpCost;
            if (fullStepSupport) {
                additionalCost += fullStepAscentDyCost * dy + fullStepAscentBaseCost;
            }
        } else if (dy < DESCENT_DY_THRESHOLD) {
            additionalCost += stepDownCost;
            additionalCost += fallDyCost * Math.abs(dy);
        }

        int moveDx = cx - px;
        int moveDz = cz - pz;
        if (isParkourOffset(moveDx, moveDz)) {
            additionalCost += parkourCost;
            if (hasConstrainedParkourLanding(context, cx, cy, cz, moveDx, moveDz)) {
                additionalCost += constrainedParkourLandingCost;
            }
        }

        for (int i = 2; i <= 3; i++) {
            if (canOcclude(context, cx, cy + i, cz)) {
                additionalCost += 0.1 / i;
            }
        }

        boolean wallN = isFullWallBlock(context, cx, cy, cz - 1);
        boolean wallS = isFullWallBlock(context, cx, cy, cz + 1);
        boolean wallW = isFullWallBlock(context, cx - 1, cy, cz);
        boolean wallE = isFullWallBlock(context, cx + 1, cy, cz);
        boolean wallNW = isFullWallBlock(context, cx - 1, cy, cz - 1);
        boolean wallNE = isFullWallBlock(context, cx + 1, cy, cz - 1);
        boolean wallSW = isFullWallBlock(context, cx - 1, cy, cz + 1);
        boolean wallSE = isFullWallBlock(context, cx + 1, cy, cz + 1);

        int cardinalWalls = (wallN ? 1 : 0) + (wallS ? 1 : 0) + (wallW ? 1 : 0) + (wallE ? 1 : 0);
        int diagonalWalls = (wallNW ? 1 : 0) + (wallNE ? 1 : 0) + (wallSW ? 1 : 0) + (wallSE ? 1 : 0);

        double cardinalWallWeight = cardinalWallCost;
        double diagonalWallWeight = diagonalWallCost;
        if (partialAscent) {
            cardinalWallWeight += partialAscentEdgeCost;
            diagonalWallWeight += PARTIAL_ASCENT_DIAGONAL_EDGE_PENALTY;
        }
        additionalCost += cardinalWalls * cardinalWallWeight + diagonalWalls * diagonalWallWeight;

        if (cardinalWalls > 0 || diagonalWalls > 0) {
            int ascentDist = detectPartialAscentAhead(context, cx, cy, cz, moveDx, moveDz, corridorLookaheadBlocks);
            int openingDist = detectOpeningAhead(context, cx, cy, cz, moveDx, moveDz, corridorLookaheadBlocks);
            int transitionDist = (ascentDist > 0 && openingDist > 0)
                ? Math.min(ascentDist, openingDist)
                : Math.max(ascentDist, openingDist);

            if (transitionDist > 0) {
                double multiplier = switch (transitionDist) {
                    case 1 -> APPROACH_MULTIPLIER_DIST_1;
                    case 2 -> APPROACH_MULTIPLIER_DIST_2;
                    default -> APPROACH_MULTIPLIER_DIST_3;
                };
                additionalCost += cardinalWalls * (cardinalWallCost * multiplier);
                additionalCost += diagonalWalls * (diagonalWallCost * multiplier);
            }

            if (openingDist > 0 && isCardinalMove(moveDx, moveDz)) {
                int imbalance = countLateralImbalance(moveDx, moveDz, wallN, wallS, wallW, wallE);
                double decay = switch (openingDist) {
                    case 1 -> 1.0;
                    case 2 -> 0.65;
                    default -> 0.35;
                };
                additionalCost += imbalance * APPROACH_OPENING_IMBALANCE_PENALTY * decay;
            }

            if (partialAscent) {
                int entrySideWalls = countEntrySideWalls(context, px, py, pz, moveDx, moveDz);
                entrySideWalls += countEntrySideWalls(context, cx, cy, cz, moveDx, moveDz);
                additionalCost += entrySideWalls * partialAscentEntrySideCost;
            }

            if (isRoomOpeningTransition(context, px, py, pz, cx, cy, cz, moveDx, moveDz)) {
                int imbalance = countLateralImbalanceAt(context, px, py, pz, moveDx, moveDz)
                    + countLateralImbalanceAt(context, cx, cy, cz, moveDx, moveDz);
                additionalCost += imbalance * openingEntryImbalanceCost;
            }
        }

        additionalCost += directionCost(context, current, previous);
        return Cost.of(Math.max(0.0, additionalCost));
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

    private static boolean hasJumpHeadroom(EvaluationContext context, PathPosition pos) {
        return isPassable(context, pos.flooredX(), pos.flooredY() + 2, pos.flooredZ());
    }

    private static boolean requiresJumpForStep(EvaluationContext context, PathPosition pos, int dx, int dz) {
        if (context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider) {
            return layerProvider.checker().world().requiresJumpForStep(pos.flooredX(), pos.flooredY(), pos.flooredZ(), dx, dz);
        }
        return false;
    }

    private boolean isValidParkourMove(EvaluationContext context, PathPosition from, PathPosition to,
                                       NavigationPoint fromPoint, NavigationPoint toPoint) {
        ParkourJumpPlanner planner = parkourPlanner.get();
        if (planner == null && context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider) {
            planner = new ParkourJumpPlanner(layerProvider.checker(), context.getNavigationPointProvider(), context.getEnvironmentContext());
        }
        if (planner == null) {
            return false;
        }
        var plan = planner.plan(from, to, fromPoint, toPoint);
        ParkourEvaluationTelemetry.recordEvaluation(from, to, plan);
        return plan.feasible();
    }

    private static boolean isParkourOffset(int dx, int dz) {
        return ParkourGeometry.isCandidateOffset(dx, dz);
    }

    private static boolean hasPartialSupportCornerClip(EvaluationContext context, PathPosition previous,
                                                       PathPosition current, int dx, int dz) {
        if (!(context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider)) {
            return false;
        }
        var checker = layerProvider.checker();
        int fy = previous.flooredY();
        double srcFloor = checker.getTopY(previous.flooredX(), fy - 1, previous.flooredZ());
        double dstFloor = checker.getTopY(current.flooredX(), fy - 1, current.flooredZ());
        if (!((srcFloor > 0.1 && srcFloor < 0.95) || (dstFloor > 0.1 && dstFloor < 0.95))) {
            return false;
        }
        PathPosition corner1 = previous.add(dx, 0.0, 0.0);
        PathPosition corner2 = previous.add(0.0, 0.0, dz);
        return checker.isFullWall(corner1.flooredX(), fy - 1, corner1.flooredZ())
            || checker.isFullWall(corner2.flooredX(), fy - 1, corner2.flooredZ());
    }

    private static boolean isInsideSolidSpace(EvaluationContext context, PathPosition pos) {
        return isBlockingPlayerSpace(context, pos.flooredX(), pos.flooredY(), pos.flooredZ())
            || isBlockingPlayerSpace(context, pos.flooredX(), pos.flooredY() + 1, pos.flooredZ());
    }

    private static boolean isBlockingPlayerSpace(EvaluationContext context, int x, int y, int z) {
        if (!(context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider)) {
            return false;
        }
        var checker = layerProvider.checker();
        if (checker.isLowPartialSupport(x, y, z)) {
            return false;
        }
        return !checker.isPassable(x, y, z) && checker.getTopY(x, y, z) > 0.0;
    }

    private double movementTypeCost(NavigationPoint currentPoint, NavigationPoint previousPoint,
                                    double dy, boolean diagonalMove, boolean partialAscent) {
        double cost = diagonalMove ? diagonalCost : walkCost;
        if (currentPoint.isLiquid() || previousPoint.isLiquid()) {
            cost += swimCost;
        }
        if (currentPoint.isClimbable() || previousPoint.isClimbable()) {
            cost += climbCost;
        }
        if (partialAscent) {
            cost += partialAscentCost;
        } else if (dy > ASCENT_DY_THRESHOLD) {
            cost += jumpCost;
        }
        return cost;
    }

    private void captureSettings() {
        PathfinderSettings settings = PathfinderSettings.instance();
        maxJumpHeight = Math.max(DEFAULT_MOB_JUMP_HEIGHT, settings.maxJumpHeight.value());
        walkCost = settings.walkCost.value();
        diagonalCost = settings.diagonalCost.value();
        stepUpCost = settings.stepUpCost.value();
        stepDownCost = settings.stepDownCost.value();
        swimCost = settings.swimCost.value();
        climbCost = settings.climbCost.value();
        partialAscentCost = settings.partialAscentCost.value();
        jumpCost = settings.jumpCost.value();
        fullStepAscentDyCost = settings.fullStepAscentDyCost.value();
        fullStepAscentBaseCost = settings.fullStepAscentBaseCost.value();
        fallDyCost = settings.fallDyCost.value();
        parkourCost = settings.parkourCost.value();
        constrainedParkourLandingCost = settings.constrainedParkourLandingCost.value();
        cardinalWallCost = settings.cardinalWallCost.value();
        diagonalWallCost = settings.diagonalWallCost.value();
        partialAscentEdgeCost = settings.partialAscentEdgeCost.value();
        partialAscentEntrySideCost = settings.partialAscentEntrySideCost.value();
        openingEntryImbalanceCost = settings.openingEntryImbalanceCost.value();
        directionTurnCost = settings.directionTurnCost.value();
        directionSharpTurnCost = settings.directionSharpTurnCost.value();
        directionStraightBonus = settings.directionStraightBonus.value();
        corridorLookaheadBlocks = Math.max(MIN_APPROACH_LOOKAHEAD_DIST, settings.corridorLookaheadBlocks.value());
    }

    private static boolean isFullStepSupport(EvaluationContext context, int x, int y, int z) {
        return context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider
            && layerProvider.checker().isFullWall(x, y, z);
    }

    private static boolean isFullWallBlock(EvaluationContext context, int x, int y, int z) {
        return context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider
            && layerProvider.checker().isFullWallBlock(x, y, z);
    }

    private static boolean canOcclude(EvaluationContext context, int x, int y, int z) {
        return context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider
            && layerProvider.checker().canOcclude(x, y, z);
    }

    private static boolean isPassable(EvaluationContext context, int x, int y, int z) {
        return context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider
            && layerProvider.checker().isPassable(x, y, z);
    }

    private static int countEntrySideWalls(EvaluationContext context, int x, int y, int z, int moveDx, int moveDz) {
        if (Math.abs(moveDx) > Math.abs(moveDz)) {
            int walls = 0;
            if (isFullWallBlock(context, x, y, z - 1)) walls++;
            if (isFullWallBlock(context, x, y, z + 1)) walls++;
            return walls;
        }
        if (Math.abs(moveDz) > Math.abs(moveDx)) {
            int walls = 0;
            if (isFullWallBlock(context, x - 1, y, z)) walls++;
            if (isFullWallBlock(context, x + 1, y, z)) walls++;
            return walls;
        }
        int walls = 0;
        if (isFullWallBlock(context, x, y, z - 1)) walls++;
        if (isFullWallBlock(context, x, y, z + 1)) walls++;
        if (isFullWallBlock(context, x - 1, y, z)) walls++;
        if (isFullWallBlock(context, x + 1, y, z)) walls++;
        return walls;
    }

    private static boolean isRoomOpeningTransition(EvaluationContext context, int px, int py, int pz, int cx, int cy, int cz,
                                                   int moveDx, int moveDz) {
        if (!isCardinalMove(moveDx, moveDz)) return false;
        int prevSides = countCardinalSideWalls(context, px, py, pz, moveDx, moveDz);
        int curSides = countCardinalSideWalls(context, cx, cy, cz, moveDx, moveDz);
        return curSides > prevSides && curSides >= 1;
    }

    private static int countLateralImbalance(int moveDx, int moveDz, boolean wallN, boolean wallS, boolean wallW, boolean wallE) {
        if (!isCardinalMove(moveDx, moveDz)) return 0;
        boolean sideA;
        boolean sideB;
        if (Math.abs(moveDx) > Math.abs(moveDz)) {
            sideA = wallN;
            sideB = wallS;
        } else {
            sideA = wallW;
            sideB = wallE;
        }
        return sideA ^ sideB ? 1 : 0;
    }

    private static int countLateralImbalanceAt(EvaluationContext context, int x, int y, int z, int moveDx, int moveDz) {
        if (!isCardinalMove(moveDx, moveDz)) return 0;
        boolean sideA;
        boolean sideB;
        if (Math.abs(moveDx) > Math.abs(moveDz)) {
            sideA = isFullWallBlock(context, x, y, z - 1);
            sideB = isFullWallBlock(context, x, y, z + 1);
        } else {
            sideA = isFullWallBlock(context, x - 1, y, z);
            sideB = isFullWallBlock(context, x + 1, y, z);
        }
        return sideA ^ sideB ? 1 : 0;
    }

    private static int countCardinalSideWalls(EvaluationContext context, int x, int y, int z, int moveDx, int moveDz) {
        if (!isCardinalMove(moveDx, moveDz)) return 0;
        if (Math.abs(moveDx) > Math.abs(moveDz)) {
            int walls = 0;
            if (isFullWallBlock(context, x, y, z - 1)) walls++;
            if (isFullWallBlock(context, x, y, z + 1)) walls++;
            return walls;
        }
        int walls = 0;
        if (isFullWallBlock(context, x - 1, y, z)) walls++;
        if (isFullWallBlock(context, x + 1, y, z)) walls++;
        return walls;
    }

    private static boolean isCardinalMove(int moveDx, int moveDz) {
        return (moveDx == 0) != (moveDz == 0);
    }

    private static boolean hasConstrainedParkourLanding(EvaluationContext context, int x, int y, int z,
                                                        int moveDx, int moveDz) {
        int stepX = Math.abs(moveDx) >= Math.abs(moveDz) ? Integer.signum(moveDx) : 0;
        int stepZ = stepX == 0 ? Integer.signum(moveDz) : 0;
        if (stepX == 0 && stepZ == 0) {
            return false;
        }
        int aheadX = x + stepX;
        int aheadZ = z + stepZ;
        return isFullWallBlock(context, aheadX, y, aheadZ)
            || isFullWallBlock(context, aheadX, y + 1, aheadZ);
    }

    private static int detectPartialAscentAhead(EvaluationContext context, int bx, int by, int bz,
                                                int moveDx, int moveDz, int lookaheadBlocks) {
        if (moveDx == 0 && moveDz == 0) return 0;
        int stepX = Integer.signum(moveDx);
        int stepZ = Integer.signum(moveDz);
        if (!(context.getNavigationPointProvider() instanceof WorldNavigationPointProvider layerProvider)) {
            return 0;
        }
        var checker = layerProvider.checker();
        int foundDistance = 0;
        boolean blocked = false;
        for (int dist = 1; dist <= lookaheadBlocks && foundDistance == 0 && !blocked; dist++) {
            int ax = bx + stepX * dist;
            int az = bz + stepZ * dist;
            blocked = isFullWallBlock(context, ax, by, az);
            int belowY = by - 1;
            if (!blocked && !checker.isAir(ax, belowY, az) && !checker.isFullWallBlock(ax, belowY, az)) {
                double topY = checker.getTopY(ax, belowY, az);
                if (topY > 0.2 && topY < 0.95) {
                    foundDistance = dist;
                }
            }
        }
        return foundDistance;
    }

    private static int detectOpeningAhead(EvaluationContext context, int bx, int by, int bz,
                                          int moveDx, int moveDz, int lookaheadBlocks) {
        if (!isCardinalMove(moveDx, moveDz)) return 0;
        int stepX = Integer.signum(moveDx);
        int stepZ = Integer.signum(moveDz);
        int prevSides = countCardinalSideWalls(context, bx, by, bz, moveDx, moveDz);
        int foundDistance = 0;
        boolean blocked = false;
        for (int dist = 1; dist <= lookaheadBlocks && foundDistance == 0 && !blocked; dist++) {
            int ax = bx + stepX * dist;
            int az = bz + stepZ * dist;
            blocked = isFullWallBlock(context, ax, by, az) || isFullWallBlock(context, ax, by + 1, az);
            if (!blocked) {
                int aheadSides = countCardinalSideWalls(context, ax, by, az, moveDx, moveDz);
                if (aheadSides > prevSides && aheadSides >= 1) {
                    foundDistance = dist;
                }
                prevSides = aheadSides;
            }
        }
        return foundDistance;
    }

    private double directionCost(EvaluationContext context, PathPosition current, PathPosition previous) {
        PathPosition gpPos = context.getGrandparentPathPosition();
        PathPosition ggpPos = context.getGreatGrandparentPathPosition();
        double avgX = 0.0;
        double avgZ = 0.0;
        double totalW = 0.0;
        if (gpPos != null) {
            double dx1 = previous.x - gpPos.x;
            double dz1 = previous.z - gpPos.z;
            double len1 = Math.sqrt(dx1 * dx1 + dz1 * dz1);
            if (len1 > 0.1) {
                avgX += dx1 / len1;
                avgZ += dz1 / len1;
                totalW += 1.0;
            }
        }
        if (ggpPos != null && gpPos != null) {
            double dx2 = gpPos.x - ggpPos.x;
            double dz2 = gpPos.z - ggpPos.z;
            double len2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
            if (len2 > 0.1) {
                avgX += dx2 / len2 * 0.6;
                avgZ += dz2 / len2 * 0.6;
                totalW += 0.6;
            }
        }
        if (totalW <= 0.1) {
            return 0.0;
        }
        double[] averageDirection = normalizeWeightedDirection(avgX, avgZ, totalW);
        double cdx = current.x - previous.x;
        double cdz = current.z - previous.z;
        double curLen = Math.sqrt(cdx * cdx + cdz * cdz);
        if (curLen <= 0.1) {
            return 0.0;
        }
        double dot = (cdx / curLen) * averageDirection[0] + (cdz / curLen) * averageDirection[1];
        double angleDeg = Math.toDegrees(Math.acos(Math.clamp(dot, -1.0, 1.0)));
        if (angleDeg < 5.0) {
            return -directionStraightBonus;
        }
        double normalizedTurn = Math.clamp(angleDeg / 90.0, 0.0, 2.0);
        double sharpTurn = angleDeg <= 90.0 ? 0.0 : (angleDeg - 90.0) / 90.0;
        return normalizedTurn * normalizedTurn * directionTurnCost + sharpTurn * directionSharpTurnCost;
    }

    private static double[] normalizeWeightedDirection(double x, double z, double weight) {
        if (weight <= 0.0) {
            return new double[] {0.0, 0.0};
        }
        return new double[] {x / weight, z / weight};
    }
}
