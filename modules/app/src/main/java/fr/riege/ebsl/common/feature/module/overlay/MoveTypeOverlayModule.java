package fr.riege.ebsl.common.feature.module.overlay;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import imgui.ImDrawList;
import imgui.ImGui;

@SuppressWarnings("java:S6548")
public final class MoveTypeOverlayModule extends AbstractAnchoredOverlayModule {
    public static final MoveTypeOverlayModule INSTANCE = new MoveTypeOverlayModule();

    private MoveTypeOverlayModule() {
        super(
            "move_type_overlay",
            "Move Type Overlay",
            "Shows the current pathfinder movement type while navigating.",
            KeyDisplayAnchor.TOP_LEFT);
    }

    @Override
    public void renderGameViewport(EbslPlatform platform, NavigationService navigation, UiRect viewport) {
        if (!isEnabled() || !navigation.isNavigating()) return;
        Node.MoveType moveType = navigation.currentMoveType();
        if (moveType == null) return;
        render(ImGui.getForegroundDrawList(), viewport, moveType.name());
    }

    static void renderBox(ImDrawList dl, UiRect viewport, KeyDisplayAnchor anchor, String label) {
        float pad = 12.0f;
        float textH = 10.0f;
        float boxW = label.length() * 6.5f + pad * 2;
        float boxH = textH + pad * 2;
        float x0 = anchor.x(viewport, boxW, pad);
        float y0 = anchor.y(viewport, boxH, pad);
        dl.addRectFilled(x0, y0, x0 + boxW, y0 + boxH, 0xCC101820, 4.0f);
        dl.addRect(x0, y0, x0 + boxW, y0 + boxH, 0xFF2E3C4E, 4.0f, 0, 1.0f);
        dl.addText(x0 + pad, y0 + pad, 0xFFDDEEFF, label);
    }

    private void render(ImDrawList dl, UiRect viewport, String label) {
        renderBox(dl, viewport, anchor(), label);
    }
}
