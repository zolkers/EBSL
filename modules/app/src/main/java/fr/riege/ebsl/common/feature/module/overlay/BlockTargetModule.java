package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.domain.world.BlockId;
import imgui.ImGui;

@SuppressWarnings("java:S6548")
public final class BlockTargetModule extends AbstractAnchoredOverlayModule {
    public static final BlockTargetModule INSTANCE = new BlockTargetModule();

    private BlockTargetModule() {
        super(KeyDisplayAnchor.TOP_RIGHT);
    }

    @Override public String id() { return "block_target"; }
    @Override public String displayName() { return "Block Target"; }
    @Override public String description() { return "Shows the resource ID of the block currently looked at."; }

    @Override
    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        if (!isEnabled()) return;
        BlockId target = platform.player().targetedBlock();
        if (target == null) return;
        MoveTypeOverlayModule.renderBox(ImGui.getForegroundDrawList(), viewport, anchor(), target.toString());
    }
}
