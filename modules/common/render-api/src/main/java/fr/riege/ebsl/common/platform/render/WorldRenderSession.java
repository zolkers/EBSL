package fr.riege.ebsl.common.platform.render;

@SuppressWarnings("java:S107")
public final class WorldRenderSession implements AutoCloseable {
    private final RenderHandle handle;
    private RenderPaint paint = RenderPaint.SOLID_WHITE;
    private boolean ignoreDepth;
    private float lineWidth = 1.0f;
    private boolean opened;
    private double gradientMinY;
    private double gradientRangeY = 1.0;

    WorldRenderSession(RenderHandle handle) {
        if (handle == null) {
            throw new NullPointerException("render handle required");
        }
        this.handle = handle;
    }

    public WorldRenderSession paint(RenderPaint paint) {
        this.paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
        return this;
    }

    public WorldRenderSession color(RenderColor color) {
        return paint(RenderPaint.solid(color));
    }

    public WorldRenderSession argb(int argb) {
        return color(RenderColor.argb(argb));
    }

    public WorldRenderSession gradient(RenderColor from, RenderColor to) {
        return paint(RenderPaint.gradient(from, to));
    }

    public WorldRenderSession rainbow() {
        return paint(RenderPaint.rainbow());
    }

    public WorldRenderSession rainbow(float alpha) {
        return paint(RenderPaint.rainbow(alpha));
    }

    public WorldRenderSession lineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.1f, lineWidth);
        return this;
    }

    public WorldRenderSession ignoreDepth(boolean ignoreDepth) {
        this.ignoreDepth = ignoreDepth;
        return this;
    }

    public WorldRenderSession throughWalls() {
        return ignoreDepth(true);
    }

    public WorldRenderSession depthTested() {
        return ignoreDepth(false);
    }

    public WorldRenderSession line(double x1, double y1, double z1,
                                   double x2, double y2, double z2) {
        beginLines();
        RenderColor from = paint.colorAt(0.0f);
        RenderColor to = paint.colorAt(1.0f);
        handle.emitLine(localX(x1), localY(y1), localZ(z1), localX(x2), localY(y2), localZ(z2), lineWidth, from, to);
        return this;
    }

    public WorldRenderSession filledBlock(int x, int y, int z) {
        return filledBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderSession wireBlock(int x, int y, int z) {
        return wireBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderSession filledBox(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ) {
        beginTriangles();
        double x1 = localX(Math.min(minX, maxX));
        double y1 = localY(Math.min(minY, maxY));
        double z1 = localZ(Math.min(minZ, maxZ));
        double x2 = localX(Math.max(minX, maxX));
        double y2 = localY(Math.max(minY, maxY));
        double z2 = localZ(Math.max(minZ, maxZ));
        gradientMinY = y1;
        gradientRangeY = Math.max(1.0e-5, y2 - y1);
        quad(x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
        quad(x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        quad(x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        quad(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
        quad(x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2);
        quad(x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1);
        return this;
    }

    public WorldRenderSession wireBox(double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
        beginLines();
        double x1 = Math.min(minX, maxX);
        double y1 = Math.min(minY, maxY);
        double z1 = Math.min(minZ, maxZ);
        double x2 = Math.max(minX, maxX);
        double y2 = Math.max(minY, maxY);
        double z2 = Math.max(minZ, maxZ);
        gradientMinY = y1;
        gradientRangeY = Math.max(1.0e-5, y2 - y1);
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
        return this;
    }

    @Override
    public void close() {
        if (!opened) {
            return;
        }
        handle.end(ignoreDepth);
        opened = false;
    }

    private void beginLines() {
        if (opened) {
            return;
        }
        RenderColor base = paint.colorAt(0.0f);
        handle.beginLines(base.r(), base.g(), base.b(), base.a());
        opened = true;
    }

    private void beginTriangles() {
        if (opened) {
            return;
        }
        RenderColor base = paint.colorAt(0.0f);
        handle.beginTriangles(base.r(), base.g(), base.b(), base.a());
        opened = true;
    }

    private void quad(double ax, double ay, double az,
                      double bx, double by, double bz,
                      double cx, double cy, double cz,
                      double dx, double dy, double dz) {
        RenderColor ca = paint.colorAt(vertexProgress(ay));
        RenderColor cb = paint.colorAt(vertexProgress(by));
        RenderColor cc = paint.colorAt(vertexProgress(cy));
        RenderColor cd = paint.colorAt(vertexProgress(dy));
        handle.emitTriangle(ax, ay, az, bx, by, bz, cx, cy, cz, ca, cb, cc);
        handle.emitTriangle(ax, ay, az, cx, cy, cz, dx, dy, dz, ca, cc, cd);
    }

    private void emitRawLine(double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        RenderColor from = paint.colorAt(vertexProgress(y1));
        RenderColor to = paint.colorAt(vertexProgress(y2));
        handle.emitLine(localX(x1), localY(y1), localZ(z1), localX(x2), localY(y2), localZ(z2), lineWidth, from, to);
    }

    private float vertexProgress(double y) {
        return Math.clamp((float) ((y - gradientMinY) / gradientRangeY), 0.0f, 1.0f);
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
