package fr.riege.ebsl.common.platform.render;

public final class WorldRenderBuilder {
    private final RenderHandle handle;
    private RenderColor color = RenderColor.WHITE;
    private boolean ignoreDepth;
    private float lineWidth = 1.0f;

    WorldRenderBuilder(RenderHandle handle) {
        if (handle == null) {
            throw new NullPointerException("render handle required");
        }
        this.handle = handle;
    }

    public WorldRenderBuilder color(RenderColor color) {
        this.color = color != null ? color : RenderColor.WHITE;
        return this;
    }

    public WorldRenderBuilder argb(int argb) {
        return color(RenderColor.argb(argb));
    }

    public WorldRenderBuilder lineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.1f, lineWidth);
        return this;
    }

    public WorldRenderBuilder ignoreDepth(boolean ignoreDepth) {
        this.ignoreDepth = ignoreDepth;
        return this;
    }

    public WorldRenderBuilder throughWalls() {
        return ignoreDepth(true);
    }

    public WorldRenderBuilder depthTested() {
        return ignoreDepth(false);
    }

    public WorldRenderBuilder line(double x1, double y1, double z1,
                                   double x2, double y2, double z2) {
        handle.beginLines(color.r(), color.g(), color.b(), color.a());
        handle.emitLine(localX(x1), localY(y1), localZ(z1), localX(x2), localY(y2), localZ(z2), lineWidth);
        handle.end(ignoreDepth);
        return this;
    }

    public WorldRenderBuilder filledBlock(int x, int y, int z) {
        return filledBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderBuilder wireBlock(int x, int y, int z) {
        return wireBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderBuilder filledBox(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ) {
        double x1 = localX(Math.min(minX, maxX));
        double y1 = localY(Math.min(minY, maxY));
        double z1 = localZ(Math.min(minZ, maxZ));
        double x2 = localX(Math.max(minX, maxX));
        double y2 = localY(Math.max(minY, maxY));
        double z2 = localZ(Math.max(minZ, maxZ));

        handle.beginTriangles(color.r(), color.g(), color.b(), color.a());
        quad(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
        quad(x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        quad(x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        quad(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
        quad(x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2);
        quad(x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1);
        handle.end(ignoreDepth);
        return this;
    }

    public WorldRenderBuilder wireBox(double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
        double x1 = Math.min(minX, maxX);
        double y1 = Math.min(minY, maxY);
        double z1 = Math.min(minZ, maxZ);
        double x2 = Math.max(minX, maxX);
        double y2 = Math.max(minY, maxY);
        double z2 = Math.max(minZ, maxZ);

        handle.beginLines(color.r(), color.g(), color.b(), color.a());
        emitRawLine(x1, y1, z1, x2, y1, z1);
        emitRawLine(x2, y1, z1, x2, y1, z2);
        emitRawLine(x2, y1, z2, x1, y1, z2);
        emitRawLine(x1, y1, z2, x1, y1, z1);
        emitRawLine(x1, y2, z1, x2, y2, z1);
        emitRawLine(x2, y2, z1, x2, y2, z2);
        emitRawLine(x2, y2, z2, x1, y2, z2);
        emitRawLine(x1, y2, z2, x1, y2, z1);
        emitRawLine(x1, y1, z1, x1, y2, z1);
        emitRawLine(x2, y1, z1, x2, y2, z1);
        emitRawLine(x2, y1, z2, x2, y2, z2);
        emitRawLine(x1, y1, z2, x1, y2, z2);
        handle.end(ignoreDepth);
        return this;
    }

    private void quad(double ax, double ay, double az,
                      double bx, double by, double bz,
                      double cx, double cy, double cz,
                      double dx, double dy, double dz) {
        handle.emitTriangle(ax, ay, az, bx, by, bz, cx, cy, cz);
        handle.emitTriangle(ax, ay, az, cx, cy, cz, dx, dy, dz);
    }

    private void emitRawLine(double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        handle.emitLine(localX(x1), localY(y1), localZ(z1), localX(x2), localY(y2), localZ(z2), lineWidth);
    }

    private double localX(double x) {
        return x - handle.cameraX();
    }

    private double localY(double y) {
        return y - handle.cameraY();
    }

    private double localZ(double z) {
        return z - handle.cameraZ();
    }
}
