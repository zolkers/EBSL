package fr.riege.ebsl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.riege.ebsl.EbslMod;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class EbslRenderPipelines {
    private static final Identifier POSITION_COLOR_SHADER =
        Identifier.fromNamespaceAndPath(EbslMod.MOD_ID, "core/pos_color");
    private static final Identifier POSITION_TEX_SHADER =
        Identifier.fromNamespaceAndPath("minecraft", "core/position_tex");
    private static final Identifier DOCKED_VIEWPORT_SHADER =
        Identifier.fromNamespaceAndPath(EbslMod.MOD_ID, "core/docked_viewport");

    static final RenderPipeline LINES_WITH_DEPTH = createLines(DepthTestFunction.LEQUAL_DEPTH_TEST, "lines_depth");
    static final RenderPipeline LINES_NO_DEPTH = createLines(DepthTestFunction.NO_DEPTH_TEST, "lines_no_depth");
    public static final RenderPipeline DOCKED_VIEWPORT = RenderPipelines.register(RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath(EbslMod.MOD_ID, "pipelines/docked_viewport"))
        .withVertexShader(POSITION_TEX_SHADER)
        .withFragmentShader(DOCKED_VIEWPORT_SHADER)
        .withSampler("Sampler0")
        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
        .withoutBlend()
        .withCull(false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .build());

    private EbslRenderPipelines() {
    }

    private static RenderPipeline createLines(DepthTestFunction depthTest, String name) {
        return RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(EbslMod.MOD_ID, "pipelines/" + name))
            .withVertexShader(POSITION_COLOR_SHADER)
            .withFragmentShader(POSITION_COLOR_SHADER)
            .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(depthTest)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
    }
}
