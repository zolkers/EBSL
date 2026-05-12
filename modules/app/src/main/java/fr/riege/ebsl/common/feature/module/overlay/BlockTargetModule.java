package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.module.PathfinderModuleCategory;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.Settingable;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.domain.world.BlockId;
import imgui.ImGui;

public final class BlockTargetModule extends Settingable implements PathfinderModule {
    public static final BlockTargetModule INSTANCE = new BlockTargetModule();

    private final BooleanSetting enabledSetting = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final EnumSetting<KeyDisplayAnchor> anchorSetting = registerSetting(
        new EnumSetting<>("anchor", "Position", KeyDisplayAnchor.TOP_RIGHT, KeyDisplayAnchor.class));

    private BlockTargetModule() {
    }

    @Override public String id() { return "block_target"; }
    @Override public String displayName() { return "Block Target"; }
    @Override public String description() { return "Shows the resource ID of the block currently looked at."; }
    @Override public PathfinderModuleCategory category() { return PathfinderModuleCategory.RENDER; }
    @Override public boolean isEnabled() { return enabledSetting.value(); }
    @Override public void setEnabled(boolean enabled) { enabledSetting.setValue(enabled); }

    @Override
    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        if (!isEnabled()) return;
        BlockId target = platform.player().targetedBlock();
        if (target == null) return;
        MoveTypeOverlayModule.renderBox(ImGui.getForegroundDrawList(), viewport, anchorSetting.value(), target.toString());
    }
}
