package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

public final class WaterMovementContext {
    private final Node waypoint;
    private final Node nextWaypoint;
    private final Vec3d playerPos;
    private final double distToFinal;
    private final boolean playerInWater;
    private final boolean headUnderWater;
    private boolean handled;
    private boolean jumpPressed;
    private boolean shiftPressed;
    private boolean sprintPressed;

    public WaterMovementContext(Node waypoint, Node nextWaypoint, Vec3d playerPos,
                                double distToFinal, boolean playerInWater, boolean headUnderWater) {
        this.waypoint = waypoint;
        this.nextWaypoint = nextWaypoint;
        this.playerPos = playerPos;
        this.distToFinal = distToFinal;
        this.playerInWater = playerInWater;
        this.headUnderWater = headUnderWater;
    }

    public Node waypoint() { return waypoint; }
    public Node nextWaypoint() { return nextWaypoint; }
    public Vec3d playerPos() { return playerPos; }
    public double distToFinal() { return distToFinal; }
    public boolean playerInWater() { return playerInWater; }
    public boolean headUnderWater() { return headUnderWater; }

    public void setVerticalControls(boolean jumpPressed, boolean shiftPressed) {
        this.handled = true;
        this.jumpPressed = jumpPressed;
        this.shiftPressed = shiftPressed;
    }

    public void setSprintPressed(boolean sprintPressed) {
        this.sprintPressed = sprintPressed;
    }

    public boolean handled() { return handled; }
    public boolean jumpPressed() { return jumpPressed; }
    public boolean shiftPressed() { return shiftPressed; }
    public boolean sprintPressed() { return sprintPressed; }
}
