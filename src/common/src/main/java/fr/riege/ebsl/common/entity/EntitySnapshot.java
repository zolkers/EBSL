package fr.riege.ebsl.common.entity;

import fr.riege.ebsl.common.math.Vec3d;

public record EntitySnapshot(
    int id,
    String typeId,
    String displayName,
    String name,
    Vec3d position,
    Vec3d eyePosition,
    double minX,
    double minY,
    double minZ,
    double maxX,
    double maxY,
    double maxZ,
    boolean living,
    boolean mob,
    boolean alive,
    boolean removed,
    float health
) {
    public double distanceToSq(Vec3d other) {
        double dx = position.x() - other.x();
        double dy = position.y() - other.y();
        double dz = position.z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public double bbHeight() {
        return Math.max(0.0, maxY - minY);
    }
}
