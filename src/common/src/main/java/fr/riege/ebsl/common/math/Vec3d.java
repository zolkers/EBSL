package fr.riege.ebsl.common.math;

public record Vec3d(double x, double y, double z) {
    public Vec3d add(double dx, double dy, double dz) {
        return new Vec3d(x + dx, y + dy, z + dz);
    }
    public double distanceTo(Vec3d other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public double distanceToSq(Vec3d other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
    public Vec3i toBlockPos() { return new Vec3i((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)); }
}
