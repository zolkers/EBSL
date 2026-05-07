package fr.riege.ebsl.loader.viewport;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.common.platform.service.UiService;
import fr.riege.ebsl.common.feature.ui.CommonImGuiOverlay;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import net.minecraft.client.Minecraft;

public final class DockedMouseCoordinates {
    private static final double OUTSIDE_VIEWPORT = -1_000_000.0;

    private DockedMouseCoordinates() {
    }

    public static double remapScaledX(Window window, double rawX, double vanillaScaledX, UiService ui) {
        if (!shouldRemap(ui)) {
            return vanillaScaledX;
        }
        UiRect viewport = CommonImGuiOverlay.gameViewportRect(window.getScreenWidth(), window.getScreenHeight());
        if (rawX < viewport.x() || rawX > viewport.right()) {
            return OUTSIDE_VIEWPORT;
        }
        double t = (rawX - viewport.x()) / Math.max(1.0, viewport.width());
        return t * window.getGuiScaledWidth();
    }

    public static double remapScaledY(Window window, double rawY, double vanillaScaledY, UiService ui) {
        if (!shouldRemap(ui)) {
            return vanillaScaledY;
        }
        UiRect viewport = CommonImGuiOverlay.gameViewportRect(window.getScreenWidth(), window.getScreenHeight());
        if (rawY < viewport.y() || rawY > viewport.bottom()) {
            return OUTSIDE_VIEWPORT;
        }
        double t = (rawY - viewport.y()) / Math.max(1.0, viewport.height());
        return t * window.getGuiScaledHeight();
    }

    private static boolean shouldRemap(UiService ui) {
        Minecraft minecraft = Minecraft.getInstance();
        return ui.isVisible() && minecraft.screen != null;
    }
}
