package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.math.Vec3d;

public final class AngleUtils {
    private AngleUtils() {
    }

    public static float normalizeAngle(float angle) {
        float a = angle % 360f;
        if (a >= 180f) a -= 360f;
        if (a < -180f) a += 360f;
        return a;
    }

    public static float getRotationDelta(float from, float to) {
        float normalizedTo = normalizeAngle(to);
        float normalizedFrom = normalizeAngle(from);
        float delta = normalizedTo - normalizedFrom;
        if (delta > 180f) delta -= 360f;
        if (delta < -180f) delta += 360f;
        return delta;
    }

    public static Rotation getRotation(Vec3d from, Vec3d to) {
        double xDiff = to.x() - from.x();
        double yDiff = to.y() - from.y();
        double zDiff = to.z() - from.z();
        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(yDiff, dist));
        return new Rotation(yaw, pitch);
    }
}
