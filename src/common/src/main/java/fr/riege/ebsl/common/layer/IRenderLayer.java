package fr.riege.ebsl.common.layer;

public interface IRenderLayer {
    void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix);
    void endFrame();
}
