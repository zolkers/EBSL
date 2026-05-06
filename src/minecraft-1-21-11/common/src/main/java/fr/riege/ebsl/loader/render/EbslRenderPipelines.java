package fr.riege.ebsl.loader.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class EbslRenderPipelines {
    private static final String MOD_ID = "ebsl";
    private static final Identifier POSITION_TEX_SHADER =
        Identifier.fromNamespaceAndPath("minecraft", "core/position_tex");
    private static final Identifier DOCKED_VIEWPORT_SHADER =
        Identifier.fromNamespaceAndPath(MOD_ID, "core/docked_viewport");

    public static final RenderPipeline DOCKED_VIEWPORT = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipelines/docked_viewport"))
        .withVertexShader(POSITION_TEX_SHADER)
        .withFragmentShader(DOCKED_VIEWPORT_SHADER)
        .withSampler("Sampler0")
        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
        .withoutBlend()
        .withCull(false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .build();

    private EbslRenderPipelines() {
    }
}
