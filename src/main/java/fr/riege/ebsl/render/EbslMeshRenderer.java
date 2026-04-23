package fr.riege.ebsl.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class EbslMeshRenderer implements AutoCloseable {
    private DynamicUniformStorage<EbslMeshUniforms> uniformStorage;
    private final EbslMeshUniforms uniforms = new EbslMeshUniforms();

    private DynamicUniformStorage<EbslMeshUniforms> uniformStorage() {
        if (uniformStorage == null) {
            uniformStorage = new DynamicUniformStorage<>("ebsl:mesh_data", EbslMeshUniforms.BLOCK_SIZE, 256);
        }
        return uniformStorage;
    }

    public void endFrame() {
        if (uniformStorage != null) {
            uniformStorage.endFrame();
        }
    }

    public void render(MeshData meshData, RenderPipeline pipeline, Matrix4f projection, Matrix4f modelView) {
        MeshData.DrawState drawState = meshData.drawState();
        VertexFormat format = drawState.format();
        int indexCount = drawState.indexCount();

        uniforms.projection.set(projection);
        uniforms.modelView.set(modelView);
        Window window = Minecraft.getInstance().getWindow();
        uniforms.screenWidth = window.getWidth();
        uniforms.screenHeight = window.getHeight();

        GpuBufferSlice uniformSlice = uniformStorage().writeUniform(uniforms);
        GpuBuffer vertexBuffer = format.uploadImmediateVertexBuffer(meshData.vertexBuffer());

        GpuBuffer indexBuffer;
        VertexFormat.IndexType indexType;
        if (meshData.indexBuffer() != null) {
            indexBuffer = format.uploadImmediateIndexBuffer(Objects.requireNonNull(meshData.indexBuffer()));
            indexType = drawState.indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(drawState.mode());
            indexBuffer = sequentialBuffer.getBuffer(indexCount);
            indexType = sequentialBuffer.type();
        }

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (target.getColorTextureView() == null) {
            return;
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (var pass = encoder.createRenderPass(
            () -> "ebsl",
            target.getColorTextureView(), OptionalInt.empty(),
            target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            pass.setUniform("MeshData", uniformSlice);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.drawIndexed(0, 0, indexCount, 1);
        }
    }

    @Override
    public void close() {
        if (uniformStorage != null) {
            uniformStorage.close();
        }
    }
}
