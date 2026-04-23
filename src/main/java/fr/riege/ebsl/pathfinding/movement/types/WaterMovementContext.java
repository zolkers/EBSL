package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class WaterMovementContext {
    private final Minecraft minecraft;
    private final Node waypoint;
    private final Node nextWaypoint;
    private final Vec3 playerPos;
    private final double distToFinal;
    private boolean handled;
    private boolean jumpPressed;
    private boolean shiftPressed;
    private boolean sprintPressed;

    public WaterMovementContext(Minecraft minecraft, Node waypoint, Node nextWaypoint,
                                Vec3 playerPos, double distToFinal) {
        this.minecraft = minecraft;
        this.waypoint = waypoint;
        this.nextWaypoint = nextWaypoint;
        this.playerPos = playerPos;
        this.distToFinal = distToFinal;
    }

    public Minecraft minecraft() {
        return minecraft;
    }

    public Node waypoint() {
        return waypoint;
    }

    public Node nextWaypoint() {
        return nextWaypoint;
    }

    public Vec3 playerPos() {
        return playerPos;
    }

    public double distToFinal() {
        return distToFinal;
    }

    public void setVerticalControls(boolean jumpPressed, boolean shiftPressed) {
        this.handled = true;
        this.jumpPressed = jumpPressed;
        this.shiftPressed = shiftPressed;
    }

    public void setSprintPressed(boolean sprintPressed) {
        this.sprintPressed = sprintPressed;
    }

    public boolean handled() {
        return handled;
    }

    public boolean jumpPressed() {
        return jumpPressed;
    }

    public boolean shiftPressed() {
        return shiftPressed;
    }

    public boolean sprintPressed() {
        return sprintPressed;
    }
}
