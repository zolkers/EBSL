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

package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;

import java.util.List;
import java.util.Objects;

final class PathSteering {
    private PathSteering() {
    }

    private static final double LOOKAHEAD_BASE = 0.85;
    private static final double LOOKAHEAD_SPEED_SCALE = 5.0;
    private static final double LOOKAHEAD_MAX = 3.35;
    private static final double CORNER_LOOKAHEAD_MAX = 1.35;

    static SteeringVector steer(MovementTerrain checker, List<Node> path, IPlayerLayer player, Vec3d playerPos,
                                Node targetWp, int pursuitSegment, boolean precisionWindow) {
        if (targetWp == null) {
            return new SteeringVector(0.0, 0.0, false, false);
        }
        SteeringTarget target = steeringTarget(path, player, playerPos, targetWp, pursuitSegment, precisionWindow);
        Vec3d centerlineCorrection = centerlineCorrection(path, playerPos, pursuitSegment);
        double correctionLen = horizontalLength(centerlineCorrection.x(), centerlineCorrection.z());
        boolean lateralCorrection = correctionLen > PathfinderSettings.instance().cornerSteeringCenterlineStart.value();
        double targetX = target.x();
        double targetZ = target.z();
        double desiredX = targetX - playerPos.x();
        double desiredZ = targetZ - playerPos.z();
        double desiredLen = horizontalLength(desiredX, desiredZ);
        if (desiredLen < 1.0e-6) {
            return new SteeringVector(desiredX, desiredZ, false, lateralCorrection);
        }

        desiredX /= desiredLen;
        desiredZ /= desiredLen;
        PathfinderSettings settings = PathfinderSettings.instance();
        if (!Boolean.TRUE.equals(settings.cornerSteeringEnabled.value())) {
            return new SteeringVector(desiredX, desiredZ, false, lateralCorrection);
        }

        double scanRadius = settings.cornerSteeringScanRadius.value();
        Vec3d obstacleNudge = obstacleNudge(checker, playerPos, scanRadius);
        double nudgeLen = horizontalLength(obstacleNudge.x(), obstacleNudge.z());
        boolean nearCorner = nudgeLen > 1.0e-6 || target.cornerLimited();

        double centerlineStart = settings.cornerSteeringCenterlineStart.value();
        if (correctionLen > centerlineStart && nearCorner) {
            double centerlineMax = Math.max(centerlineStart + 1.0e-6, settings.cornerSteeringCenterlineMax.value());
            double correctionScale = Math.min(1.0, correctionLen / centerlineMax);
            double centerlineWeight = settings.cornerSteeringCenterlineWeight.value();
            desiredX += (centerlineCorrection.x() / correctionLen) * centerlineWeight * correctionScale;
            desiredZ += (centerlineCorrection.z() / correctionLen) * centerlineWeight * correctionScale;
        }

        if (nearCorner) {
            double nudgeWeight = settings.cornerSteeringNudgeWeight.value();
            desiredX += (obstacleNudge.x() / nudgeLen) * nudgeWeight;
            desiredZ += (obstacleNudge.z() / nudgeLen) * nudgeWeight;
        }

        double adjustedLen = horizontalLength(desiredX, desiredZ);
        if (adjustedLen < 1.0e-6) {
            return new SteeringVector(targetX - playerPos.x(), targetZ - playerPos.z(), nearCorner, lateralCorrection);
        }
        return new SteeringVector(desiredX / adjustedLen, desiredZ / adjustedLen, nearCorner, lateralCorrection);
    }

    private static Vec3d centerlineCorrection(List<Node> path, Vec3d playerPos, int pursuitSegment) {
        if (path == null || path.size() < 2) return new Vec3d(0.0, 0.0, 0.0);
        int segment = Math.clamp(pursuitSegment, 0, path.size() - 2);
        Node from = path.get(segment);
        Node to = path.get(segment + 1);
        double ax = from.position.centeredX();
        double az = from.position.centeredZ();
        double bx = to.position.centeredX();
        double bz = to.position.centeredZ();
        double dx = bx - ax;
        double dz = bz - az;
        double lenSq = dx * dx + dz * dz;
        double t = lenSq < 1.0e-6 ? 0.0
            : Math.clamp(
                ((playerPos.x() - ax) * dx + (playerPos.z() - az) * dz) / lenSq,
                0.0,
                1.0
        );
        return new Vec3d((ax + dx * t) - playerPos.x(), 0.0, (az + dz * t) - playerPos.z());
    }

    private static SteeringTarget steeringTarget(List<Node> path, IPlayerLayer player, Vec3d playerPos,
                                                 Node targetWp, int pursuitSegment, boolean precisionWindow) {
        Node waypoint = Objects.requireNonNull(targetWp, "targetWp");
        if (path == null || path.size() < 2 || precisionWindow || isPrecisionMove(targetWp)) {
            return new SteeringTarget(waypoint.position.centeredX(), waypoint.position.centeredZ(), false);
        }
        double speed = horizontalLength(player.velocity().x(), player.velocity().z());
        double lookahead = Math.min(LOOKAHEAD_MAX, LOOKAHEAD_BASE + speed * LOOKAHEAD_SPEED_SCALE);
        if (isTightTurnAhead(path, pursuitSegment)) {
            lookahead = Math.min(lookahead, CORNER_LOOKAHEAD_MAX);
        }

        int segment = Math.clamp(pursuitSegment, 0, path.size() - 2);
        double remaining = lookahead;
        double x = playerPos.x();
        double z = playerPos.z();
        boolean cornerLimited = false;
        for (int i = segment; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            double ax = from.position.centeredX();
            double az = from.position.centeredZ();
            double bx = to.position.centeredX();
            double bz = to.position.centeredZ();
            SegmentAdvance advance = segmentAdvance(new SegmentInput(ax, az, bx, bz, x, z, i == segment, remaining));
            remaining = advance.remaining();
            if (i > segment && isPrecisionMove(to)) return new SteeringTarget(bx, bz, true);
            if (advance.targetFound()) return new SteeringTarget(advance.x(), advance.z(), cornerLimited);
            if (isTightTurnAt(path, i + 1)) return new SteeringTarget(bx, bz, true);
        }
        return new SteeringTarget(waypoint.position.centeredX(), waypoint.position.centeredZ(), false);
    }

    private static SegmentAdvance segmentAdvance(SegmentInput input) {
        double dx = input.bx() - input.ax();
        double dz = input.bz() - input.az();
        double len = horizontalLength(dx, dz);
        if (len < 1.0e-6) {
            return new SegmentAdvance(false, 0.0, 0.0, input.remaining());
        }
        double t = input.currentSegment()
            ? Math.clamp(((input.x() - input.ax()) * dx + (input.z() - input.az()) * dz) / (len * len), 0.0, 1.0)
            : 0.0;
        double available = len * (1.0 - t);
        if (input.remaining() > available) {
            return new SegmentAdvance(false, 0.0, 0.0, input.remaining() - available);
        }
        double targetT = t + input.remaining() / len;
        return new SegmentAdvance(true, input.ax() + dx * targetT, input.az() + dz * targetT, 0.0);
    }

    private static boolean isPrecisionMove(Node node) {
        return node != null && switch (node.moveType()) {
            case JUMP, PARKOUR, STEP_UP, FALL, SWIM, CLIMB, FLY -> true;
            default -> false;
        };
    }

    private static boolean isTightTurnAhead(List<Node> path, int pursuitSegment) {
        int maxTurnIndex = Math.max(1, path.size() - 2);
        int start = Math.clamp(pursuitSegment + 1L, 1, maxTurnIndex);
        int end = Math.clamp(start + 2L, start, maxTurnIndex);
        for (int i = start; i <= end; i++) {
            if (isTightTurnAt(path, i)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTightTurnAt(List<Node> path, int idx) {
        if (idx <= 0 || idx >= path.size() - 1) {
            return false;
        }
        double inX = path.get(idx).position.centeredX() - path.get(idx - 1).position.centeredX();
        double inZ = path.get(idx).position.centeredZ() - path.get(idx - 1).position.centeredZ();
        double outX = path.get(idx + 1).position.centeredX() - path.get(idx).position.centeredX();
        double outZ = path.get(idx + 1).position.centeredZ() - path.get(idx).position.centeredZ();
        double inLen = horizontalLength(inX, inZ);
        double outLen = horizontalLength(outX, outZ);
        if (inLen < 1.0e-6 || outLen < 1.0e-6) {
            return false;
        }
        double dot = (inX / inLen) * (outX / outLen) + (inZ / inLen) * (outZ / outLen);
        return dot < 0.50;
    }

    private static Vec3d obstacleNudge(MovementTerrain checker, Vec3d playerPos, double scanRadius) {
        if (checker == null) return new Vec3d(0.0, 0.0, 0.0);
        double nudgeX = 0.0;
        double nudgeZ = 0.0;
        int feetY = (int) Math.floor(playerPos.y());
        int baseX = (int) Math.floor(playerPos.x());
        int baseZ = (int) Math.floor(playerPos.z());
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                int blockX = baseX + ox;
                int blockZ = baseZ + oz;
                if ((ox != 0 || oz != 0) && isBodyBlocking(checker, blockX, feetY, blockZ)) {
                    double closestX = Math.clamp(playerPos.x(), blockX, blockX + 1.0);
                    double closestZ = Math.clamp(playerPos.z(), blockZ, blockZ + 1.0);
                    double awayX = playerPos.x() - closestX;
                    double awayZ = playerPos.z() - closestZ;
                    double dist = horizontalLength(awayX, awayZ);
                    if (dist > 1.0e-6 && dist <= scanRadius) {
                        double weight = (scanRadius - dist) / scanRadius;
                        nudgeX += (awayX / dist) * weight;
                        nudgeZ += (awayZ / dist) * weight;
                    }
                }
            }
        }
        return new Vec3d(nudgeX, 0.0, nudgeZ);
    }

    private static boolean isBodyBlocking(MovementTerrain checker, int x, int y, int z) {
        return checker.isFullWallBlock(x, y, z) || checker.isFullWallBlock(x, y + 1, z);
    }

    private static double horizontalLength(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    record SteeringVector(double x, double z, boolean nearCorner, boolean lateralCorrection) {
    }

    private record SteeringTarget(double x, double z, boolean cornerLimited) {
    }

    private record SegmentAdvance(boolean targetFound, double x, double z, double remaining) {
    }

    private record SegmentInput(double ax, double az, double bx, double bz,
                                double x, double z, boolean currentSegment, double remaining) {
    }
}
