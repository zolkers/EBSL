package fr.riege.ebsl.common.pathfinding.rotation;

public final class Rotation {
    public final float yaw;
    public final float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "Rotation{yaw=" + yaw + ", pitch=" + pitch + "}";
    }
}
