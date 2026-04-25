package fr.riege.ebsl.ui.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.event.events.input.CharTypedEvent;
import fr.riege.ebsl.event.events.input.GrabMouseEvent;
import fr.riege.ebsl.event.events.input.KeyPressEvent;
import fr.riege.ebsl.event.events.input.MouseButtonEvent;
import fr.riege.ebsl.event.events.input.ScaledMousePosEvent;
import fr.riege.ebsl.event.events.render.BlitToScreenEvent;
import fr.riege.ebsl.ui.viewport.DockedMinecraftCompositor;
import fr.riege.ebsl.ui.viewport.DockedMouseCoordinates;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DockingInputHandler {
    private static final AtomicBoolean pendingGrabApproved = new AtomicBoolean(false);

    private DockingInputHandler() {}

    public static void register(EventBus bus) {
        bus.subscribe(GrabMouseEvent.class, DockingInputHandler::onGrabMouse);
        bus.subscribe(MouseButtonEvent.class, DockingInputHandler::onMouseButton);
        bus.subscribe(KeyPressEvent.class, DockingInputHandler::onKeyPress);
        bus.subscribe(CharTypedEvent.class, DockingInputHandler::onCharTyped);
        bus.subscribe(ScaledMousePosEvent.class, DockingInputHandler::onScaledMousePos);
        bus.subscribe(BlitToScreenEvent.class, DockingInputHandler::onBlitToScreen);
    }

    private static void onGrabMouse(GrabMouseEvent event) {
        if (!EbslImGuiOverlay.isVisible()) return;
        if (pendingGrabApproved.compareAndSet(true, false)) return;
        Window window = Minecraft.getInstance().getWindow();
        if (!EbslImGuiOverlay.acceptsMinecraftFocusAt(
                event.getXpos(), event.getYpos(), window.getScreenWidth(), window.getScreenHeight())) {
            event.cancel();
        }
    }

    private static void onMouseButton(MouseButtonEvent event) {
        if (!EbslImGuiOverlay.isVisible()) return;
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        if (event.getWindowHandle() != window.handle()) return;
        double[] x = {0}, y = {0};
        GLFW.glfwGetCursorPos(event.getWindowHandle(), x, y);
        if (!EbslImGuiOverlay.acceptsMinecraftFocusAt(x[0], y[0], window.getScreenWidth(), window.getScreenHeight())) {
            minecraft.mouseHandler.releaseMouse();
            event.cancel();
        } else {
            pendingGrabApproved.set(true);
        }
    }

    private static void onKeyPress(KeyPressEvent event) {
        if (shouldRouteKeyboardToImGui()) event.cancel();
    }

    private static void onCharTyped(CharTypedEvent event) {
        if (shouldRouteKeyboardToImGui()) event.cancel();
    }

    private static void onScaledMousePos(ScaledMousePosEvent event) {
        if (event.getAxis() == ScaledMousePosEvent.Axis.X) {
            event.setScaledPos(DockedMouseCoordinates.remapScaledX(event.getWindow(), event.getRawPos(), event.getScaledPos()));
        } else {
            event.setScaledPos(DockedMouseCoordinates.remapScaledY(event.getWindow(), event.getRawPos(), event.getScaledPos()));
        }
    }

    private static void onBlitToScreen(BlitToScreenEvent event) {
        RenderTarget target = event.getTarget();
        if (target != Minecraft.getInstance().getMainRenderTarget() || !EbslImGuiOverlay.isVisible()) return;
        DockedMinecraftCompositor.compose(target);
        EbslImGuiOverlay.render();
    }

    private static boolean shouldRouteKeyboardToImGui() {
        Minecraft minecraft = Minecraft.getInstance();
        return EbslImGuiOverlay.isVisible()
            && minecraft.mouseHandler != null
            && !minecraft.mouseHandler.isMouseGrabbed();
    }
}
