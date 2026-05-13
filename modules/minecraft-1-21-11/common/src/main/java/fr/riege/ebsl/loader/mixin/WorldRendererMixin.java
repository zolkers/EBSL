package fr.riege.ebsl.loader.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("java:S107")
@Mixin(LevelRenderer.class)
public final class WorldRendererMixin {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void ebslOnRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator,
                                    DeltaTracker deltaTracker,
                                    boolean renderBlockOutline,
                                    Camera camera,
                                    Matrix4f modelView,
                                    Matrix4f projection,
                                    Matrix4f frustumMatrix,
                                    GpuBufferSlice gpuBufferSlice,
                                    Vector4f vector4f,
                                    boolean isTicking,
                                    CallbackInfo ci) {
        RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
        Vec3 position = camera.position();
        ModloaderCommonBootstrap.onRenderWorld(new Matrix4f(projection), position.x, position.y, position.z);
        RenderSystem.getModelViewStack().popMatrix();
    }
}
