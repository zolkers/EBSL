package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.evaluation.MovementEvaluatorRegistry;
import fr.riege.ebsl.common.pathfinding.rotation.*;
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
    private static final int PARKOUR_NORMAL_LOOKAHEAD_NODES = 1;
    private static final int PARKOUR_HARD_LOOKAHEAD_NODES = 4;

    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final RotationExecutor rotationExecutor;
    private final PathTargetSelector targetSelector = new PathTargetSelector();
    private final PathPitchStabilizer pitchStabilizer = new PathPitchStabilizer();
    private final CameraTargetSmoother targetSmoother = new CameraTargetSmoother();

    private List<Vec3d> cameraPath = Collections.emptyList();
    private int cameraIndex;
    private Vec3d lastCameraCheckPos;
    private int camTargetIdx = -1;
    private int lastRotationDebugCamTarget = -2;
    private long lastRotationDispatchMs;
    private long lastPitchUpdateNanos;
    private boolean active;

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
        this.lastPitchUpdateNanos = 0;
        this.active = false;
        this.pitchStabilizer.reset(player.pitch());
        this.targetSmoother.reset(player.eyePosition().y());
    }

    void reset() {
        this.cameraPath = Collections.emptyList();
        this.cameraIndex = 0;
        this.lastCameraCheckPos = null;
        this.camTargetIdx = -1;
        this.lastRotationDebugCamTarget = -2;
        this.lastRotationDispatchMs = 0;
        this.lastPitchUpdateNanos = 0;
        this.active = false;
        this.pitchStabilizer.reset(player.pitch());
        this.targetSmoother.clear();
    }

    void updateRotation(Vec3d playerPos, List<Node> path, int pursuitSegment, Consumer<String> debug) {
        if (path == null || path.isEmpty()) {
            return;
        }
        active = true;

        boolean alreadyRotating = rotationExecutor.isRotating();
        RotationTarget rotationTarget = selectRotationTarget(playerPos, path, pursuitSegment, alreadyRotating);
        rotationTarget = keepRotationTargetAhead(playerPos, path, pursuitSegment, rotationTarget);
        rotationTarget = stabilizeTargetHeight(rotationTarget);
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

        Vec3d eye = player.eyePosition();
        double dx = rotationTarget.position().x() - eye.x();
        double dz = rotationTarget.position().z() - eye.z();
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        float candidatePitch = horizDist >= PathfinderSettings.instance().pitchMinHorizontalDistance.value()
            ? rawRot.pitch : 0f;
        pitchStabilizer.tick(candidatePitch, player.isInWater(), consumePitchDtSeconds());
        debug(debug, "pitch spring stable=%.2f candidate=%.2f horizDist=%.2f",
            pitchStabilizer.getStablePitch(), candidatePitch, horizDist);

        dispatchYawIfNeeded(pursuitSegment, debug, rawRot.yaw, rotationTarget);
    }

    void renderFrame() {
        if (!active) {
            return;
        }
        rotationExecutor.update(pitchStabilizer.getStablePitch());
    }

    void stop() {
        active = false;
        rotationExecutor.stopRotating();
    }

    int getCamTargetIdx() {
        return camTargetIdx;
    }

    int getCameraIndex() {
        return cameraIndex;
    }

    List<Vec3d> getCameraPath() {
        return cameraPath;
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
                plan.yawThreshold(), plan.pitchThreshold(), plan.durationMs(), plan.yawEasing(), plan.pitchEasing(), true);
        }

        if (PathfinderSettings.instance().useCameraRail.value() && !cameraPath.isEmpty()) {
            int camTarget = pickCameraRailTarget(playerPos);
            Vec3d rotTargetPos = getCameraRailGuideTarget(playerPos, camTarget);
            rotTargetPos = naturalizeCameraTarget(playerPos, path, pursuitSegment, rotTargetPos);
            int visualizerIndex = Math.clamp(camTarget + 1, 0, cameraPath.size() - 1);
            return defaultRotationTarget(path, pursuitSegment, alreadyRotating, "camera_rail", camTarget, visualizerIndex, rotTargetPos);
        }

        int camTarget = targetSelector.pickLegacyCamTarget(world, player.eyePosition(), playerPos, path, pursuitSegment);
        Node rotTarget = path.get(camTarget);
        Vec3d rotTargetPos = new Vec3d(
            rotTarget.position.centeredX(),
            rotTarget.position.flooredY() + PathfinderSettings.instance().legacyCameraEyeY.value(),
            rotTarget.position.centeredZ());
        rotTargetPos = naturalizeCameraTarget(playerPos, path, pursuitSegment, rotTargetPos);
        return defaultRotationTarget(path, pursuitSegment, alreadyRotating, "legacy", camTarget, camTarget, rotTargetPos);
    }

    private Optional<RotationTarget> selectParkourLandingTarget(Vec3d playerPos, List<Node> path, int pursuitSegment,
                                                               boolean alreadyRotating) {
        int landingIndex = findParkourLandingIndex(path, pursuitSegment, PARKOUR_HARD_LOOKAHEAD_NODES);
        if (landingIndex < 0) {
            return Optional.empty();
        }

        boolean hardToPrepare = requiresParkourAnticipation(playerPos, path, landingIndex);
        int clampedPursuitSegment = Math.clamp(pursuitSegment, 0, path.size() - 1);
        if (landingIndex > clampedPursuitSegment + PARKOUR_NORMAL_LOOKAHEAD_NODES && !hardToPrepare) {
            return Optional.empty();
        }

        Node takeoff = path.get((int) Math.clamp((long) landingIndex - 1L, 0L, (long) path.size() - 1L));
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
            EasingType.EASE_OUT_SINE,
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
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = (int) Math.clamp((long) start + lookaheadNodes, 0L, (long) path.size() - 1L);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == Node.MoveType.PARKOUR) {
                return i;
            }
        }
        return -1;
    }

    private void dispatchYawIfNeeded(int pursuitSegment, Consumer<String> debug,
                                      float desiredYaw, RotationTarget rotationTarget) {
        boolean alreadyRotating = rotationExecutor.isRotating();
        float referenceYaw = alreadyRotating ? rotationExecutor.getTargetYaw() : player.yaw();
        float yawDrift = Math.abs(AngleUtils.getRotationDelta(referenceYaw, desiredYaw));

        debug(debug, "desired yaw=%.2f reference=%.2f drift=%.2f", desiredYaw, referenceYaw, yawDrift);

        long now = System.currentTimeMillis();
        if (yawDrift > rotationTarget.yawThreshold()
            && (!alreadyRotating || now - lastRotationDispatchMs
                >= PathfinderSettings.instance().rotationRedispatchCooldownMs.value())) {
            debug(debug, "rotateTo dispatch easing=%s/%s durationMs=%d wp=%d camTargetIdx=%d mode=%s registry=%s",
                rotationTarget.yawEasing(), rotationTarget.pitchEasing(), rotationTarget.durationMs(), pursuitSegment, camTargetIdx,
                rotationTarget.mode(), rotationTarget.registry());
            rotationExecutor.rotateTo(new Rotation(desiredYaw, 0f),
                new TimedEaseStrategy(rotationTarget.yawEasing(), rotationTarget.pitchEasing(), rotationTarget.durationMs()));
            lastRotationDispatchMs = now;
            return;
        }

        debug(debug, "rotateTo skipped small yaw drift wp=%d camTargetIdx=%d", pursuitSegment, camTargetIdx);
    }

    private RotationTarget defaultRotationTarget(List<Node> path, int pursuitSegment, boolean alreadyRotating,
                                                 String mode, int targetIndex, int visualizerIndex, Vec3d position) {
        boolean cameraRail = "camera_rail".equals(mode);
        EasingType yawEasing;
        EasingType pitchEasing;

        if (cameraRail) {
            yawEasing = EasingType.EASE_IN_OUT_SINE;
            pitchEasing = EasingType.EASE_OUT_SINE;
        } else {
            float difficulty = computePathDifficulty(path, pursuitSegment);
            yawEasing = difficulty < 0.4f
                ? EasingType.EASE_IN_OUT_SINE
                : EasingType.EASE_OUT_CUBIC;
            pitchEasing = EasingType.EASE_OUT_SINE;
        }

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
            yawEasing,
            pitchEasing,
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
        return Math.clamp(tightness + (ascending ? 0.4f : 0.0f), Float.NEGATIVE_INFINITY, 1.0f);
    }

    private Vec3d naturalizeCameraTarget(Vec3d playerPos, List<Node> path, int pursuitSegment, Vec3d target) {
        PathfinderSettings settings = PathfinderSettings.instance();
        double blend = settings.cameraNaturalFocusBlend.value();
        double lateral = settings.cameraNaturalLateralOffset.value();
        double vertical = settings.cameraNaturalVerticalOffset.value();
        if (target == null || blend >= 0.999 && lateral <= 1.0e-6 && Math.abs(vertical) <= 1.0e-6) {
            return target;
        }

        Vec3d eye = player.eyePosition();
        double tx = eye.x() + (target.x() - eye.x()) * blend;
        double ty = target.y() + vertical;
        double tz = eye.z() + (target.z() - eye.z()) * blend;

        double dirX = target.x() - playerPos.x();
        double dirZ = target.z() - playerPos.z();
        double dirLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (dirLen > 1.0e-6 && lateral > 1.0e-6) {
            double turn = pathTurnSign(path, pursuitSegment);
            double side = turn == 0.0 ? 0.35 : turn;
            tx += (-dirZ / dirLen) * lateral * side;
            tz += (dirX / dirLen) * lateral * side;
        }

        return new Vec3d(tx, ty, tz);
    }

    private static double pathTurnSign(List<Node> path, int pursuitSegment) {
        if (path == null || path.size() < 3) {
            return 0.0;
        }
        int aIdx = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int bIdx = Math.clamp(aIdx + 1, 0, path.size() - 1);
        int cIdx = Math.clamp(aIdx + 2, 0, path.size() - 1);
        if (aIdx == bIdx || bIdx == cIdx) {
            return 0.0;
        }
        Node a = path.get(aIdx);
        Node b = path.get(bIdx);
        Node c = path.get(cIdx);
        double abx = b.position.centeredX() - a.position.centeredX();
        double abz = b.position.centeredZ() - a.position.centeredZ();
        double bcx = c.position.centeredX() - b.position.centeredX();
        double bcz = c.position.centeredZ() - b.position.centeredZ();
        double cross = abx * bcz - abz * bcx;
        if (Math.abs(cross) < 1.0e-6) {
            return 0.0;
        }
        return Math.signum(cross);
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
        while (cameraIndex + 1 < cameraPath.size() && hasPassedCameraNode(playerPos, cameraIndex)) {
            cameraIndex++;
        }
        if (distMoved >= PathfinderSettings.instance().cameraRailReachedDist.value() * 0.5) {
            lastCameraCheckPos = playerPos;
            cameraIndex = Math.max(cameraIndex, nearestCameraRailIndex(playerPos));
        }
        return cameraIndex;
    }

    private int nearestCameraRailIndex(Vec3d playerPos) {
        int bestIndex = Math.clamp(cameraIndex, 0, cameraPath.size() - 1);
        double bestDistSq = Double.MAX_VALUE;
        for (int i = bestIndex; i < cameraPath.size(); i++) {
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

    private RotationTarget keepRotationTargetAhead(Vec3d playerPos, List<Node> path, int pursuitSegment, RotationTarget target) {
        Vec3d forward = pathForward(path, pursuitSegment);
        if (forward == null) {
            return target;
        }

        Vec3d eye = player.eyePosition();
        double tx = target.position().x() - eye.x();
        double tz = target.position().z() - eye.z();
        double targetLen = Math.sqrt(tx * tx + tz * tz);
        if (targetLen < 1.0e-6) {
            return target;
        }

        double dot = (tx / targetLen) * forward.x() + (tz / targetLen) * forward.z();
        if (dot >= PathfinderSettings.instance().cameraMinForwardDot.value()) {
            return target;
        }

        int fallbackIndex = (int) Math.clamp(
            (long) pursuitSegment + Math.max(1, PathfinderSettings.instance().cameraLookahead.value() / 4),
            0L,
            (long) path.size() - 1L);
        Node fallback = path.get(fallbackIndex);
        Vec3d position = new Vec3d(
            fallback.position.centeredX(),
            fallback.position.flooredY() + PathfinderSettings.instance().legacyCameraEyeY.value(),
            fallback.position.centeredZ());
        position = naturalizeCameraTarget(playerPos, path, pursuitSegment, position);
        return new RotationTarget(
            target.mode() + "_forward_guard",
            fallbackIndex,
            fallbackIndex,
            position,
            target.yawThreshold(),
            target.pitchThreshold(),
            target.durationMs(),
            target.yawEasing(),
            target.pitchEasing(),
            target.registry());
    }

    private RotationTarget stabilizeTargetHeight(RotationTarget target) {
        Vec3d smoothed = targetSmoother.smooth(target.position(), player.eyePosition());
        if (smoothed == target.position()) {
            return target;
        }
        return new RotationTarget(
            target.mode(),
            target.targetIndex(),
            target.visualizerIndex(),
            smoothed,
            target.yawThreshold(),
            target.pitchThreshold(),
            target.durationMs(),
            target.yawEasing(),
            target.pitchEasing(),
            target.registry());
    }

    private static Vec3d pathForward(List<Node> path, int pursuitSegment) {
        if (path == null || path.size() < 2) {
            return null;
        }

        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        Node from = path.get(start);
        for (int i = start + 1; i < path.size(); i++) {
            Node to = path.get(i);
            double dx = to.position.centeredX() - from.position.centeredX();
            double dz = to.position.centeredZ() - from.position.centeredZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 1.0e-6) {
                return new Vec3d(dx / len, 0.0, dz / len);
            }
        }
        return null;
    }

    private Vec3d getCameraRailGuideTarget(Vec3d playerPos, int idx) {
        int start = Math.clamp(idx, 0, cameraPath.size() - 1);
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
        t = Math.clamp(t, 0.0, 1.0);

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

    private double consumePitchDtSeconds() {
        long now = System.nanoTime();
        if (lastPitchUpdateNanos == 0) {
            lastPitchUpdateNanos = now;
            return 0.05;
        }
        double dt = (now - lastPitchUpdateNanos) / 1_000_000_000.0;
        lastPitchUpdateNanos = now;
        return Math.clamp(dt, 0.002, 0.10);
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderSettings.instance().showDebug.value()) {
            debug.accept(String.format(message, args));
        }
    }

    private record RotationTarget(String mode, int targetIndex, int visualizerIndex, Vec3d position,
                                  float yawThreshold, float pitchThreshold, long durationMs,
                                  EasingType yawEasing, EasingType pitchEasing, boolean registry) {
    }
}
