package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;

public interface IRenderLayer {
    void drawLine(Vec3d from, Vec3d to, int colorARGB, float width);
    void drawBox(Vec3d min, Vec3d max, int colorARGB, float width);
    void drawFilledBox(Vec3d min, Vec3d max, int colorARGB);
    void drawSphere(Vec3d center, float radius, int colorARGB, int segments);
    void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix);
    void endFrame();
}
