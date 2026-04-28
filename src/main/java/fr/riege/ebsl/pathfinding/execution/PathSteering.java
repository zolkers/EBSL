package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class PathSteering {
    private PathSteering() {
    }

    static SteeringVector steer(WalkabilityChecker checker, List<Node> path, Vec3 playerPos,
                                Node targetWp, int pursuitSegment) {
        double targetX = targetWp.position.centeredX();
        double targetZ = targetWp.position.centeredZ();
        double desiredX = targetX - playerPos.x;
        double desiredZ = targetZ - playerPos.z;
        double desiredLen = horizontalLength(desiredX, desiredZ);
        if (desiredLen < 1.0e-6) {
            return new SteeringVector(desiredX, desiredZ, false);
        }

        desiredX /= desiredLen;
        desiredZ /= desiredLen;
        if (!PathfinderConfig.CORNER_STEERING_ENABLED.get()) {
            return new SteeringVector(desiredX, desiredZ, false);
        }

        Vec3 obstacleNudge = obstacleNudge(checker, playerPos);
        double nudgeLen = horizontalLength(obstacleNudge.x, obstacleNudge.z);
        boolean nearCorner = nudgeLen > 1.0e-6;

        Vec3 centerlineCorrection = centerlineCorrection(path, playerPos, pursuitSegment);
        double correctionLen = horizontalLength(centerlineCorrection.x, centerlineCorrection.z);
        double centerlineStart = PathfinderConfig.CORNER_STEERING_CENTERLINE_START.get();
        if (correctionLen > centerlineStart && nearCorner) {
            double centerlineMax = Math.max(centerlineStart + 1.0e-6, PathfinderConfig.CORNER_STEERING_CENTERLINE_MAX.get());
            double correctionScale = Math.min(1.0, correctionLen / centerlineMax);
            double centerlineWeight = PathfinderConfig.CORNER_STEERING_CENTERLINE_WEIGHT.get();
            desiredX += (centerlineCorrection.x / correctionLen) * centerlineWeight * correctionScale;
            desiredZ += (centerlineCorrection.z / correctionLen) * centerlineWeight * correctionScale;
        }

        if (nearCorner) {
            double nudgeWeight = PathfinderConfig.CORNER_STEERING_NUDGE_WEIGHT.get();
            desiredX += (obstacleNudge.x / nudgeLen) * nudgeWeight;
            desiredZ += (obstacleNudge.z / nudgeLen) * nudgeWeight;
        }

        double adjustedLen = horizontalLength(desiredX, desiredZ);
        if (adjustedLen < 1.0e-6) {
            return new SteeringVector(targetX - playerPos.x, targetZ - playerPos.z, nearCorner);
        }
        return new SteeringVector(desiredX / adjustedLen, desiredZ / adjustedLen, nearCorner);
    }

    private static Vec3 centerlineCorrection(List<Node> path, Vec3 playerPos, int pursuitSegment) {
        if (path == null || path.size() < 2) {
            return Vec3.ZERO;
        }
        int segment = Math.max(0, Math.min(pursuitSegment, path.size() - 2));
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
            : Math.max(0.0, Math.min(1.0, ((playerPos.x - ax) * dx + (playerPos.z - az) * dz) / lenSq));
        return new Vec3((ax + dx * t) - playerPos.x, 0.0, (az + dz * t) - playerPos.z);
    }

    private static Vec3 obstacleNudge(WalkabilityChecker checker, Vec3 playerPos) {
        if (checker == null) {
            return Vec3.ZERO;
        }

        double nudgeX = 0.0;
        double nudgeZ = 0.0;
        int feetY = (int) Math.floor(playerPos.y);
        int baseX = (int) Math.floor(playerPos.x);
        int baseZ = (int) Math.floor(playerPos.z);

        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                if (ox == 0 && oz == 0) {
                    continue;
                }
                int blockX = baseX + ox;
                int blockZ = baseZ + oz;
                if (!isBodyBlocking(checker, blockX, feetY, blockZ)) {
                    continue;
                }
                double closestX = clamp(playerPos.x, blockX, blockX + 1.0);
                double closestZ = clamp(playerPos.z, blockZ, blockZ + 1.0);
                double awayX = playerPos.x - closestX;
                double awayZ = playerPos.z - closestZ;
                double dist = horizontalLength(awayX, awayZ);
                double scanRadius = PathfinderConfig.CORNER_STEERING_SCAN_RADIUS.get();
                if (dist <= 1.0e-6 || dist > scanRadius) {
                    continue;
                }
                double weight = (scanRadius - dist) / scanRadius;
                nudgeX += (awayX / dist) * weight;
                nudgeZ += (awayZ / dist) * weight;
            }
        }
        return new Vec3(nudgeX, 0.0, nudgeZ);
    }

    private static boolean isBodyBlocking(WalkabilityChecker checker, int x, int y, int z) {
        return checker.isFullWallBlock(x, y, z) || checker.isFullWallBlock(x, y + 1, z);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double horizontalLength(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    record SteeringVector(double x, double z, boolean nearCorner) {
    }
}
