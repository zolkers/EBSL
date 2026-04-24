package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.MainViewTab;
import net.minecraft.client.gui.GuiGraphics;

public final class HeaderPanel implements UiPanel {
    @Override
    public void init(EbslViewportScreen screen, ViewportLayout layout) {
        int x = UiTheme.PAD;
        for (MainViewTab tab : MainViewTab.values()) {
            screen.addButton(tab.label(), x, 6, 72, 22, button -> screen.state().setMainViewTab(tab));
            x += 78;
        }
    }

    @Override
    public void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                       int mouseX, int mouseY, float partialTick) {
        graphics.drawString(screen.fontRenderer(), "EBSL", 98, 12, UiTheme.TEXT, false);
    }
}
