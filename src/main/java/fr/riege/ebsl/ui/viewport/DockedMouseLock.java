package fr.riege.ebsl.ui.viewport;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.layout.UiRect;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class DockedMouseLock {
    private static final double EDGE_PADDING = 1.0;

    private DockedMouseLock() {
    }

    public static void confineIfFocused(Minecraft client) {
        if (client == null
            || client.mouseHandler == null
            || client.mouseHandler.isMouseGrabbed()
            || !EbslImGuiOverlay.shouldConfineMinecraftMouse()) {
            return;
        }

        Window window = client.getWindow();
        UiRect viewport = EbslImGuiOverlay.gameViewportRect(window.getScreenWidth(), window.getScreenHeight());
        double minX = viewport.x() + EDGE_PADDING;
        double maxX = viewport.right() - EDGE_PADDING;
        double minY = viewport.y() + EDGE_PADDING;
        double maxY = viewport.bottom() - EDGE_PADDING;

        double x = client.mouseHandler.xpos();
        double y = client.mouseHandler.ypos();
        double clampedX = clamp(x, minX, maxX);
        double clampedY = clamp(y, minY, maxY);
        if (clampedX == x && clampedY == y) {
            return;
        }

        long handle = GLFW.glfwGetCurrentContext();
        if (handle != 0L) {
            GLFW.glfwSetCursorPos(handle, clampedX, clampedY);
        }
    }

    private static double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
