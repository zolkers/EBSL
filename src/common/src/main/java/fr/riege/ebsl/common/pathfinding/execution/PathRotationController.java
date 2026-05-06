package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementEvaluatorRegistry;
import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.rotation.EasingType;
import fr.riege.ebsl.common.pathfinding.rotation.Rotation;
import fr.riege.ebsl.common.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.common.pathfinding.rotation.TimedEaseStrategy;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

final class PathRotationController {
    private static final double PARKOUR_LANDING_LOOK_MIN_DIST = 0.70;
    private static final double PARKOUR_HARD_LANDING_LOOK_MIN_DIST = 0.50;
    private static final double PARKOUR_LANDING_LOOK_PASSED_DOT = -0.20;
    private static final double PARKOUR_HARD_LANDING_LOOK_PASSED_DOT = -0.05;
    private static final double PARKOUR_HARD_TURN_DOT = 0.82;
    private static final double PARKOUR_HARD_DRIFT_DISTANCE = 0.32;
    private static final double PARKOUR_HARD_DRIFT_TAKEOFF_DIST = 5.0;
    private static final int PARKOUR_NORMAL_LOOKAHEAD_NODES = 2;
    private static final int PARKOUR_HARD_LOOKAHEAD_NODES = 4;

    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final RotationExecutor rotationExecutor;
    private final PathTargetSelector targetSelector = new PathTargetSelector();
    private final PathPitchStabilizer pitchStabilizer = new PathPitchStabilizer();

    private List<Vec3d> cameraPath = Collections.emptyList();
    private int cameraIndex;
    private Vec3d lastCameraCheckPos;
    private int camTargetIdx = -1;
    private int lastRotationDebugCamTarget = -2;
    private long lastRotationDispatchMs;

    PathRotationController(IWorldLayer world, IPlayerLayer player, RotationExecutor rotationExecutor) {
        this.world = world;
        this.player = player;
        this.rotationExecutor = rotationExecutor;
    }

    void rebuild(List<Node> path) {
        this.cameraPath = PathfinderSettings.instance().useCameraRail.value() ? CameraRailBuilder.build(path) : Collections.emptyList();
        this.cameraIndex = 0;
        this.lastCameraCheckPos = null;
        this.camTargetIdx = -1;
        this.lastRotationDebugCamTarget = -2;
        this.lastRotationDispatchMs = 0;
        this.pitchStabilizer.reset();
    }

    void reset() {
        this.cameraPath = Collections.emptyList();
        this.cameraIndex = 0;
        this.lastCameraCheckPos = null;
        this.camTargetIdx = -1;
        this.lastRotationDebugCamTarget = -2;
        this.lastRotationDispatchMs = 0;
        this.pitchStabilizer.reset();
    }

    void updateRotation(Vec3d playerPos, List<Node> path, int pursuitSegment, Consumer<String> debug) {
        if (path == null || path.isEmpty()) {
            return;
        }

        boolean alreadyRotating = rotationExecutor.isRotating();
        RotationTarget rotationTarget = selectRotationTarget(playerPos, path, pursuitSegment, alreadyRotating);
        camTargetIdx = rotationTarget.visualizerIndex();

        if (lastRotationDebugCamTarget != camTargetIdx) {
            debug(debug, "cam target changed prev=%d current=%d pursuitSegment=%d",
                lastRotationDebugCamTarget, camTargetIdx, pursuitSegment);
            lastRotationDebugCamTarget = camTargetIdx;
        }

        debug(debug, "target pick mode=%s camTarget=%d player=(%.2f,%.2f,%.2f) rotTarget=(%.2f,%.2f,%.2f)",
            rotationTarget.mode(), rotationTarget.targetIndex(), playerPos.x(), playerPos.y(), playerPos.z(),
            rotationTarget.position().x(), rotationTarget.position().y(), rotationTarget.position().z());

        Rotation rawRot = AngleUtils.getRotation(player.eyePosition(), rotationTarget.position());
        Rotation desiredRot = pitchStabilizer.stabilize(player, rotationTarget.position(), rawRot, debug);
        dispatchRotationIfNeeded(path, pursuitSegment, debug, desiredRot, rotationTarget);
    }

    void tickExecutor() {
        rotationExecutor.update();
    }

    void stop() {
        rotationExecutor.stopRotating();
    }

    int getCamTargetIdx() {
        return camTargetIdx;
    }

    private RotationTarget selectRotationTarget(Vec3d playerPos, List<Node> path, int pursuitSegment, boolean alreadyRotating) {
        Optional<RotationTarget> parkourLanding = selectParkourLandingTarget(playerPos, path, pursuitSegment, alreadyRotating);
        if (parkourLanding.isPresent()) {
            return parkourLanding.get();
        }

        Optional<MovementSmoothing.Plan> smoothing = MovementSmoothingRegistry.resolve(path, pursuitSegment, alreadyRotating);
        if (smoothing.isPresent()) {
            MovementSmoothing.Plan plan = smoothing.get();
            return new RotationTarget(plan.mode(), plan.targetIndex(), plan.visualizerIndex(), plan.position(),
                plan.yawThreshold(), plan.pitchThreshold(), plan.durationMs(), plan.easing(), true);
        }

        if (PathfinderSettings.instance().useCameraRail.value() && !cameraPath.isEmpty()) {
            int camTarget = pickCameraRailTarget(playerPos);
            Vec3d rotTargetPos = getCameraRailGuideTarget(playerPos, camTarget);
            int visualizerIndex = Math.min(cameraPath.size() - 1, camTarget + 1);
            return defaultRotationTarget(path, pursuitSegment, alreadyRotating, "camera_rail", camTarget, visualizerIndex, rotTargetPos);
        }

        int camTarget = targetSelector.pickLegacyCamTarget(world, player.eyePosition(), playerPos, path, pursuitSegment);
        Node rotTarget = path.get(camTarget);
        Vec3d rotTargetPos = new Vec3d(
            rotTarget.position.centeredX(),
            rotTarget.position.flooredY() + PathfinderSettings.instance().legacyCameraEyeY.value(),
            rotTarget.position.centeredZ());
        return defaultRotationTarget(path, pursuitSegment, alreadyRotating, "legacy", camTarget, camTarget, rotTargetPos);
    }

    private Optional<RotationTarget> selectParkourLandingTarget(Vec3d playerPos, List<Node> path, int pursuitSegment,
                                                               boolean alreadyRotating) {
        int landingIndex = findParkourLandingIndex(path, pursuitSegment, PARKOUR_HARD_LOOKAHEAD_NODES);
        if (landingIndex < 0) {
            return Optional.empty();
        }

        boolean hardToPrepare = requiresParkourAnticipation(playerPos, path, landingIndex);
        if (landingIndex > Math.max(0, pursuitSegment) + PARKOUR_NORMAL_LOOKAHEAD_NODES && !hardToPrepare) {
            return Optional.empty();
        }

        Node takeoff = path.get(Math.max(0, landingIndex - 1));
        Node landing = path.get(landingIndex);
        if (shouldReleaseParkourLandingTarget(playerPos, takeoff, landing, hardToPrepare)) {
            return Optional.empty();
        }

        Vec3d landingTarget = new Vec3d(landing.position.centeredX(), landing.position.flooredY() + 1.0, landing.position.centeredZ());
        return Optional.of(new RotationTarget(
            hardToPrepare ? "parkour_landing_hard" : "parkour_landing",
            landingIndex,
            landingIndex,
            landingTarget,
            (float) (double) (alreadyRotating
                ? PathfinderSettings.instance().parkourActiveYawRetargetDeg.value()
                : PathfinderSettings.instance().parkourIdleYawDeadbandDeg.value()),
            (float) (double) (alreadyRotating
                ? PathfinderSettings.instance().parkourActivePitchRetargetDeg.value()
                : PathfinderSettings.instance().parkourIdlePitchDeadbandDeg.value()),
            PathfinderSettings.instance().parkourRotationDurationMs.value(),
            EasingType.EASE_OUT_CUBIC,
            true));
    }

    private static boolean shouldReleaseParkourLandingTarget(Vec3d playerPos, Node takeoff, Node landing, boolean hardToPrepare) {
        double tx = takeoff.position.centeredX();
        double tz = takeoff.position.centeredZ();
        double lx = landing.position.centeredX();
        double lz = landing.position.centeredZ();
        double pathDx = lx - tx;
        double pathDz = lz - tz;
        double pathLenSq = pathDx * pathDx + pathDz * pathDz;
        if (pathLenSq < 1.0e-6) {
            return true;
        }

        double toLandingX = lx - playerPos.x();
        double toLandingZ = lz - playerPos.z();
        double landingDist = Math.sqrt(toLandingX * toLandingX + toLandingZ * toLandingZ);
        double releaseDist = hardToPrepare ? PARKOUR_HARD_LANDING_LOOK_MIN_DIST : PARKOUR_LANDING_LOOK_MIN_DIST;
        if (landingDist <= releaseDist) {
            return true;
        }

        double playerProgressDot = ((playerPos.x() - tx) * pathDx + (playerPos.z() - tz) * pathDz) / pathLenSq;
        double landingAheadDot = (toLandingX * pathDx + toLandingZ * pathDz) / pathLenSq;
        double progressRelease = hardToPrepare ? 1.05 : 0.95;
        double passedDot = hardToPrepare ? PARKOUR_HARD_LANDING_LOOK_PASSED_DOT : PARKOUR_LANDING_LOOK_PASSED_DOT;
        return playerProgressDot >= progressRelease || landingAheadDot < passedDot;
    }

    private static boolean requiresParkourAnticipation(Vec3d playerPos, List<Node> path, int landingIndex) {
        if (landingIndex <= 0 || landingIndex >= path.size()) {
            return false;
        }

        int takeoffIndex = landingIndex - 1;
        Node takeoff = path.get(takeoffIndex);
        Node landing = path.get(landingIndex);
        double tx = takeoff.position.centeredX();
        double tz = takeoff.position.centeredZ();
        double lx = landing.position.centeredX();
        double lz = landing.position.centeredZ();
        double jumpDx = lx - tx;
        double jumpDz = lz - tz;
        double jumpLenSq = jumpDx * jumpDx + jumpDz * jumpDz;
        if (jumpLenSq < 1.0e-6) {
            return false;
        }

        boolean hardTurn = false;
        if (takeoffIndex > 0) {
            Node previous = path.get(takeoffIndex - 1);
            double prevDx = tx - previous.position.centeredX();
            double prevDz = tz - previous.position.centeredZ();
            double prevLenSq = prevDx * prevDx + prevDz * prevDz;
            if (prevLenSq > 1.0e-6) {
                double turnDot = (prevDx * jumpDx + prevDz * jumpDz) / Math.sqrt(prevLenSq * jumpLenSq);
                hardTurn = turnDot < PARKOUR_HARD_TURN_DOT;
            }
        }

        double jumpLen = Math.sqrt(jumpLenSq);
        double lateralDrift = Math.abs(((playerPos.x() - tx) * jumpDz - (playerPos.z() - tz) * jumpDx) / jumpLen);
        double toTakeoffX = tx - playerPos.x();
        double toTakeoffZ = tz - playerPos.z();
        double takeoffDist = Math.sqrt(toTakeoffX * toTakeoffX + toTakeoffZ * toTakeoffZ);
        boolean hardDrift = takeoffDist <= PARKOUR_HARD_DRIFT_TAKEOFF_DIST && lateralDrift >= PARKOUR_HARD_DRIFT_DISTANCE;

        return hardTurn || hardDrift;
    }

    private static int findParkourLandingIndex(List<Node> path, int pursuitSegment, int lookaheadNodes) {
        if (path == null || path.isEmpty()) {
            return -1;
        }
        int start = Math.max(0, pursuitSegment);
        int end = Math.min(path.size() - 1, start + lookaheadNodes);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == Node.MoveType.PARKOUR) {
                return i;
            }
        }
        return -1;
    }

    private void dispatchRotationIfNeeded(List<Node> path, int pursuitSegment, Consumer<String> debug,
                                          Rotation desiredRot, RotationTarget rotationTarget) {
        boolean alreadyRotating = rotationExecutor.isRotating();
        float referenceYaw = alreadyRotating ? rotationExecutor.getTargetYaw() : player.yaw();
        float referencePitch = alreadyRotating ? rotationExecutor.getTargetPitch() : player.pitch();
        float yawDrift = Math.abs(AngleUtils.getRotationDelta(referenceYaw, desiredRot.yaw));
        float pitchDrift = Math.abs(AngleUtils.getRotationDelta(referencePitch, desiredRot.pitch));

        debug(debug, "desired=(yaw=%.2f,pitch=%.2f) currentTarget=(yaw=%.2f,pitch=%.2f) drift=(yaw=%.2f,pitch=%.2f)",
            desiredRot.yaw, desiredRot.pitch, referenceYaw, referencePitch, yawDrift, pitchDrift);

        long now = System.currentTimeMillis();
        if ((yawDrift > rotationTarget.yawThreshold() || pitchDrift > rotationTarget.pitchThreshold())
            && (!alreadyRotating || now - lastRotationDispatchMs >= PathfinderSettings.instance().rotationRedispatchCooldownMs.value())) {
            debug(debug, "rotateTo dispatch easing=%s durationMs=%d wp=%d camTargetIdx=%d mode=%s registry=%s",
                rotationTarget.easing(), rotationTarget.durationMs(), pursuitSegment, camTargetIdx,
                rotationTarget.mode(), rotationTarget.registry());
            rotationExecutor.rotateTo(desiredRot, new TimedEaseStrategy(rotationTarget.easing(), rotationTarget.durationMs()));
            lastRotationDispatchMs = now;
            return;
        }

        debug(debug, "rotateTo skipped small drift wp=%d camTargetIdx=%d", pursuitSegment, camTargetIdx);
    }

    private RotationTarget defaultRotationTarget(List<Node> path, int pursuitSegment, boolean alreadyRotating,
                                                 String mode, int targetIndex, int visualizerIndex, Vec3d position) {
        boolean cameraRail = "camera_rail".equals(mode);
        EasingType easing = cameraRail
            ? EasingType.EASE_OUT_CUBIC
            : computePathDifficulty(path, pursuitSegment) < 0.4f
                ? EasingType.EASE_OUT_BACK
                : EasingType.EASE_OUT_CUBIC;
        return new RotationTarget(
            mode,
            targetIndex,
            visualizerIndex,
            position,
            (float) (double) (alreadyRotating
                ? PathfinderSettings.instance().activeYawRetargetDeg.value()
                : PathfinderSettings.instance().idleYawDeadbandDeg.value()),
            (float) (double) (alreadyRotating
                ? PathfinderSettings.instance().activePitchRetargetDeg.value()
                : PathfinderSettings.instance().idlePitchDeadbandDeg.value()),
            PathfinderSettings.instance().rotationDurationMs.value(),
            easing,
            false);
    }

    private float computePathDifficulty(List<Node> path, int pursuitSegment) {
        Vec3d pos = player.position();
        int x = (int) Math.floor(pos.x());
        int y = (int) Math.floor(pos.y());
        int z = (int) Math.floor(pos.z());
        int walls = 0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : dirs) {
            if (world.isSolid(x + dir[0], y, z + dir[1])) walls++;
            if (world.isSolid(x + dir[0], y + 1, z + dir[1])) walls++;
        }
        float tightness = walls / 8.0f;
        boolean ascending = pursuitSegment < path.size()
            && MovementEvaluatorRegistry.get(path.get(pursuitSegment).moveType).countsAsAscendingDifficulty();
        return Math.min(1.0f, tightness + (ascending ? 0.4f : 0.0f));
    }

    private int pickCameraRailTarget(Vec3d playerPos) {
        if (cameraPath.isEmpty()) {
            return 0;
        }
        if (lastCameraCheckPos == null) {
            cameraIndex = nearestCameraRailIndex(playerPos);
            lastCameraCheckPos = playerPos;
        }

        double distMoved = lastCameraCheckPos.distanceTo(playerPos);
        if (distMoved >= PathfinderSettings.instance().cameraRailReachedDist.value() * 0.5) {
            lastCameraCheckPos = playerPos;
            while (cameraIndex + 1 < cameraPath.size() && hasPassedCameraNode(playerPos, cameraIndex)) {
                cameraIndex++;
            }
        }
        return cameraIndex;
    }

    private int nearestCameraRailIndex(Vec3d playerPos) {
        int bestIndex = Math.max(0, Math.min(cameraIndex, cameraPath.size() - 1));
        double bestDistSq = Double.MAX_VALUE;
        for (int i = 0; i < cameraPath.size(); i++) {
            Vec3d point = cameraPath.get(i);
            double distSq = point.distanceToSq(playerPos);
            if (distSq < bestDistSq - 1.0e-4
                || (Math.abs(distSq - bestDistSq) <= 1.0e-4 && i > bestIndex)) {
                bestDistSq = distSq;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private Vec3d getCameraRailGuideTarget(Vec3d playerPos, int idx) {
        int start = Math.max(0, Math.min(idx, cameraPath.size() - 1));
        if (start + 1 >= cameraPath.size()) {
            return cameraPath.getLast();
        }

        Vec3d a = cameraPath.get(start);
        Vec3d b = cameraPath.get(start + 1);
        double abx = b.x() - a.x();
        double aby = b.y() - a.y();
        double abz = b.z() - a.z();
        double lenSq = abx * abx + aby * aby + abz * abz;
        double t = lenSq < 1.0e-6
            ? 0.0
            : ((playerPos.x() - a.x()) * abx + (playerPos.y() - a.y()) * aby + (playerPos.z() - a.z()) * abz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        Vec3d cursor = lerp(a, b, t);
        double remaining = PathfinderSettings.instance().cameraRailGuideLookaheadDist.value();
        int seg = start;

        while (remaining > 0.0 && seg + 1 < cameraPath.size()) {
            Vec3d from = seg == start ? cursor : cameraPath.get(seg);
            Vec3d to = cameraPath.get(seg + 1);
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

    private boolean hasPassedCameraNode(Vec3d playerPos, int idx) {
        Vec3d cur = cameraPath.get(idx);
        double dx = cur.x() - playerPos.x();
        double dz = cur.z() - playerPos.z();
        if (Math.sqrt(dx * dx + dz * dz) <= PathfinderSettings.instance().cameraRailReachedDist.value()) {
            return true;
        }

        if (idx + 1 >= cameraPath.size()) {
            return false;
        }
        Vec3d next = cameraPath.get(idx + 1);
        double dirX = next.x() - cur.x();
        double dirZ = next.z() - cur.z();
        double segLenSq = dirX * dirX + dirZ * dirZ;
        if (segLenSq < 1.0e-6) {
            return false;
        }
        double toPlayerX = playerPos.x() - cur.x();
        double toPlayerZ = playerPos.z() - cur.z();
        double dot = toPlayerX * dirX + toPlayerZ * dirZ;
        return dot > segLenSq * 0.20;
    }

    private static Vec3d lerp(Vec3d a, Vec3d b, double t) {
        return new Vec3d(
            a.x() + (b.x() - a.x()) * t,
            a.y() + (b.y() - a.y()) * t,
            a.z() + (b.z() - a.z()) * t);
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderSettings.instance().showDebug.value()) {
            debug.accept(String.format(message, args));
        }
    }

    private record RotationTarget(String mode, int targetIndex, int visualizerIndex, Vec3d position,
                                  float yawThreshold, float pitchThreshold, long durationMs,
                                  EasingType easing, boolean registry) {
    }
}
