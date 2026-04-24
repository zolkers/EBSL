package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import net.minecraft.client.gui.GuiGraphics;

public interface UiPanel {
    void init(EbslViewportScreen screen, ViewportLayout layout);

    void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                int mouseX, int mouseY, float partialTick);
}
