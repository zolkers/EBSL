package fr.riege.ebsl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.riege.ebsl.TemplateMod;
import net.minecraft.resources.Identifier;

public final class TemplateRenderPipelines {
    private static final Identifier POSITION_COLOR_SHADER =
        Identifier.fromNamespaceAndPath(TemplateMod.MOD_ID, "core/pos_color");

    static final RenderPipeline LINES_WITH_DEPTH = createLines(DepthTestFunction.LEQUAL_DEPTH_TEST, "lines_depth");
    static final RenderPipeline LINES_NO_DEPTH = createLines(DepthTestFunction.NO_DEPTH_TEST, "lines_no_depth");

    private TemplateRenderPipelines() {
    }

    private static RenderPipeline createLines(DepthTestFunction depthTest, String name) {
        return RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(TemplateMod.MOD_ID, "pipelines/" + name))
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
