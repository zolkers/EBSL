package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;

public interface ImGuiUiPanel {
    void render(EbslUiState state, ViewportLayout layout);
}
