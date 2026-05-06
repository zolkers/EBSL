package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.render.RenderHandle;

public interface IRenderLayer extends RenderHandle {
    void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix);
    void endFrame();
}
