package fr.riege.ebsl.ui.panel;

import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.command.GoalCommandSupport;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.ui.EbslViewportScreen;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

public final class GoalsPanel implements UiPanel {
    private EditBox goalX;
    private EditBox goalY;
    private EditBox goalZ;

    @Override
    public void init(EbslViewportScreen screen, ViewportLayout layout) {
        UiRect panel = layout.left();
        int x = panel.x() + UiTheme.PAD + 42;
        int y = panel.y() + 48;
        goalX = screen.addEditBox(x, y, 70, 18, "X");
        goalY = screen.addEditBox(x + 76, y, 70, 18, "Y");
        goalZ = screen.addEditBox(x + 152, y, 70, 18, "Z");
        fillCurrentPosition();
        screen.addButton("Use current", panel.x() + UiTheme.PAD, y + 26, 112, 22, button -> fillCurrentPosition());
        screen.addButton("Go", panel.right() - 92, y + 26, 76, 22, button -> startGoal());
    }

    @Override
    public void render(EbslViewportScreen screen, GuiGraphics graphics, ViewportLayout layout,
                       int mouseX, int mouseY, float partialTick) {
        UiRect panel = layout.left();
        graphics.drawString(screen.fontRenderer(), "Goals", panel.x() + UiTheme.PAD, panel.y() + 16, UiTheme.TEXT, false);
        graphics.drawString(screen.fontRenderer(), "X", panel.x() + UiTheme.PAD, panel.y() + 52, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Y", panel.x() + UiTheme.PAD + 118, panel.y() + 52, UiTheme.TEXT_MUTED, false);
        graphics.drawString(screen.fontRenderer(), "Z", panel.x() + UiTheme.PAD + 194, panel.y() + 52, UiTheme.TEXT_MUTED, false);
    }

    private void fillCurrentPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || goalX == null) {
            return;
        }
        goalX.setValue(Integer.toString((int) Math.floor(mc.player.getX())));
        goalY.setValue(Integer.toString((int) Math.floor(mc.player.getY())));
        goalZ.setValue(Integer.toString((int) Math.floor(mc.player.getZ())));
    }

    private void startGoal() {
        Integer x = parseInt(goalX.getValue());
        Integer y = parseInt(goalY.getValue());
        Integer z = parseInt(goalZ.getValue());
        if (x == null || y == null || z == null) {
            GoalCommandSupport.sendClientMessage("Invalid goal coordinates.");
            AnalyticsEventLog.record("goal", "Invalid goal coordinates");
            return;
        }
        PathfindingManager.startGoal(Minecraft.getInstance(), NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(NavigationModeType.WALK)
            .build());
        AnalyticsEventLog.record("goal", "Started walk goal " + x + ", " + y + ", " + z);
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
