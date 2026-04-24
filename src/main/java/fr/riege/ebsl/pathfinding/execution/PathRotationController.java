package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.movement.types.MovementRegistry;
import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.pathfinding.rotation.EasingType;
import fr.riege.ebsl.pathfinding.rotation.Rotation;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.pathfinding.rotation.strategy.TimedEaseStrategy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

final class PathRotationController {
    private static final boolean USE_CAMERA_RAIL = true;
    private static final double CAMERA_RAIL_REACHED_DIST = 1.15;
    private static final double LEGACY_CAMERA_EYE_Y = 1.6;
    private static final double CAMERA_RAIL_GUIDE_LOOKAHEAD_DIST = 3.5;
    private static final long ROTATION_REDISPATCH_COOLDOWN_MS = 220;
    private static final float IDLE_YAW_DEADBAND_DEG = 2.0f;
    private static final float IDLE_PITCH_DEADBAND_DEG = 3.0f;
    private static final float ACTIVE_YAW_RETARGET_DEG = 6.0f;
    private static final float ACTIVE_PITCH_RETARGET_DEG = 5.0f;

    private List<Vec3> cameraPath = Collections.emptyList();
    private int cameraIndex;
    private Vec3 lastCameraCheckPos;
    private int camTargetIdx = -1;
    private int lastRotationDebugCamTarget = -2;
    private long lastRotationDispatchMs;
    private final PathTargetSelector targetSelector = new PathTargetSelector();

    void rebuild(List<Node> path) {
        this.cameraPath = USE_CAMERA_RAIL ? CameraRailBuilder.build(path) : Collections.emptyList();
        this.cameraIndex = 0;
        this.lastCameraCheckPos = null;
        this.camTargetIdx = -1;
        this.lastRotationDebugCamTarget = -2;
        this.lastRotationDispatchMs = 0;
    }

    void reset() {
        this.cameraPath = Collections.emptyList();
        this.cameraIndex = 0;
        this.lastCameraCheckPos = null;
        this.camTargetIdx = -1;
        this.lastRotationDebugCamTarget = -2;
        this.lastRotationDispatchMs = 0;
    }

    void updateRotation(Minecraft mc, Vec3 playerPos, List<Node> path, int pursuitSegment, Consumer<String> debug) {
        if (path == null || path.isEmpty() || mc.player == null) {
            return;
        }

        RotationTarget rotationTarget = selectRotationTarget(mc, playerPos, path, pursuitSegment);
        camTargetIdx = rotationTarget.visualizerIndex();

        if (lastRotationDebugCamTarget != camTargetIdx) {
            debug(debug, "cam target changed prev=%d current=%d pursuitSegment=%d",
                lastRotationDebugCamTarget, camTargetIdx, pursuitSegment);
            lastRotationDebugCamTarget = camTargetIdx;
        }

        debug(debug, "target pick mode=%s camTarget=%d player=(%.2f,%.2f,%.2f) rotTarget=(%.2f,%.2f,%.2f)",
            rotationTarget.mode(),
            rotationTarget.targetIndex(),
            playerPos.x, playerPos.y, playerPos.z,
            rotationTarget.position().x, rotationTarget.position().y, rotationTarget.position().z);

        Rotation desiredRot = AngleUtils.getRotation(rotationTarget.position());
        dispatchRotationIfNeeded(mc, path, pursuitSegment, debug, desiredRot);
    }

    List<Vec3> getCameraPath() {
        return cameraPath;
    }

    int getCameraIndex() {
        return cameraIndex;
    }

    int getCamTargetIdx() {
        return camTargetIdx;
    }

    private RotationTarget selectRotationTarget(Minecraft mc, Vec3 playerPos, List<Node> path, int pursuitSegment) {
        if (USE_CAMERA_RAIL && !cameraPath.isEmpty()) {
            int camTarget = pickCameraRailTarget(playerPos);
            Vec3 rotTargetPos = getCameraRailGuideTarget(playerPos, camTarget);
            int visualizerIndex = Math.min(cameraPath.size() - 1, camTarget + 1);
            return new RotationTarget("camera_rail", camTarget, visualizerIndex, rotTargetPos);
        }

        int camTarget = targetSelector.pickLegacyCamTarget(mc, playerPos, path, pursuitSegment);
        Node rotTarget = path.get(camTarget);
        Vec3 rotTargetPos = new Vec3(
            rotTarget.position.centeredX(),
            rotTarget.position.flooredY() + LEGACY_CAMERA_EYE_Y,
            rotTarget.position.centeredZ());
        return new RotationTarget("legacy", camTarget, camTarget, rotTargetPos);
    }

    private void dispatchRotationIfNeeded(Minecraft mc, List<Node> path, int pursuitSegment,
                                          Consumer<String> debug, Rotation desiredRot) {
        boolean alreadyRotating = RotationExecutor.isRotating();
        float referenceYaw = alreadyRotating ? RotationExecutor.getTargetYaw() : mc.player.getYRot();
        float referencePitch = alreadyRotating ? RotationExecutor.getTargetPitch() : mc.player.getXRot();
        float yawDrift = Math.abs(AngleUtils.getRotationDelta(
            referenceYaw, desiredRot.yaw));
        float pitchDrift = Math.abs(AngleUtils.getRotationDelta(
            referencePitch, desiredRot.pitch));

        debug(debug, "desired=(yaw=%.2f,pitch=%.2f) currentTarget=(yaw=%.2f,pitch=%.2f) drift=(yaw=%.2f,pitch=%.2f)",
            desiredRot.yaw,
            desiredRot.pitch,
            referenceYaw,
            referencePitch,
            yawDrift,
            pitchDrift);

        float yawThreshold = alreadyRotating ? ACTIVE_YAW_RETARGET_DEG : IDLE_YAW_DEADBAND_DEG;
        float pitchThreshold = alreadyRotating ? ACTIVE_PITCH_RETARGET_DEG : IDLE_PITCH_DEADBAND_DEG;
        long now = System.currentTimeMillis();
        if ((yawDrift > yawThreshold || pitchDrift > pitchThreshold)
            && (!alreadyRotating || now - lastRotationDispatchMs >= ROTATION_REDISPATCH_COOLDOWN_MS)) {
            long durationMs = 550;
            EasingType easing = USE_CAMERA_RAIL && !cameraPath.isEmpty()
                ? EasingType.EASE_OUT_CUBIC
                : computePathDifficulty(mc, path, pursuitSegment) < 0.4f
                    ? EasingType.EASE_OUT_BACK
                    : EasingType.EASE_OUT_CUBIC;
            debug(debug, "rotateTo dispatch easing=%s durationMs=%d wp=%d camTargetIdx=%d",
                easing, durationMs, pursuitSegment, camTargetIdx);
            RotationExecutor.rotateTo(desiredRot, new TimedEaseStrategy(easing, durationMs));
            lastRotationDispatchMs = now;
            return;
        }

        debug(debug, "rotateTo skipped small drift wp=%d camTargetIdx=%d",
            pursuitSegment, camTargetIdx);
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderConfig.SHOW_DEBUG.get()) {
            debug.accept(String.format(message, args));
        }
    }

    private float computePathDifficulty(Minecraft mc, List<Node> path, int pursuitSegment) {
        if (mc.level == null || mc.player == null || path == null || path.isEmpty()) {
            return 0f;
        }
        BlockPos pos = mc.player.blockPosition();

        int walls = 0;
        for (Direction dir : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}) {
            BlockPos adj = pos.relative(dir);
            if (mc.level.getBlockState(adj).canOcclude()) {
                walls++;
            }
            if (mc.level.getBlockState(adj.above()).canOcclude()) {
                walls++;
            }
        }
        float tightness = walls / 8.0f;

        boolean ascending = pursuitSegment < path.size()
            && MovementRegistry.get(path.get(pursuitSegment).moveType).countsAsAscendingDifficulty();
        return Math.min(1.0f, tightness + (ascending ? 0.4f : 0.0f));
    }

    private int pickCameraRailTarget(Vec3 playerPos) {
        if (cameraPath.isEmpty()) {
            return 0;
        }
        if (lastCameraCheckPos == null) {
            cameraIndex = nearestCameraRailIndex(playerPos);
            lastCameraCheckPos = playerPos;
        }

        double distMoved = lastCameraCheckPos.distanceTo(playerPos);
        if (distMoved >= CAMERA_RAIL_REACHED_DIST * 0.5) {
            lastCameraCheckPos = playerPos;
            while (cameraIndex + 1 < cameraPath.size() && hasPassedCameraNode(playerPos, cameraIndex)) {
                cameraIndex++;
            }
        }
        return cameraIndex;
    }

    private int nearestCameraRailIndex(Vec3 playerPos) {
        int bestIndex = Math.max(0, Math.min(cameraIndex, cameraPath.size() - 1));
        double bestDistSq = Double.MAX_VALUE;
        for (int i = 0; i < cameraPath.size(); i++) {
            Vec3 point = cameraPath.get(i);
            double dx = point.x - playerPos.x;
            double dy = point.y - playerPos.y;
            double dz = point.z - playerPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < bestDistSq - 1.0e-4
                || (Math.abs(distSq - bestDistSq) <= 1.0e-4 && i > bestIndex)) {
                bestDistSq = distSq;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private Vec3 getCameraRailGuideTarget(Vec3 playerPos, int idx) {
        int start = Math.max(0, Math.min(idx, cameraPath.size() - 1));
        if (start + 1 >= cameraPath.size()) {
            return cameraPath.getLast();
        }

        Vec3 a = cameraPath.get(start);
        Vec3 b = cameraPath.get(start + 1);
        double abx = b.x - a.x;
        double aby = b.y - a.y;
        double abz = b.z - a.z;
        double lenSq = abx * abx + aby * aby + abz * abz;
        double t = lenSq < 1.0e-6
            ? 0.0
            : ((playerPos.x - a.x) * abx + (playerPos.y - a.y) * aby + (playerPos.z - a.z) * abz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        Vec3 cursor = lerp(a, b, t);
        double remaining = CAMERA_RAIL_GUIDE_LOOKAHEAD_DIST;
        int seg = start;

        while (remaining > 0.0 && seg + 1 < cameraPath.size()) {
            Vec3 from = seg == start ? cursor : cameraPath.get(seg);
            Vec3 to = cameraPath.get(seg + 1);
            double segLen = from.distanceTo(to);
            if (segLen < 1.0e-6) {
                seg++;
                continue;
            }
            if (remaining <= segLen) {
                return lerp(from, to, remaining / segLen);
            }
            remaining -= segLen;
            seg++;
        }

        return cameraPath.getLast();
    }

    private boolean hasPassedCameraNode(Vec3 playerPos, int idx) {
        Vec3 cur = cameraPath.get(idx);
        double dx = cur.x - playerPos.x;
        double dz = cur.z - playerPos.z;
        if (Math.sqrt(dx * dx + dz * dz) <= CAMERA_RAIL_REACHED_DIST) {
            return true;
        }

        if (idx + 1 >= cameraPath.size()) {
            return false;
        }
        Vec3 next = cameraPath.get(idx + 1);
        double dirX = next.x - cur.x;
        double dirZ = next.z - cur.z;
        double segLenSq = dirX * dirX + dirZ * dirZ;
        if (segLenSq < 1.0e-6) {
            return false;
        }
        double toPlayerX = playerPos.x - cur.x;
        double toPlayerZ = playerPos.z - cur.z;
        double dot = toPlayerX * dirX + toPlayerZ * dirZ;
        return dot > segLenSq * 0.20;
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return new Vec3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t);
    }

    private record RotationTarget(String mode, int targetIndex, int visualizerIndex, Vec3 position) {
    }
}
