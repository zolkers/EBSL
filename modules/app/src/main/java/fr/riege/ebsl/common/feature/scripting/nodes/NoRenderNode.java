package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.platform.render.RenderingSystem;

@EbslNodeDefinition(value = EbslNodeType.NO_RENDER, aliases = {"disable_render", "render_off"})
public final class NoRenderNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new BooleanSetting("disable", "Disable Rendering", true));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        RenderingSystem.setEnabled(!disable(invocation.has(0) ? invocation.arg(0) : ""));
        return 0;
    }

    private static boolean disable(String token) {
        return NoRenderDirective.disablesRendering(token);
    }
}
