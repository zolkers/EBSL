package fr.riege.ebsl.common.ui;

import fr.riege.ebsl.common.layer.IImGuiLayer;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.service.UiService;
import fr.riege.ebsl.common.settings.BooleanSetting;
import fr.riege.ebsl.common.settings.DoubleSetting;
import fr.riege.ebsl.common.settings.IntSetting;
import fr.riege.ebsl.common.settings.Setting;
import fr.riege.ebsl.common.terminal.CommandRegistry;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandSuggestion;
import fr.riege.ebsl.common.terminal.TerminalLog;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.List;

public final class CommonImGuiOverlay {
    private static final int HEADER_H = 34;
    private static final int BOTTOM_H = 112;
    private static final int LEFT_W = 264;
    private static final int RIGHT_W = 300;
    private static final int TAB_H = 26;

    private static final int BG_HEADER = 0xE611151A;
    private static final int BG_PANEL = 0xE6141820;
    private static final int BG_PANEL_DARK = 0xE612171E;
    private static final int BORDER = 0xFF26313D;
    private static final int ACCENT = 0xFF4A90E2;

    private static final int FIXED_PANEL_FLAGS =
        ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoTitleBar;

    private static final ImString terminalInput = new ImString(256);
    private static CenterTab centerTab = CenterTab.GAME;
    private static boolean terminalScrollToBottom;
    private static boolean styleApplied;

    private CommonImGuiOverlay() {
    }

    public static void render(EbslPlatform platform, NavigationService navigation, UiService ui) {
        if (!ui.isVisible()) {
            return;
        }
        applyStyleOnce();

        IImGuiLayer imgui = platform.imgui();
        Layout layout = Layout.create(imgui.getViewportWidth(), imgui.getViewportHeight());
        renderHeader(layout);
        renderGoals(layout, navigation, platform);
        renderCenter(layout, navigation);
        renderSystems(layout, navigation);
        renderAnalytics(layout, navigation);
        drawLayoutLines(layout);
    }

    public static Rect gameViewportRect(int width, int height) {
        Rect center = Layout.create(width, height).center;
        int top = center.y + TAB_H + 8;
        return new Rect(center.x + 8, top, center.width - 16, center.bottom() - top - 8);
    }

    public static boolean acceptsMinecraftFocusAt(double x, double y, int width, int height, UiService ui) {
        if (!shouldConfineMinecraftMouse(ui)) {
            return false;
        }
        Rect viewport = gameViewportRect(width, height);
        return x >= viewport.x
            && x <= viewport.right()
            && y >= viewport.y
            && y <= viewport.bottom();
    }

    public static boolean shouldConfineMinecraftMouse(UiService ui) {
        return ui.isVisible() && centerTab == CenterTab.GAME;
    }

    private static void renderHeader(Layout layout) {
        nextFixedWindow(layout.header);
        if (ImGui.begin("##ebsl-header", FIXED_PANEL_FLAGS)) {
            ImDrawList drawList = ImGui.getWindowDrawList();
            drawList.addRectFilled(layout.header.x, layout.header.y, layout.header.right(), layout.header.bottom(), BG_HEADER);
            ImGui.text("EBSL");
            ImGui.sameLine(72.0f);
            ImGui.button("Main", 72.0f, 18.0f);
            ImGui.sameLine();
            ImGui.button("Tools", 72.0f, 18.0f);
            ImGui.sameLine();
            ImGui.button("Debug", 72.0f, 18.0f);
        }
        ImGui.end();
    }

    private static void renderGoals(Layout layout, NavigationService navigation, EbslPlatform platform) {
        nextFixedWindow(layout.left);
        if (ImGui.begin("Goals##ebsl-left", FIXED_PANEL_FLAGS)) {
            ImGui.text("Goals");
            ImGui.separator();
            goalButton("Column +X", () -> navigation.startColumnGoal(currentBlockX(platform) + 16, currentBlockZ(platform)));
            goalButton("Column -X", () -> navigation.startColumnGoal(currentBlockX(platform) - 16, currentBlockZ(platform)));
            goalButton("Column +Z", () -> navigation.startColumnGoal(currentBlockX(platform), currentBlockZ(platform) + 16));
            goalButton("Column -Z", () -> navigation.startColumnGoal(currentBlockX(platform), currentBlockZ(platform) - 16));
            ImGui.separator();
            ImGui.textDisabled("Commands");
            ImGui.bulletText("goal column <x> <z>");
            ImGui.bulletText("goal walk <x> <y> <z>");
            ImGui.bulletText("pathtest <x> <y> <z>");
            ImGui.spacing();
            pushDangerButton();
            if (ImGui.button("Stop##goal-stop", -1.0f, 24.0f)) {
                navigation.stop(true);
                TerminalLog.addOutput("Navigation stopped.");
            }
            ImGui.popStyleColor(3);
        }
        ImGui.end();
    }

    private static void renderCenter(Layout layout, NavigationService navigation) {
        nextFixedWindow(layout.center);
        int flags = FIXED_PANEL_FLAGS | ImGuiWindowFlags.NoBackground;
        if (ImGui.begin("##ebsl-center-viewport", flags)) {
            Rect tabs = tabsRect(layout.center);
            Rect viewport = viewportRect(layout.center);
            drawTabs(tabs);
            switch (centerTab) {
                case GAME -> drawGameViewportShell(viewport, navigation);
                case PATHFINDER -> renderPathfinderSettings(viewport);
                case TERMINAL -> renderTerminal(viewport);
                case LOG -> renderLog(viewport);
            }
            drawViewportFrame(viewport);
        }
        ImGui.end();
    }

    private static void renderSystems(Layout layout, NavigationService navigation) {
        nextFixedWindow(layout.right);
        if (ImGui.begin("Pathfinder botting##ebsl-right", FIXED_PANEL_FLAGS)) {
            ImGui.text("Systems");
            ImGui.separator();
            statusRow("Navigation", navigation.pathStatus());
            statusRow("Move", navigation.currentMoveType().name());
            statusRow("Nodes", Integer.toString(navigation.lastPathNodeCount()));
            statusRow("Sneak latch", navigation.isWalkSneakLatched() ? "on" : "off");
            ImGui.spacing();
            ImBoolean debug = new ImBoolean(PathfinderSettings.instance().showDebug.value());
            if (ImGui.checkbox("Show debug", debug)) {
                PathfinderSettings.instance().showDebug.setValue(debug.get());
            }
            ImGui.spacing();
            ImGui.text("Pathfinder settings");
            renderSetting(PathfinderSettings.instance().maxJumpHeight);
            renderSetting(PathfinderSettings.instance().defaultWalkMaxIterations);
            renderSetting(PathfinderSettings.instance().defaultWalkMaxLength);
            renderSetting(PathfinderSettings.instance().walkTargetDeadzone);
            renderSetting(PathfinderSettings.instance().jumpTriggerDist);
        }
        ImGui.end();
    }

    private static void renderAnalytics(Layout layout, NavigationService navigation) {
        nextFixedWindow(layout.bottom);
        if (ImGui.begin("Analytics##ebsl-bottom", FIXED_PANEL_FLAGS)) {
            ImGui.text("Analytics");
            ImGui.separator();
            ImGui.columns(2, "ebsl-analytics-columns", false);
            ImGui.textDisabled("Navigation: " + navigation.pathStatus());
            ImGui.textDisabled("Selected module: common runtime");
            ImGui.textDisabled("Jump height: " + PathfinderSettings.instance().maxJumpHeight.value());
            ImGui.textDisabled("Visualizer: common render layer");
            ImGui.nextColumn();
            ImGui.text("Event log");
            List<TerminalLog.LogEntry> entries = TerminalLog.snapshot();
            int start = Math.max(0, entries.size() - 6);
            for (int i = start; i < entries.size(); i++) {
                TerminalLog.LogEntry entry = entries.get(i);
                ImGui.textDisabled(entry.type() + ": " + entry.text());
            }
            ImGui.columns(1);
        }
        ImGui.end();
    }

    private static void drawTabs(Rect tabs) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(tabs.x, tabs.y, tabs.right(), tabs.bottom(), 0xEE10141A);
        ImGui.setCursorScreenPos(tabs.x + 8.0f, tabs.y + 5.0f);
        for (CenterTab tab : CenterTab.values()) {
            if (ImGui.button(tab.label, tab.width, 22.0f)) {
                centerTab = tab;
            }
            ImGui.sameLine();
        }
    }

    private static void drawGameViewportShell(Rect viewport, NavigationService navigation) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x, viewport.y, viewport.right(), viewport.bottom(), 0x18000000);
        ImGui.setCursorScreenPos(viewport.x + 14.0f, viewport.y + 14.0f);
        ImGui.textDisabled("Minecraft viewport");
        ImGui.text("Navigation: " + navigation.pathStatus());
        ImGui.text("Move type: " + navigation.currentMoveType());
        ImGui.text("Path nodes: " + navigation.lastPathNodeCount());
    }

    private static void renderPathfinderSettings(Rect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x, viewport.y, viewport.right(), viewport.bottom(), BG_PANEL_DARK);
        drawList.addRectFilled(viewport.x, viewport.y, viewport.right(), viewport.y + 34.0f, BG_PANEL);
        drawList.addLine(viewport.x, viewport.y + 34.0f, viewport.right(), viewport.y + 34.0f, BORDER, 1.0f);
        ImGui.setCursorScreenPos(viewport.x + 14.0f, viewport.y + 14.0f);
        ImGui.beginChild("##pathfinder-settings-scroll", viewport.width - 28.0f, viewport.height - 28.0f, false);
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) {
            PathfinderSettings.resetToDefaults();
        }
        renderSettingsGroup("General", PathfinderSettings.generalSettings());
        renderSettingsGroup("Movement cost", PathfinderSettings.movementCostSettings());
        renderSettingsGroup("Execution", PathfinderSettings.executionSettings());
        renderSettingsGroup("Recovery", PathfinderSettings.recoverySettings());
        renderSettingsGroup("Smoothing", PathfinderSettings.smoothingSettings());
        ImGui.endChild();
    }

    private static void renderSettingsGroup(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(false, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) {
            return;
        }
        ImGui.indent(10.0f);
        for (Setting<?> setting : settings) {
            renderSetting(setting);
        }
        ImGui.unindent(10.0f);
    }

    private static void renderTerminal(Rect viewport) {
        ImGui.getWindowDrawList().addRectFilled(viewport.x, viewport.y, viewport.right(), viewport.bottom(), 0xEE0D1117);
        float inputH = 28.0f;
        float boxX = viewport.x + 8.0f;
        float boxRight = viewport.right() - 8.0f;
        float boxW = boxRight - boxX;
        float logH = viewport.height - inputH - 20.0f;

        ImGui.setCursorScreenPos(boxX, viewport.y + 8.0f);
        if (ImGui.beginChild("##terminal-log", boxW, logH, false)) {
            if (TerminalLog.consumeDirty()) terminalScrollToBottom = true;
            for (TerminalLog.LogEntry entry : TerminalLog.snapshot()) {
                switch (entry.type()) {
                    case INPUT -> ImGui.textColored(0.45f, 0.55f, 0.65f, 1.0f, entry.text());
                    case OUTPUT -> ImGui.textColored(0.87f, 0.93f, 0.97f, 1.0f, entry.text());
                    case ERROR -> ImGui.textColored(0.90f, 0.35f, 0.30f, 1.0f, entry.text());
                }
            }
            if (terminalScrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                terminalScrollToBottom = false;
            }
            ImGui.endChild();
        }

        ImGui.setCursorScreenPos(boxX, viewport.y + 8.0f + logH + 4.0f);
        ImGui.textColored(0.45f, 0.75f, 0.45f, 1.0f, ">");
        ImGui.sameLine();
        ImGui.setNextItemWidth(boxW - 22.0f);
        boolean submitted = ImGui.inputText("##terminal-input", terminalInput, ImGuiInputTextFlags.EnterReturnsTrue);
        if (submitted && !terminalInput.get().isBlank()) {
            dispatchTerminal(terminalInput.get());
            terminalInput.set("");
            terminalScrollToBottom = true;
        }

        List<CommandSuggestion> suggestions = CommandRegistry.suggest(terminalInput.get());
        if (!suggestions.isEmpty() && !terminalInput.get().isBlank()) {
            ImGui.setCursorScreenPos(boxX + 18.0f, viewport.y + 8.0f + logH - 22.0f);
            ImGui.textDisabled(suggestions.getFirst().fill() + " " + suggestions.getFirst().hint());
        }
    }

    private static void renderLog(Rect viewport) {
        ImGui.getWindowDrawList().addRectFilled(viewport.x, viewport.y, viewport.right(), viewport.bottom(), 0xEE0A0F14);
        ImGui.setCursorScreenPos(viewport.x + 14.0f, viewport.y + 14.0f);
        if (ImGui.button("Clear##log-clear", 72.0f, 22.0f)) {
            TerminalLog.clear();
        }
        ImGui.separator();
        ImGui.beginChild("##log-scroll", viewport.width - 28.0f, viewport.height - 52.0f, false);
        for (TerminalLog.LogEntry entry : TerminalLog.snapshot()) {
            ImGui.textDisabled(entry.type() + ": " + entry.text());
        }
        ImGui.endChild();
    }

    private static void dispatchTerminal(String input) {
        TerminalLog.addInput("> " + input);
        CommandResult result = CommandRegistry.dispatch(input);
        for (String line : result.lines()) {
            if (result.success()) {
                TerminalLog.addOutput(line);
            } else {
                TerminalLog.addError(line);
            }
        }
    }

    private static void renderSetting(Setting<?> setting) {
        if (setting instanceof BooleanSetting boolSetting) {
            ImBoolean value = new ImBoolean(boolSetting.value());
            if (ImGui.checkbox(setting.displayName(), value)) {
                boolSetting.setValue(value.get());
            }
        } else if (setting instanceof IntSetting intSetting) {
            int[] value = {intSetting.value()};
            if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                intSetting.setValue(value[0]);
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            float[] value = {(float) doubleSetting.value().doubleValue()};
            if (ImGui.sliderFloat(setting.displayName(), value, (float) doubleSetting.min(), (float) doubleSetting.max(), "%.2f")) {
                doubleSetting.setValue((double) value[0]);
            }
        } else {
            ImGui.textDisabled(setting.displayName() + ": " + setting.value());
        }
    }

    private static void goalButton(String label, Runnable action) {
        if (ImGui.button(label, -1.0f, 24.0f)) {
            action.run();
        }
    }

    private static int currentBlockX(EbslPlatform platform) {
        return (int) Math.floor(platform.player().position().x());
    }

    private static int currentBlockZ(EbslPlatform platform) {
        return (int) Math.floor(platform.player().position().z());
    }

    private static void statusRow(String label, String value) {
        ImGui.textDisabled(label + ":");
        ImGui.sameLine(118.0f);
        ImGui.text(value);
    }

    private static void pushDangerButton() {
        ImGui.pushStyleColor(ImGuiCol.Button, 0xFF8A2630);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0xFFA8323E);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0xFFC23B49);
    }

    private static Rect tabsRect(Rect center) {
        return new Rect(center.x, center.y, center.width, TAB_H + 8);
    }

    private static Rect viewportRect(Rect center) {
        int top = center.y + TAB_H + 8;
        return new Rect(center.x + 8, top, center.width - 16, center.bottom() - top - 8);
    }

    private static void drawViewportFrame(Rect viewport) {
        ImDrawList drawList = ImGui.getForegroundDrawList();
        drawList.addRect(viewport.x, viewport.y, viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        drawList.addLine(viewport.x, viewport.y, viewport.right(), viewport.y, 0xFFFFFFFF, 1.0f);
        drawList.addLine(viewport.x, viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }

    private static void drawLayoutLines(Layout layout) {
        drawRectBorder(layout.center, ACCENT, 2.0f);
        drawRectBorder(layout.left, BORDER, 1.0f);
        drawRectBorder(layout.right, BORDER, 1.0f);
        drawRectBorder(layout.bottom, BORDER, 1.0f);
        ImGui.getForegroundDrawList().addLine(layout.center.x, layout.center.y, layout.center.right(), layout.center.y, ACCENT, 2.0f);
        ImGui.getForegroundDrawList().addLine(layout.center.x, layout.center.bottom(), layout.center.right(), layout.center.bottom(), ACCENT, 2.0f);
    }

    private static void nextFixedWindow(Rect rect) {
        ImGui.setNextWindowPos(rect.x, rect.y, ImGuiCond.Always);
        ImGui.setNextWindowSize(rect.width, rect.height, ImGuiCond.Always);
    }

    private static void drawRectBorder(Rect rect, int color, float thickness) {
        ImGui.getForegroundDrawList().addRect(rect.x, rect.y, rect.right(), rect.bottom(), color, 0.0f, 0, thickness);
    }

    private static void applyStyleOnce() {
        if (styleApplied) return;
        styleApplied = true;
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(0.0f);
        style.setChildRounding(0.0f);
        style.setFrameRounding(3.0f);
        style.setGrabRounding(3.0f);
        style.setWindowBorderSize(0.0f);
        style.setFrameBorderSize(0.0f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }

    private enum CenterTab {
        GAME("Game", 72.0f),
        PATHFINDER("Pathfinder", 112.0f),
        TERMINAL("Terminal", 86.0f),
        LOG("Log", 72.0f);

        private final String label;
        private final float width;

        CenterTab(String label, float width) {
            this.label = label;
            this.width = width;
        }
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
    }

    private record Layout(Rect header, Rect left, Rect center, Rect right, Rect bottom) {
        static Layout create(int width, int height) {
            int bottomY = Math.max(HEADER_H + 80, height - BOTTOM_H);
            int rightX = Math.max(LEFT_W + 120, width - RIGHT_W);
            return new Layout(
                new Rect(0, 0, width, HEADER_H),
                new Rect(0, HEADER_H, LEFT_W, bottomY - HEADER_H),
                new Rect(LEFT_W, HEADER_H, rightX - LEFT_W, bottomY - HEADER_H),
                new Rect(rightX, HEADER_H, width - rightX, bottomY - HEADER_H),
                new Rect(0, bottomY, width, height - bottomY));
        }
    }
}
