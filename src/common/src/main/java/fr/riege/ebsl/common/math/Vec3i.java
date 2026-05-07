package fr.riege.ebsl.common.math;

public record Vec3i(int x, int y, int z) {
    public Vec3d center() { return new Vec3d(x + 0.5, y + 0.5, z + 0.5); }
    public Vec3i offset(int dx, int dy, int dz) { return new Vec3i(x + dx, y + dy, z + dz); }
    public double distanceTo(Vec3i other) {
        double dx = x - (double) other.x;
        double dy = y - (double) other.y;
        double dz = z - (double) other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
