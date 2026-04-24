package fr.riege.ebsl.pathfinding.execution;


import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.check.PathCheckContext;
import fr.riege.ebsl.pathfinding.check.PathCheckRegistry;
import fr.riege.ebsl.pathfinding.check.PathCheckResult;
import fr.riege.ebsl.pathfinding.check.PathProximitySnapshot;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

/**
 * State machine that walks a player along a pre-computed path using keyboard injection.
 * Includes jump handling and automatic stall detection.
 */
public final class PathExecutor {

    public enum State { IDLE, WALKING, REPLANNING, FINISHED, FAILED }

    // Stall detection thresholds
    private static final double STUCK_DIST_THRESHOLD = 0.2;
    private static final long   STUCK_TIME_MS        = 2000;
    private static final double DRIFT_DISTANCE       = 4.5;
    private static final long   REPLAN_COOLDOWN_MS   = 5000;

    // Jump triggers
    static final double JUMP_TRIGGER_DIST      = 0.6;
    static final double STEP_UP_TRIGGER_DIST   = 1.0;
    static final int    JUMP_COOLDOWN_TICKS = 8;
    static final long   STALL_JUMP_PROGRESS_MS = 450;

    // Anti-unstuck escalation
    private static final long   UNSTUCK_JUMP_MS    = 1200;  // try jumping after 1.2s
    private static final long   UNSTUCK_BACKUP_MS  = 2200;  // try backing up after 2.2s
    private static final int    BACKUP_TICKS       = 8;      // back up for 8 ticks
    private static final long   PATH_REPLAN_STALE_MS = 1400;
    private static final double PATH_REPLAN_DRIFT_DISTANCE = 1.75;
    private static final double PATH_PROGRESS_EPSILON = 0.08;
    private static final long   GROUNDED_NO_PROGRESS_REPLAN_MS = 1000;
    private static final long   PATH_REPLAN_HARD_STALE_MS = 4200;

    static final double WALK_TARGET_DEADZONE = 0.28;
    static final double WALK_FORWARD_DOT     = 0.18;
    static final double WALK_BACKWARD_DOT    = -0.45;
    static final double WALK_STRAFE_DOT      = 0.32;

    private static final long   COAST_TIMEOUT_MS    = 3000;

    /** Direct goal proximity check - if player is this close horizontally, finish. */
    private static final double GOAL_REACHED_HDIST = 1.2;
    private static final double GOAL_REACHED_VDIST = 2.0;

    private State      state = State.IDLE;
    private boolean    precise;

    private long lastReplanTime  = 0;
    private long coastStartTime  = 0;
    private int  jumpCooldown    = 0;
    private int  goalX, goalY, goalZ;
    private double goalCenterX   = 0.5;
    private double goalCenterZ   = 0.5;
    private Entity rotationTarget;
    private Vec3 lookTargetPos   = null;
    private boolean allowReplan  = true;
    private boolean allowJumps   = true;
    private boolean allowRotation = true;
    private boolean exactGoalCentering = false;
    private double stickySneakDistance = -1.0;
    private boolean sneakLatched = false;
    private double preciseGoalTolerance = 0.5;
    private Runnable onFinished;
    private int  debugTick       = 0;
    private int  backupTicksLeft = 0;
    private boolean replanFromPlayerRequested = false;
    private WalkMovementController movementController;
    private final PathTracker pathTracker = new PathTracker();
    private final PathRotationController rotationController = new PathRotationController();

    // --- Public API ----------------------------------------------------------

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise) {
        start(path, goalX, goalY, goalZ, precise, null);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise, Entity rotationTarget) {
        start(path, goalX, goalY, goalZ, precise, rotationTarget, null);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, boolean precise,
                      Entity rotationTarget, Runnable onFinished) {
        this.goalX            = goalX;
        this.goalY            = goalY;
        this.goalZ            = goalZ;
        this.goalCenterX      = 0.5;
        this.goalCenterZ      = 0.5;
        this.precise          = precise;
        this.rotationTarget   = rotationTarget;
        this.lookTargetPos    = null;
        this.allowReplan      = true;
        this.allowJumps       = true;
        this.allowRotation    = true;
        this.exactGoalCentering = false;
        this.stickySneakDistance = -1.0;
        this.preciseGoalTolerance = 0.5;
        this.onFinished       = onFinished;
        this.jumpCooldown     = 0;
        this.coastStartTime   = 0;
        this.backupTicksLeft  = 0;
        this.replanFromPlayerRequested = false;
        pathTracker.start(path);
        rotationController.rebuild(path);
        this.movementController = new WalkMovementController(path);
        state = State.WALKING;
    }

    public void setLookTarget(Vec3 lookTargetPos) {
        this.lookTargetPos = lookTargetPos;
    }

    public void setAllowReplan(boolean allowReplan) {
        this.allowReplan = allowReplan;
    }

    public void setPreciseGoalTolerance(double preciseGoalTolerance) {
        this.preciseGoalTolerance = Math.max(0.01, preciseGoalTolerance);
    }

    public void setAllowJumps(boolean allowJumps) {
        this.allowJumps = allowJumps;
    }

    public void setAllowRotation(boolean allowRotation) {
        this.allowRotation = allowRotation;
    }

    public void setExactGoalCentering(boolean exactGoalCentering) {
        this.exactGoalCentering = exactGoalCentering;
    }

    public void setStickySneakDistance(double stickySneakDistance) {
        this.stickySneakDistance = stickySneakDistance;
    }

    public void setSneakLatched(boolean sneakLatched) {
        this.sneakLatched = sneakLatched;
    }

    public void setGoalCenterOffsets(double goalCenterX, double goalCenterZ) {
        this.goalCenterX = goalCenterX;
        this.goalCenterZ = goalCenterZ;
    }

    public boolean isSneakLatched() {
        return sneakLatched;
    }

    public State getState() { return state; }
    public int getWaypointIndex() { return pathTracker.getPursuitSegment(); }
    public int getCamTargetIdx()  { return rotationController.getCamTargetIdx(); }
    public List<Vec3> getCameraPath() { return rotationController.getCameraPath(); }
    public List<Node> getPathSnapshot() { return pathTracker.getPathSnapshot(); }
    public int getCameraIndex() { return rotationController.getCameraIndex(); }
    void setJumpCooldown(int jumpCooldown) { this.jumpCooldown = jumpCooldown; }
    public double getProgressRatio(Vec3 playerPos) {
        return pathTracker.getProgressRatio(playerPos);
    }

    public double getRemainingDistance(Vec3 playerPos) {
        return pathTracker.getRemainingDistance(playerPos);
    }

    public boolean consumeReplanFromPlayerRequest() {
        boolean requested = replanFromPlayerRequested;
        replanFromPlayerRequested = false;
        return requested;
    }

    public void continueWith(List<Node> continuationPath, int goalX, int goalY, int goalZ) {
        if (continuationPath == null || continuationPath.isEmpty()) {
            return;
        }
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        pathTracker.continueWith(continuationPath);
        List<Node> mergedPath = pathTracker.getPath();
        rotationController.rebuild(mergedPath);
        this.movementController = new WalkMovementController(mergedPath);
    }

    public void tick(Minecraft mc) {
        if (mc.player == null) return;
        if (state != State.WALKING && state != State.REPLANNING) return;
        if (ClientUtils.isInventoryScreenOpen(mc)) {
            releaseAll(mc);
            return;
        }

        if (mc.player.getAbilities().flying) {
            releaseAll(mc);
            mc.options.keyShift.setDown(true);
            return;
        }

        jumpCooldown = Math.max(0, jumpCooldown - 1);

        Vec3 playerPos = mc.player.position();

        List<Node> path = pathTracker.getPath();
        if (path == null || path.isEmpty()) {
            finish(mc);
            return;
        }

        // -- Direct goal proximity check -------------------------------------
        // Catches cases where waypoint advancement gets stuck but player is at the goal.
        {
            double gx = goalX + 0.5, gz = goalZ + 0.5;
            double gdx = gx - playerPos.x, gdz = gz - playerPos.z;
            double gHDist = Math.sqrt(gdx * gdx + gdz * gdz);
            double gVDist = Math.abs(goalY - playerPos.y);
            if (gHDist <= GOAL_REACHED_HDIST && gVDist <= GOAL_REACHED_VDIST) {
                finish(mc);
                return;
            }
        }

        // -- Movement progress tracking -------------------------------------
        double distMoved = pathTracker.noteMovementProgress(playerPos, STUCK_DIST_THRESHOLD);

        long now = System.currentTimeMillis();
        pathTracker.advancePursuit(playerPos, now);

        pathTracker.computeAndTrackPathProgress(playerPos, PATH_PROGRESS_EPSILON, now);

        PathProximitySnapshot proximity = pathTracker.analyzePathProximity(playerPos);
        pathTracker.updateSevereOffPathState(proximity, now);

        PathCheckResult pathCheckResult = PathCheckRegistry.evaluate(new PathCheckContext(
            playerPos,
            path,
            pathTracker.getPursuitSegment(),
            goalX,
            goalY,
            goalZ,
            pathTracker.getSevereOffPathDuration(now),
            proximity,
            pathTracker.checkpointSnapshot(playerPos, proximity, now)
        ));
        if (pathCheckResult.isForceReplan()) {
            triggerReplan(mc, true, true, pathCheckResult.reason());
            return;
        }
        if (pathCheckResult.isCutoff()) {
            if (pathTracker.applySmartCutoff(pathCheckResult.cutoffSegmentIndex())) {
                path = pathTracker.getPath();
                rotationController.rebuild(path);
                movementController = new WalkMovementController(path);
                PathVisualizer.setPath(path, 0);
                PathVisualizer.setCameraPath(rotationController.getCameraPath());
                PathVisualizer.updateExecution(pathTracker.getPursuitSegment(), rotationController.getCamTargetIdx());
                proximity = pathTracker.analyzePathProximity(playerPos);
            }
        }

        // Periodic debug
        if (PathfinderConfig.SHOW_DEBUG.get() && ++debugTick % 40 == 0 && pathTracker.getPursuitSegment() < path.size()) {
            Node wp   = path.get(pathTracker.getPursuitSegment());
            double dx = wp.position.centeredX() - playerPos.x;
            double dz = wp.position.centeredZ() - playerPos.z;
            double hDist = Math.sqrt(dx * dx + dz * dz);
            ClientUtils.sendDebugMessage(mc, String.format(
                    "path seg=%d/%d hDist=%.2f",
                    pathTracker.getPursuitSegment(), path.size(), hDist));
        }

        // -- Goal reached / coasting ----------------------------------------
        if (pathTracker.getPursuitSegment() >= path.size() - 1) {
            if (!precise) {
                // Approximate mode: release all keys immediately
                finish(mc);
                return;
            }

            // Precise mode: try to reach the exact block center horizontally.
            if (coastStartTime == 0) coastStartTime = System.currentTimeMillis();
            double dx = (goalX + goalCenterX) - playerPos.x;
            double dz = (goalZ + goalCenterZ) - playerPos.z;
            double hDist = Math.sqrt(dx * dx + dz * dz);

            if (exactGoalCentering && hDist > preciseGoalTolerance) {
                applyGoalCenteringMovement(mc, dx, dz);
            } else {
                releaseAll(mc);
            }

            if (hDist <= preciseGoalTolerance
                    || System.currentTimeMillis() - coastStartTime > COAST_TIMEOUT_MS) {
                finish(mc);
            }
            return;
        }

        // -- Stall + drift detection with escalating recovery ----------------
        long    staleDuration = now - pathTracker.getLastProgressTime();
        long    pathStaleDuration = now - pathTracker.getLastPathProgressTime();
        boolean progressStale = staleDuration > STUCK_TIME_MS;
        boolean pathProgressStale = pathStaleDuration > PATH_REPLAN_STALE_MS;
        double  hDistToPath   = proximity.horizontalDistance();
        boolean drifted       = hDistToPath > DRIFT_DISTANCE;
        boolean cooldownPassed = now - lastReplanTime > REPLAN_COOLDOWN_MS;

        if (allowReplan
                && mc.player.onGround()
                && pathStaleDuration > GROUNDED_NO_PROGRESS_REPLAN_MS) {
            triggerReplan(mc, true, true, String.format("grounded no progress stale=%d", pathStaleDuration));
            return;
        }

        if (allowReplan && pathStaleDuration > PATH_REPLAN_HARD_STALE_MS && cooldownPassed) {
            triggerReplan(mc, false, true, "hard stale path progress");
            return;
        }

        // Handle backup movement if active
        if (backupTicksLeft > 0) {
            backupTicksLeft--;
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(true);
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);
            mc.options.keyJump.setDown(false);
            mc.options.keySprint.setDown(false);
            if (backupTicksLeft == 0) {
                mc.options.keyDown.setDown(false);
            }
            return;
        }

        // Escalating unstuck: jump -> backup -> replan
        boolean recoveryJump = false;
        if (staleDuration > UNSTUCK_BACKUP_MS && !drifted) {
            // Try backing up briefly to unstick from block edges
            if (backupTicksLeft == 0 && mc.player.onGround()) {
                backupTicksLeft = BACKUP_TICKS;
                pathTracker.noteMovementProgress(playerPos, 0.0);
                return;
            }
        } else if (staleDuration > UNSTUCK_JUMP_MS && !drifted) {
            // Try a jump to get unstuck from block edges
            if (mc.player.onGround() && jumpCooldown == 0) {
                mc.options.keyJump.setDown(true);
                jumpCooldown = JUMP_COOLDOWN_TICKS;
                recoveryJump = true;
            }
        }

        if (allowReplan && pathProgressStale && cooldownPassed
                && (hDistToPath > PATH_REPLAN_DRIFT_DISTANCE || distMoved >= STUCK_DIST_THRESHOLD)) {
            triggerReplan(mc, false, true, String.format("path progress stale drift=%.2f", hDistToPath));
            return;
        }

        if (allowReplan && progressStale && drifted && cooldownPassed) {
            triggerReplan(mc, false, true, String.format("drift stale=%.2f", hDistToPath));
            return;
        }

        // Replan if stuck for too long even without drift (true deadlock)
        if (staleDuration > STUCK_TIME_MS * 2 && cooldownPassed) {
            triggerReplan(mc, false, true, String.format("deadlock stale=%d", staleDuration));
            return;
        }

        // -- Rotation: look at best visible waypoint ahead -------------
        if (allowRotation) {
            rotationController.updateRotation(mc, playerPos, path, pathTracker.getPursuitSegment(), message -> {
            });
        }

        // -- Distance to final goal -----------------------------------------
        double finalDx = (goalX + goalCenterX) - playerPos.x;
        double finalDy = goalY         - playerPos.y;
        double finalDz = (goalZ + goalCenterZ) - playerPos.z;
        double distToFinal = Math.sqrt(finalDx * finalDx + finalDy * finalDy + finalDz * finalDz);

        if (!sneakLatched && stickySneakDistance > 0 && distToFinal <= stickySneakDistance) {
            sneakLatched = true;
        }

        Node movementWp = getMovementWaypoint();
        movementController.apply(this, mc, playerPos, movementWp, distToFinal,
                allowJumps && !recoveryJump, sneakLatched, pathTracker.getPursuitSegment(), jumpCooldown, pathTracker.getLastProgressTime());

        if (sneakLatched) {
            mc.options.keyShift.setDown(true);
        }
    }

    public void stop(Minecraft mc) {
        state = State.IDLE;
        RotationExecutor.stopRotating();
        releaseAll(mc);
    }

    public void releaseAll(Minecraft mc) {
        MovementInputController.releaseAll(mc, sneakLatched);
    }

    // --- Internal helpers ----------------------------------------------------

    private Node getMovementWaypoint() {
        return pathTracker.getMovementWaypoint();
    }

    private void triggerReplan(Minecraft mc, boolean force, boolean fromPlayer, String reason) {
        releaseAll(mc);
        lastReplanTime = System.currentTimeMillis();
        pathTracker.markReplanTriggered(lastReplanTime);
        replanFromPlayerRequested = fromPlayer;
        if (PathfinderConfig.SHOW_DEBUG.get()) {
            ClientUtils.sendDebugMessage(mc, "Path replan: " + reason);
        }
        state = State.REPLANNING;
    }

    private void applyGoalCenteringMovement(Minecraft mc, double dx, double dz) {
        if (mc.player == null) {
            return;
        }

        MovementInputController.applyGoalCentering(mc, dx, dz);
    }

    private void finish(Minecraft mc) {
        RotationExecutor.stopRotating();
        releaseAll(mc);
        state = State.FINISHED;
        if (onFinished != null) {
            onFinished.run();
        }
    }

}
