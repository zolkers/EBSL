package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;

public interface ImGuiUiPanel {
    void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform);
}
