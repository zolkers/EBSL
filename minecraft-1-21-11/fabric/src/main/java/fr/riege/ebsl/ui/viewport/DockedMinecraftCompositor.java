package fr.riege.ebsl.ui.viewport;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.riege.ebsl.render.EbslRenderPipelines;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.layout.UiRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalInt;

public final class DockedMinecraftCompositor {
    private static TextureTarget sourceCopy;
    private static boolean composing;

    private DockedMinecraftCompositor() {
    }

    public static void compose(RenderTarget target) {
        if (composing || target.getColorTexture() == null || target.getColorTextureView() == null) {
            return;
        }

        composing = true;
        try {
            ensureSourceCopy(target.width, target.height);
            copyFrame(target, sourceCopy);
            clearTarget(target);
            drawCopyIntoViewport(target, sourceCopy);
        } finally {
            composing = false;
        }
    }

    private static void ensureSourceCopy(int width, int height) {
        if (sourceCopy == null) {
            sourceCopy = new TextureTarget("ebsl:docked_minecraft_source", width, height, false);
            return;
        }
        if (sourceCopy.width != width || sourceCopy.height != height) {
            sourceCopy.resize(width, height);
        }
    }

    private static void copyFrame(RenderTarget source, RenderTarget destination) {
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
            source.getColorTexture(),
            destination.getColorTexture(),
            0, 0, 0, 0, 0,
            source.width,
            source.height);
    }

    private static void clearTarget(RenderTarget target) {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), 0xFF080B10);
    }

    private static void drawCopyIntoViewport(RenderTarget target, RenderTarget source) {
        Window window = Minecraft.getInstance().getWindow();
        UiRect viewport = minecraftGuiRectForImGuiViewport(window);
        BlitRenderState blit = new BlitRenderState(
            EbslRenderPipelines.DOCKED_VIEWPORT,
            TextureSetup.singleTexture(
                source.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)),
            new Matrix3x2f(),
            viewport.x(), viewport.y(), viewport.right(), viewport.bottom(),
            0.0f, 1.0f, 1.0f, 0.0f,
            0xFFFFFFFF,
            new ScreenRectangle(viewport.x(), viewport.y(), viewport.width(), viewport.height()));

        BufferBuilder builder = Tesselator.getInstance().begin(
            EbslRenderPipelines.DOCKED_VIEWPORT.getVertexFormatMode(),
            EbslRenderPipelines.DOCKED_VIEWPORT.getVertexFormat());
        blit.buildVertices(builder);

        try (MeshData mesh = builder.build()) {
            if (mesh == null) {
                return;
            }
            drawMesh(target, source, mesh);
        }
    }

    private static UiRect minecraftGuiRectForImGuiViewport(Window window) {
        UiRect physical = EbslImGuiOverlay.gameViewportRect(window.getWidth(), window.getHeight());
        float scaleX = (float) window.getWidth() / (float) window.getGuiScaledWidth();
        float scaleY = (float) window.getHeight() / (float) window.getGuiScaledHeight();
        int x = Math.round(physical.x() / scaleX);
        int y = Math.round(physical.y() / scaleY);
        int right = Math.round(physical.right() / scaleX);
        int bottom = Math.round(physical.bottom() / scaleY);
        return new UiRect(x, y, Math.max(1, right - x), Math.max(1, bottom - y));
    }

    private static void drawMesh(RenderTarget target, RenderTarget source, MeshData mesh) {
        MeshData.DrawState drawState = mesh.drawState();
        VertexFormat format = drawState.format();
        GpuBuffer vertexBuffer = format.uploadImmediateVertexBuffer(mesh.vertexBuffer());

        GpuBuffer indexBuffer;
        VertexFormat.IndexType indexType;
        if (mesh.indexBuffer() != null) {
            indexBuffer = format.uploadImmediateIndexBuffer(mesh.indexBuffer());
            indexType = drawState.indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(drawState.mode());
            indexBuffer = sequential.getBuffer(drawState.indexCount());
            indexType = sequential.type();
        }

        GpuBufferSlice transform = RenderSystem.getDynamicUniforms().writeTransform(
            new Matrix4f().setTranslation(0.0f, 0.0f, -11000.0f),
            new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            new Vector3f(),
            new Matrix4f());

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            () -> "ebsl:docked_minecraft",
            target.getColorTextureView(),
            OptionalInt.empty())) {
            pass.setPipeline(EbslRenderPipelines.DOCKED_VIEWPORT);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transform);
            pass.bindTexture("Sampler0", source.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.drawIndexed(0, 0, drawState.indexCount(), 1);
        }
    }
}
