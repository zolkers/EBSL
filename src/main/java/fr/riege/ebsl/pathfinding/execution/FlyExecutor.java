package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.NavigationMode;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.annotation.NavigationModeHandler;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;


@PathingStage(PathingStage.Stage.EXECUTION)
@NavigationModeHandler(NavigationMode.FLY)
public final class FlyExecutor {

    public enum State {
        IDLE, FLYING, DECELERATING, FINISHED
    }

    private State state = State.IDLE;
    private List<Node> path;
    private int goalX, goalY, goalZ;
    private long decelStartTime = 0;

    private Runnable onFinished;
    private boolean usePitchControl = false;
    private float targetPitch = 0;
    private boolean useLookTargetRotation = false;
    private Vec3 lookTarget = null;
    private final FlyRotationController rotationController = new FlyRotationController();
    private final FlyVerticalController verticalController = new FlyVerticalController();
    private final FlyProgressTracker progressTracker = new FlyProgressTracker();

    public void start(List<Node> path, int goalX, int goalY, int goalZ) {
        start(path, goalX, goalY, goalZ, null);
    }

    public void start(List<Node> path, int goalX, int goalY, int goalZ, Runnable onFinished) {
        this.path = new ArrayList<>(path);
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
        this.decelStartTime = 0;
        this.onFinished = onFinished;
        this.usePitchControl = false;
        this.useLookTargetRotation = false;
        this.lookTarget = null;
        this.rotationController.reset();
        this.progressTracker.reset();
        state = State.FLYING;
    }

    public State getState() {
        return state;
    }

    public void tick(Minecraft mc) {
        if (mc.player == null)
            return;

        if (ClientUtils.isInventoryScreenOpen(mc)) {
            releaseAll(mc);
            return;
        }

        if (state == State.DECELERATING) {
            tickDecelerate(mc);
            return;
        }

        if (state != State.FLYING)
            return;
        if (path == null || path.isEmpty()) {
            finish(mc);
            return;
        }

        Vec3 pos = mc.player.position();

        int wpIndex = progressTracker.advanceWaypoints(path, pos, message -> debugRotation("%s", message));

        Vec3 goal = new Vec3(goalX + 0.5, goalY + 0.15, goalZ + 0.5);
        double distToGoal = pos.distanceTo(goal);

        if (progressTracker.shouldStopNow(mc, goal)) {
            debugRotation("begin decelerate reason=shouldStopNow distToGoal=%.2f wp=%d", distToGoal, wpIndex);
            beginDecelerate(mc);
            return;
        }

        if (wpIndex >= path.size()) {
            debugRotation("begin decelerate reason=path_end wp=%d", wpIndex);
            beginDecelerate(mc);
            return;
        }

        Node wp = path.get(wpIndex);
        double dx = (wp.position.flooredX() + 0.5) - pos.x;
        double dz = (wp.position.flooredZ() + 0.5) - pos.z;
        double dyWp = wp.position.flooredY() + 0.15;

        if (wpIndex >= path.size() - 2 && progressTracker.shouldStopNow(mc, goal)) {
            debugRotation("begin decelerate reason=near_end_shouldStop wp=%d distToGoal=%.2f", wpIndex, distToGoal);
            beginDecelerate(mc);
            return;
        }

        if (useLookTargetRotation && lookTarget != null) {
            debugRotation("tick rotate lookTarget=(%.2f,%.2f,%.2f) pos=(%.2f,%.2f,%.2f)",
                    lookTarget.x, lookTarget.y, lookTarget.z, pos.x, pos.y, pos.z);
            rotationController.rotateTowardLookTarget(mc, lookTarget, message -> debugRotation("%s", message));
        } else if (distToGoal > 3.0) {
            debugRotation("tick rotate waypoint wp=%d dx=%.2f dz=%.2f distToGoal=%.2f",
                    wpIndex, dx, dz, distToGoal);
            rotationController.rotateWaypointYaw(mc, dx, dz, message -> debugRotation("%s", message));
        } else {
            debugRotation("tick rotate skipped lock-range distToGoal=%.2f wp=%d",
                    distToGoal, wpIndex);
        }
        if (usePitchControl) {
            float prevPitch = mc.player.getXRot();
            mc.player.setXRot(targetPitch);
            mc.player.xRotO = prevPitch;
            debugRotation("tick pitch control prev=%.2f target=%.2f", prevPitch, targetPitch);
        }

        applyStrafingMovement(mc, dx, dz);
        mc.options.keySprint.setDown(distToGoal > 5.0);
        verticalController.adjustVerticalKeysWithRaycast(mc, pos, dyWp);

        progressTracker.trackMovement(pos);
        if (progressTracker.shouldAbortForStuck()) {
            if (mc.player != null) {
                fr.riege.ebsl.util.ClientUtils.sendMessage(mc, "§cFly stuck! Aborting navigation.", false);
            }
            stop(mc);
            return;
        }
        if (progressTracker.shouldClimbForRecovery()) {
            mc.options.keyJump.setDown(true);
            mc.options.keyShift.setDown(false);
        }

        if (PathfinderSettings.instance().showDebug.value()) {
            ClientUtils.sendDebugMessage(mc, String.format(
                    "fly wp=%d/%d dist=%.2f state=%s",
                    Math.min(wpIndex + 1, path.size()), path.size(), distToGoal, state));
        }
    }

    public void stop(Minecraft mc) {
        state = State.IDLE;
        RotationExecutor.stopRotating();
        releaseAll(mc);
    }

    public void releaseAll(Minecraft mc) {
        InputApplier.releaseAll(mc, false);
    }

    private void beginDecelerate(Minecraft mc) {
        state = State.DECELERATING;
        decelStartTime = System.currentTimeMillis();
        releaseAll(mc);
    }

    private void tickDecelerate(Minecraft mc) {
        if (mc.player == null) {
            finish(mc);
            return;
        }
        Vec3 vel = mc.player.getDeltaMovement();
        boolean stopped = Math.abs(vel.x) < 0.05 && Math.abs(vel.z) < 0.05 && Math.abs(vel.y) < 0.05;
        if (stopped || System.currentTimeMillis() - decelStartTime > 2000) {
            finish(mc);
        }
    }

    private void finish(Minecraft mc) {
        RotationExecutor.stopRotating();
        releaseAll(mc);
        state = State.FINISHED;
        if (onFinished != null) {
            onFinished.run();
        }
    }

    private void applyStrafingMovement(Minecraft mc, double dx, double dz) {
        InputApplier.applyRelativeMovement(mc, dx, dz, 0.1, -0.1, 0.1);
    }

    private void debugRotation(String message, Object... args) {
    }
}

