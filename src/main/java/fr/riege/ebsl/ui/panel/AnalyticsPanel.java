package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class AnalyticsPanel implements UiPanel {
    @Override
    public void init(EbslViewportScreen screen, ViewportLayout layout) {
    }

    @Override
    public void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                       int mouseX, int mouseY, float partialTick) {
        UiRect panel = layout.bottom();
        AnalyticsSnapshot snapshot = AnalyticsSnapshot.capture(screen.state().selectedModule());
        graphics.drawString(screen.fontRenderer(), "Analytics", panel.x() + UiTheme.PAD, panel.y() + 12, UiTheme.TEXT, false);
        graphics.drawString(screen.fontRenderer(), "Navigation: " + snapshot.navigationState(),
            panel.x() + UiTheme.PAD, panel.y() + 34, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Selected module: " + snapshot.selectedModule(),
            panel.x() + 160, panel.y() + 34, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Jump height: " + snapshot.jumpHeight(),
            panel.x() + 360, panel.y() + 34, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Visualizer: always on",
            panel.x() + UiTheme.PAD, panel.y() + 56, UiTheme.TEXT_MUTED, false);

        int x = panel.x() + Math.max(520, panel.width() / 2);
        graphics.drawString(screen.fontRenderer(), "Event log", x, panel.y() + 12, UiTheme.TEXT, false);
        List<AnalyticsEvent> events = AnalyticsEventLog.latest(4);
        int y = panel.y() + 30;
        for (AnalyticsEvent event : events) {
            graphics.drawString(screen.fontRenderer(), event.source() + ": " + event.message(), x, y, UiTheme.TEXT_DIM, false);
            y += 14;
        }
    }
}
