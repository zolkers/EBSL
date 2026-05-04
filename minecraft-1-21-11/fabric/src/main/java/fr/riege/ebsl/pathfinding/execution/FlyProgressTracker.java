package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

final class FlyProgressTracker {
    private static final double REACH = 1.5;
    private static final double STOP_THRESH = 0.5;
    private static final long STUCK_CLIMB_MS = 1500;
    private static final long STUCK_ABORT_MS = 3000;
    private static final int TICKS_FOR_STUCK = 15;

    private int waypointIndex;
    private Vec3 lastPosCheck = Vec3.ZERO;
    private long lastProgressTime;
    private int ticksSinceLastMove;
    private double finalWaypointReach = REACH;
    private double goalStopThreshold = STOP_THRESH;

    void reset() {
        waypointIndex = 0;
        lastPosCheck = Vec3.ZERO;
        lastProgressTime = System.currentTimeMillis();
        ticksSinceLastMove = 0;
        finalWaypointReach = REACH;
        goalStopThreshold = STOP_THRESH;
    }

    void setPreciseGoalTolerance(double tolerance) {
        double clamped = Math.max(0.01, tolerance);
        this.finalWaypointReach = clamped;
        this.goalStopThreshold = clamped;
    }

    int advanceWaypoints(List<Node> path, Vec3 pos, Consumer<String> debug) {
        while (waypointIndex < path.size()) {
            Node wp = path.get(waypointIndex);
            double dx = (wp.position.flooredX() + 0.5) - pos.x;
            double dy = (wp.position.flooredY() + 0.15) - pos.y;
            double dz = (wp.position.flooredZ() + 0.5) - pos.z;
            double distSq = dx * dx + dy * dy + dz * dz;

            double waypointReach = waypointIndex == path.size() - 1 ? finalWaypointReach : REACH;
            boolean reached = distSq <= waypointReach * waypointReach;
            if (!reached && waypointIndex > 0) {
                Vec3 toWp = new Vec3(dx, dy, dz);
                Vec3 prevWp = new Vec3(
                    path.get(waypointIndex - 1).position.flooredX() + 0.5,
                    path.get(waypointIndex - 1).position.flooredY() + 0.15,
                    path.get(waypointIndex - 1).position.flooredZ() + 0.5);
                Vec3 pathDir = new Vec3(
                    wp.position.flooredX() + 0.5 - prevWp.x,
                    wp.position.flooredY() + 0.15 - prevWp.y,
                    wp.position.flooredZ() + 0.5 - prevWp.z).normalize();
                if (toWp.dot(pathDir) < 0) {
                    reached = true;
                }
            }

            if (!reached) {
                break;
            }
            lastProgressTime = System.currentTimeMillis();
            ticksSinceLastMove = 0;
            waypointIndex++;
            debug.accept(String.format(
                "waypoint advance newWp=%d pos=(%.2f,%.2f,%.2f)",
                waypointIndex, pos.x, pos.y, pos.z));
        }
        return waypointIndex;
    }

    void trackMovement(Vec3 pos) {
        double moved = pos.distanceTo(lastPosCheck);
        if (moved < 0.15) {
            ticksSinceLastMove++;
        } else {
            ticksSinceLastMove = 0;
            lastPosCheck = pos;
            lastProgressTime = System.currentTimeMillis();
        }
    }

    boolean shouldAbortForStuck() {
        return ticksSinceLastMove > TICKS_FOR_STUCK
            && System.currentTimeMillis() - lastProgressTime > STUCK_ABORT_MS;
    }

    boolean shouldClimbForRecovery() {
        long stuckMs = System.currentTimeMillis() - lastProgressTime;
        return (ticksSinceLastMove > TICKS_FOR_STUCK || stuckMs > STUCK_ABORT_MS) && stuckMs > STUCK_CLIMB_MS;
    }

    boolean shouldStopNow(net.minecraft.client.Minecraft mc, Vec3 goal) {
        if (mc.player == null) {
            return false;
        }
        Vec3 vel = mc.player.getDeltaMovement();
        double simX = mc.player.getX();
        double simZ = mc.player.getZ();
        double vx = vel.x;
        double vz = vel.z;
        for (int i = 0; i < 30; i++) {
            simX += vx;
            simZ += vz;
            vx *= 0.91;
            vz *= 0.91;
            if (Math.abs(vx) < 0.01 && Math.abs(vz) < 0.01) {
                break;
            }
        }
        double predictedDist = Math.sqrt(
            (simX - goal.x) * (simX - goal.x) + (simZ - goal.z) * (simZ - goal.z));
        return predictedDist < goalStopThreshold;
    }

    int waypointIndex() {
        return waypointIndex;
    }
}
