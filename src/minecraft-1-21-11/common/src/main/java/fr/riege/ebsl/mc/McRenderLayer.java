package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IRenderLayer;

public class McRenderLayer implements IRenderLayer {
    private Frame frame = Frame.EMPTY;

    @Override public void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix) {
        frame = new Frame(camX, camY, camZ, copy(viewMatrix), copy(projMatrix));
    }

    @Override public void endFrame() {
    }

    public Frame frame() {
        return frame;
    }

    private static float[] copy(float[] values) {
        return values == null ? new float[0] : values.clone();
    }

    public record Frame(double cameraX, double cameraY, double cameraZ, float[] viewMatrix, float[] projectionMatrix) {
        static final Frame EMPTY = new Frame(0.0, 0.0, 0.0, new float[0], new float[0]);
    }
}
