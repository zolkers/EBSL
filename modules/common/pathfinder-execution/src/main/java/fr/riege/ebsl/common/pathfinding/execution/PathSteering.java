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
package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

final class PathSteering {
    private PathSteering() {
    }

    static SteeringVector steer(MovementTerrain checker, List<Node> path, Vec3d playerPos,
                                Node targetWp, int pursuitSegment) {
        double targetX = targetWp.position.centeredX();
        double targetZ = targetWp.position.centeredZ();
        double desiredX = targetX - playerPos.x();
        double desiredZ = targetZ - playerPos.z();
        double desiredLen = horizontalLength(desiredX, desiredZ);
        if (desiredLen < 1.0e-6) {
            return new SteeringVector(desiredX, desiredZ, false);
        }

        desiredX /= desiredLen;
        desiredZ /= desiredLen;
        PathfinderSettings settings = PathfinderSettings.instance();
        if (!Boolean.TRUE.equals(settings.cornerSteeringEnabled.value())) {
            return new SteeringVector(desiredX, desiredZ, false);
        }

        double scanRadius = settings.cornerSteeringScanRadius.value();
        Vec3d obstacleNudge = obstacleNudge(checker, playerPos, scanRadius);
        double nudgeLen = horizontalLength(obstacleNudge.x(), obstacleNudge.z());
        boolean nearCorner = nudgeLen > 1.0e-6;

        Vec3d centerlineCorrection = centerlineCorrection(path, playerPos, pursuitSegment);
        double correctionLen = horizontalLength(centerlineCorrection.x(), centerlineCorrection.z());
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
            return new SteeringVector(targetX - playerPos.x(), targetZ - playerPos.z(), nearCorner);
        }
        return new SteeringVector(desiredX / adjustedLen, desiredZ / adjustedLen, nearCorner);
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

    record SteeringVector(double x, double z, boolean nearCorner) {
    }
}
