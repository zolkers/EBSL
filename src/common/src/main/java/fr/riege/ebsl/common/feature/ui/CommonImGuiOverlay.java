package fr.riege.ebsl.common.feature.ui;

import fr.riege.ebsl.common.platform.layer.IImGuiLayer;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.platform.service.UiService;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiViewportRenderer;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.CenterTab;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.feature.ui.state.MainViewTab;

public final class CommonImGuiOverlay {
    private static final EbslUiState STATE = new EbslUiState();
    private static final ImGuiViewportRenderer RENDERER = new ImGuiViewportRenderer();

    private CommonImGuiOverlay() {}

    public static UiRect gameViewportRect(int width, int height) {
        UiRect center = ViewportLayout.create(width, height).center();
        int top = center.y() + UiTheme.TAB_H + 8;
        return new UiRect(center.x() + 8, top, center.width() - 16, center.bottom() - top - 8);
    }

    public static boolean acceptsMinecraftFocusAt(double x, double y, int width, int height, UiService ui) {
        if (!shouldConfineMinecraftMouse(ui)) return false;
        UiRect viewport = gameViewportRect(width, height);
        return x >= viewport.x() && x <= viewport.right()
            && y >= viewport.y() && y <= viewport.bottom();
    }

    public static boolean shouldConfineMinecraftMouse(UiService ui) {
        return ui.isVisible() && STATE.mainViewTab() == MainViewTab.MAIN && STATE.centerTab() == CenterTab.GAME;
    }

    public static void render(EbslPlatform platform, NavigationService navigation, UiService ui) {
        if (!ui.isVisible()) return;
        IImGuiLayer imgui = platform.imgui();
        ViewportLayout layout = ViewportLayout.create(imgui.getViewportWidth(), imgui.getViewportHeight());
        RENDERER.render(STATE, layout, navigation, platform);
        if (STATE.mainViewTab() == MainViewTab.MAIN && STATE.centerTab() == CenterTab.GAME) {
            BotModuleRegistry.renderGameViewport(platform, navigation, gameViewportRect(imgui.getViewportWidth(), imgui.getViewportHeight()));
        }
    }
}
