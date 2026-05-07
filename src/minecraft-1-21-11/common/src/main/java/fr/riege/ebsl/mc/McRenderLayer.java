package fr.riege.ebsl.mc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.riege.ebsl.common.layer.IRenderLayer;
import fr.riege.ebsl.loader.render.EbslMeshRenderer;
import fr.riege.ebsl.loader.render.EbslRenderPipelines;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class McRenderLayer implements IRenderLayer {
    private final EbslMeshRenderer meshRenderer = new EbslMeshRenderer();
    private final float[] color = {1.0f, 1.0f, 1.0f, 1.0f};

    private Frame frame = Frame.EMPTY;
    private Matrix4f projection = new Matrix4f();
    private BufferBuilder bufferBuilder;
    private RenderPipeline depthPipeline;
    private RenderPipeline noDepthPipeline;

    @Override public void beginFrame(double camX, double camY, double camZ, float[] viewMatrix, float[] projMatrix) {
        frame = new Frame(camX, camY, camZ, copy(viewMatrix), copy(projMatrix));
        projection = fromArray(projMatrix);
    }

    @Override public void endFrame() {
        meshRenderer.endFrame();
    }

    @Override public void beginLines(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
        depthPipeline = EbslRenderPipelines.LINES_WITH_DEPTH;
        noDepthPipeline = EbslRenderPipelines.LINES_NO_DEPTH;
        bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override public void emitLine(double x1, double y1, double z1, double x2, double y2, double z2, float lineWidth) {
        if (bufferBuilder == null) {
            return;
        }

        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        Matrix4f modelViewProjection = new Matrix4f(projection).mul(modelView);

        Vector4f clipA = modelViewProjection.transform(new Vector4f((float) x1, (float) y1, (float) z1, 1.0f));
        Vector4f clipB = modelViewProjection.transform(new Vector4f((float) x2, (float) y2, (float) z2, 1.0f));
        if (Math.abs(clipA.w) < 1.0e-5f || Math.abs(clipB.w) < 1.0e-5f) {
            return;
        }

        var window = Minecraft.getInstance().getWindow();
        float screenWidth = window.getWidth();
        float screenHeight = window.getHeight();
        float dx = (clipB.x / clipB.w - clipA.x / clipA.w) * screenWidth;
        float dy = (clipB.y / clipB.w - clipA.y / clipA.w) * screenHeight;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0e-5f) {
            return;
        }

        float px = (-dy / length) * lineWidth / screenWidth;
        float py = (dx / length) * lineWidth / screenHeight;
        Vector4f worldOffset = new Matrix4f(modelViewProjection).invert().transform(new Vector4f(px, py, 0.0f, 0.0f));

        float ax = (float) x1 + worldOffset.x * clipA.w;
        float ay = (float) y1 + worldOffset.y * clipA.w;
        float az = (float) z1 + worldOffset.z * clipA.w;
        float bx = (float) x1 - worldOffset.x * clipA.w;
        float by = (float) y1 - worldOffset.y * clipA.w;
        float bz = (float) z1 - worldOffset.z * clipA.w;
        float cx = (float) x2 + worldOffset.x * clipB.w;
        float cy = (float) y2 + worldOffset.y * clipB.w;
        float cz = (float) z2 + worldOffset.z * clipB.w;
        float dxw = (float) x2 - worldOffset.x * clipB.w;
        float dyw = (float) y2 - worldOffset.y * clipB.w;
        float dzw = (float) z2 - worldOffset.z * clipB.w;

        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];
        bufferBuilder.addVertex(ax, ay, az).setColor(r, g, b, a);
        bufferBuilder.addVertex(bx, by, bz).setColor(r, g, b, a);
        bufferBuilder.addVertex(cx, cy, cz).setColor(r, g, b, a);
        bufferBuilder.addVertex(bx, by, bz).setColor(r, g, b, a);
        bufferBuilder.addVertex(dxw, dyw, dzw).setColor(r, g, b, a);
        bufferBuilder.addVertex(cx, cy, cz).setColor(r, g, b, a);
    }

    @Override public void end(boolean ignoreDepth) {
        if (bufferBuilder == null) {
            return;
        }
        try (MeshData meshData = bufferBuilder.build()) {
            if (meshData != null) {
                meshRenderer.render(meshData, ignoreDepth ? noDepthPipeline : depthPipeline, projection, RenderSystem.getModelViewMatrix());
            }
        }
        bufferBuilder = null;
    }

    @Override public double cameraX() {
        return frame.cameraX();
    }

    @Override public double cameraY() {
        return frame.cameraY();
    }

    @Override public double cameraZ() {
        return frame.cameraZ();
    }

    public Frame frame() {
        return frame;
    }

    private static float[] copy(float[] values) {
        return values == null ? new float[0] : values.clone();
    }

    private static Matrix4f fromArray(float[] values) {
        Matrix4f matrix = new Matrix4f();
        if (values != null && values.length >= 16) {
            matrix.set(values);
        }
        return matrix;
    }

    public record Frame(double cameraX, double cameraY, double cameraZ, float[] viewMatrix, float[] projectionMatrix) {
        static final Frame EMPTY = new Frame(0.0, 0.0, 0.0, new float[0], new float[0]);
    }
}
