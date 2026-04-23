package fr.riege.ebsl.render;

import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public final class TemplateMeshUniforms implements DynamicUniformStorage.DynamicUniform {
    static final int BLOCK_SIZE = 256;

    final Matrix4f projection = new Matrix4f();
    final Matrix4f modelView = new Matrix4f();
    float screenWidth = 1.0f;
    float screenHeight = 1.0f;

    @Override
    public void write(@NonNull ByteBuffer buffer) {
        projection.get(0, buffer);
        modelView.get(64, buffer);
        buffer.putFloat(128, screenWidth);
        buffer.putFloat(132, screenHeight);
    }
}
