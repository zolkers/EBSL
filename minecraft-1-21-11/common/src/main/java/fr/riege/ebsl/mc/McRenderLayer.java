package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IRenderLayer;
import fr.riege.ebsl.common.math.Vec3d;

public class McRenderLayer implements IRenderLayer {
    @Override public void drawLine(Vec3d from, Vec3d to, int colorARGB, float width) { throw new UnsupportedOperationException("TODO"); }
    @Override public void drawBox(Vec3d min, Vec3d max, int colorARGB, float width) { throw new UnsupportedOperationException("TODO"); }
    @Override public void drawFilledBox(Vec3d min, Vec3d max, int colorARGB) { throw new UnsupportedOperationException("TODO"); }
    @Override public void drawSphere(Vec3d center, float radius, int colorARGB, int segments) { throw new UnsupportedOperationException("TODO"); }
    @Override public void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix) { throw new UnsupportedOperationException("TODO"); }
    @Override public void endFrame() { throw new UnsupportedOperationException("TODO"); }
}
