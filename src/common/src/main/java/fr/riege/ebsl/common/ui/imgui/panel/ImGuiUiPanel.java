package fr.riege.ebsl.common.ui.imgui.panel;

import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;

public interface ImGuiUiPanel {
    void render(EbslUiState state, ViewportLayout layout, NavigationService navigation);
}
