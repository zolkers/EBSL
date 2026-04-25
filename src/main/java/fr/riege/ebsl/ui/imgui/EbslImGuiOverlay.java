package fr.riege.ebsl.ui.imgui;

import cn.enaium.fabric.imgui.FabricImGui;
import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.state.EbslUiState;
import fr.riege.ebsl.ui.state.CenterTab;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;

public final class EbslImGuiOverlay {
    private static final EbslUiState STATE = new EbslUiState();
    private static final ImGuiViewportRenderer RENDERER = new ImGuiViewportRenderer();
    private static boolean visible;

    private EbslImGuiOverlay() {
    }

    public static boolean toggle() {
        visible = !visible;
        return visible;
    }

    public static boolean isVisible() {
        return visible;
    }

    public static UiRect gameViewportRect(int width, int height) {
        UiRect center = ViewportLayout.create(width, height).center();
        int top = center.y() + UiTheme.TAB_H + 8;
        return new UiRect(center.x() + 8, top, center.width() - 16, center.bottom() - top - 8);
    }

    public static boolean acceptsMinecraftFocusAt(double x, double y, int width, int height) {
        if (!shouldConfineMinecraftMouse()) {
            return false;
        }
        UiRect viewport = gameViewportRect(width, height);
        return x >= viewport.x()
            && x <= viewport.right()
            && y >= viewport.y()
            && y <= viewport.bottom();
    }

    public static boolean shouldConfineMinecraftMouse() {
        return visible && STATE.centerTab() == CenterTab.GAME;
    }

    public static void render() {
        if (!visible) {
            return;
        }
        FabricImGui.IMGUI.draw(EbslImGuiOverlay::renderFrame);
    }

    private static void renderFrame(ImGuiIO io) {
        releaseMcMouseIfClickedInImGui(io);
        suppressInputWhenMinecraftIsFocused(io);
        ViewportLayout layout = ViewportLayout.create((int) io.getDisplaySizeX(), (int) io.getDisplaySizeY());
        RENDERER.render(STATE, layout);
    }

    // Release MC's mouse grab before the suppression check runs, so the suppress
    // logic sees isMouseGrabbed()=false and leaves ImGui input alone on the click frame.
    private static void releaseMcMouseIfClickedInImGui(ImGuiIO io) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!visible || minecraft.mouseHandler == null || !minecraft.mouseHandler.isMouseGrabbed()) {
            return;
        }
        if (!ImGui.isMouseClicked(0) && !ImGui.isMouseClicked(1) && !ImGui.isMouseClicked(2)) {
            return;
        }
        Window window = minecraft.getWindow();
        double x = io.getMousePosX();
        double y = io.getMousePosY();
        if (!acceptsMinecraftFocusAt(x, y, window.getScreenWidth(), window.getScreenHeight())) {
            minecraft.mouseHandler.releaseMouse();
        }
    }

    private static void suppressInputWhenMinecraftIsFocused(ImGuiIO io) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean minecraftFocused = shouldConfineMinecraftMouse()
            && minecraft.mouseHandler != null
            && minecraft.mouseHandler.isMouseGrabbed();

        if (!minecraftFocused) {
            return;
        }

        io.clearInputMouse();
        io.setMousePos(-1_000_000.0f, -1_000_000.0f);
        io.setMouseDelta(0.0f, 0.0f);
        io.setMouseWheel(0.0f);
        io.setWantCaptureMouse(false);
        io.setWantCaptureKeyboard(false);
        io.setWantTextInput(false);
    }
}
