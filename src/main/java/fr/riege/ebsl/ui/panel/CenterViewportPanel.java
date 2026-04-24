package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.CenterTab;
import net.minecraft.client.gui.GuiGraphics;

public final class CenterViewportPanel implements UiPanel {
    @Override
    public void init(EbslViewportScreen screen, ViewportLayout layout) {
        int x = layout.center().x() + UiTheme.PAD;
        int y = layout.center().y() + UiTheme.PAD;
        for (CenterTab tab : CenterTab.values()) {
            int width = tab == CenterTab.GAME ? 72 : 88;
            screen.addButton(tab.label(), x, y, width, 22, button -> screen.state().setCenterTab(tab));
            x += width + 6;
        }
    }

    @Override
    public void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                       int mouseX, int mouseY, float partialTick) {
        UiRect center = layout.center();
        if (screen.state().centerTab() == CenterTab.GAME) {
            graphics.drawString(screen.fontRenderer(), "Game viewport", center.x() + UiTheme.PAD, center.y() + 36, 0x88E7EEF7, false);
            graphics.drawString(screen.fontRenderer(), "World remains visible here while panels stay interactive.",
                center.x() + UiTheme.PAD, center.bottom() - 18, 0x668F9AA8, false);
            return;
        }
        UiRect inner = new UiRect(center.x() + UiTheme.PAD, center.y() + UiTheme.TAB_H + UiTheme.PAD,
            center.width() - UiTheme.PAD * 2, center.height() - UiTheme.TAB_H - UiTheme.PAD * 2);
        graphics.fill(inner.x(), inner.y(), inner.right(), inner.bottom(), 0xC0141820);
        graphics.drawString(screen.fontRenderer(), "Viewport settings", inner.x() + UiTheme.PAD, inner.y() + UiTheme.PAD, UiTheme.TEXT, false);
        graphics.drawString(screen.fontRenderer(), "Backend target: Minecraft native now, ImGui renderer later.",
            inner.x() + UiTheme.PAD, inner.y() + UiTheme.PAD * 3, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Viewport contract: panels never own game rendering state.",
            inner.x() + UiTheme.PAD, inner.y() + UiTheme.PAD * 5, UiTheme.TEXT_MUTED, false);
    }
}
