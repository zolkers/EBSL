package fr.riege.ebsl.ui;

import cn.enaium.fabric.imgui.ImGuiRenderable;
import fr.riege.ebsl.ui.imgui.ImGuiViewportRenderer;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGuiIO;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EbslViewportScreen extends Screen implements ImGuiRenderable {
    private final EbslUiState state = new EbslUiState();
    private final ImGuiViewportRenderer imguiRenderer = new ImGuiViewportRenderer();
    private ViewportLayout layout;

    public EbslViewportScreen() {
        super(Component.literal("EBSL"));
    }

    @Override
    protected void init() {
        layout = ViewportLayout.create(width, height);
        clearWidgets();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (layout == null) {
            layout = ViewportLayout.create(width, height);
        }
        graphics.fill(0, 0, width, height, 0x22000000);
    }

    public EbslUiState state() {
        return state;
    }

    public Font fontRenderer() {
        return font;
    }

    public Button addButton(String label, int x, int y, int width, int height, Button.OnPress onPress) {
        return addRenderableWidget(Button.builder(Component.literal(label), onPress).bounds(x, y, width, height).build());
    }

    public EditBox addEditBox(int x, int y, int width, int height, String label) {
        return addRenderableWidget(new EditBox(font, x, y, width, height, Component.literal(label)));
    }

    public void rebuildUi() {
        init();
    }

    @Override
    public void render(ImGuiIO io) {
        layout = ViewportLayout.create((int) io.getDisplaySizeX(), (int) io.getDisplaySizeY());
        imguiRenderer.render(state, layout);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
