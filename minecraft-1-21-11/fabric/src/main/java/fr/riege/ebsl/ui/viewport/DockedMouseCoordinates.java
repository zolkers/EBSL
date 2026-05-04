package fr.riege.ebsl.ui.viewport;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.layout.UiRect;
import net.minecraft.client.Minecraft;

public final class DockedMouseCoordinates {
    private static final double OUTSIDE_VIEWPORT = -1_000_000.0;

    private DockedMouseCoordinates() {
    }

    public static double remapScaledX(Window window, double rawX, double vanillaScaledX) {
        if (!shouldRemap()) {
            return vanillaScaledX;
        }
        UiRect viewport = EbslImGuiOverlay.gameViewportRect(window.getScreenWidth(), window.getScreenHeight());
        if (rawX < viewport.x() || rawX > viewport.right()) {
            return OUTSIDE_VIEWPORT;
        }
        double t = (rawX - viewport.x()) / Math.max(1.0, viewport.width());
        return t * window.getGuiScaledWidth();
    }

    public static double remapScaledY(Window window, double rawY, double vanillaScaledY) {
        if (!shouldRemap()) {
            return vanillaScaledY;
        }
        UiRect viewport = EbslImGuiOverlay.gameViewportRect(window.getScreenWidth(), window.getScreenHeight());
        if (rawY < viewport.y() || rawY > viewport.bottom()) {
            return OUTSIDE_VIEWPORT;
        }
        double t = (rawY - viewport.y()) / Math.max(1.0, viewport.height());
        return t * window.getGuiScaledHeight();
    }

    private static boolean shouldRemap() {
        Minecraft minecraft = Minecraft.getInstance();
        return EbslImGuiOverlay.isVisible() && minecraft.screen != null;
    }
}
