package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.log.AppLog;
import fr.riege.ebsl.common.domain.packet.PacketCaptureEvent;
import fr.riege.ebsl.common.domain.packet.PacketCaptureLog;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.terminal.CommandRegistry;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import fr.riege.ebsl.common.feature.terminal.CommandSuggestion;
import fr.riege.ebsl.common.feature.terminal.TerminalLog;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.CenterTab;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ImGuiCenterViewportPanel implements ImGuiUiPanel {
    private static final DateTimeFormatter PACKET_TIME =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final ImString packetFilter   = new ImString(64);
    private final ImString terminalInput  = new ImString(256);
    private boolean terminalScrollToBottom;
    private boolean logScrollToBottom;
    private final List<CommandSuggestion> suggestions = new ArrayList<>();
    private int suggestionIdx;
    private String lastSuggestInput;
    private boolean scrollSuggestToSelected;
    private boolean terminalFocused;

    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        ImGuiPanelUtil.nextFixedWindow(layout.center());
        int flags = ImGuiPanelUtil.FIXED_PANEL_FLAGS | ImGuiWindowFlags.NoBackground;
        if (ImGui.begin("##ebsl-center-viewport", flags)) {
            UiRect tabs     = tabsRect(layout);
            UiRect viewport = viewportRect(layout);
            drawTabs(state, tabs);
            switch (state.centerTab()) {
                case GAME                -> { terminalFocused = false; drawGameViewportShell(viewport); }
                case PATHFINDER_SETTINGS -> { terminalFocused = false; renderPathfinderSettings(viewport); }
                case PACKET              -> { terminalFocused = false; renderPacketView(viewport); }
                case TERMINAL            -> renderTerminal(viewport);
                case MC_LOG              -> { terminalFocused = false; renderMcLog(viewport); }
            }
            drawViewportFrame(viewport);
            ImGui.end();
        }
    }

    private UiRect tabsRect(ViewportLayout layout) {
        UiRect c = layout.center();
        return new UiRect(c.x(), c.y(), c.width(), UiTheme.TAB_H + 8);
    }

    private UiRect viewportRect(ViewportLayout layout) {
        UiRect c = layout.center();
        int top = c.y() + UiTheme.TAB_H + 8;
        return new UiRect(c.x() + 8, top, c.width() - 16, c.bottom() - top - 8);
    }

    private void drawTabs(EbslUiState state, UiRect tabs) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(tabs.x(), tabs.y(), tabs.right(), tabs.bottom(), 0xEE10141A);
        ImGui.setCursorScreenPos(tabs.x() + 8.0f, tabs.y() + 5.0f);
        for (CenterTab tab : CenterTab.values()) {
            float w = switch (tab) {
                case GAME                -> 72.0f;
                case PACKET              -> 86.0f;
                case PATHFINDER_SETTINGS -> 148.0f;
                case TERMINAL            -> 86.0f;
                case MC_LOG              -> 72.0f;
            };
            if (ImGui.button(tab.label(), w, 22.0f)) state.setCenterTab(tab);
            ImGui.sameLine();
        }
    }

    private void drawGameViewportShell(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0x18000000);
    }

    private void renderPathfinderSettings(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);
        dl.addLine(viewport.x(), viewport.y() + 34.0f, viewport.right(), viewport.y() + 34.0f, UiTheme.BORDER, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginChild("##pf-settings-scroll", viewport.width() - 28.0f, viewport.height() - 28.0f, false);
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) PathfinderSettings.resetToDefaults();
        ImGui.spacing();
        renderSettingsGroup("General",            PathfinderSettings.generalSettings());
        renderSettingsGroup("Movement cost",      PathfinderSettings.movementCostSettings());
        renderSettingsGroup("Execution",          PathfinderSettings.executionSettings());
        renderSettingsGroup("Recovery",           PathfinderSettings.recoverySettings());
        renderSettingsGroup("Smoothing",          PathfinderSettings.smoothingSettings());
        ImGui.endChild();
    }

    private void renderSettingsGroup(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) return;
        ImGui.indent(10.0f);
        for (Setting<?> s : settings) renderSetting(s);
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    private void renderSetting(Setting<?> setting) {
        if (setting instanceof BooleanSetting s) {
            ImBoolean v = new ImBoolean(s.value());
            if (ImGui.checkbox(setting.displayName(), v)) { s.setValue(v.get()); PathfinderSettings.save(); }
        } else if (setting instanceof IntSetting s) {
            int[] v = {s.value()};
            if (ImGui.sliderInt(setting.displayName(), v, s.min(), s.max())) { s.setValue(v[0]); PathfinderSettings.save(); }
        } else if (setting instanceof DoubleSetting s) {
            float[] v = {(float) s.value().doubleValue()};
            if (ImGui.sliderFloat(setting.displayName(), v, (float) s.min(), (float) s.max(), "%.2f")) {
                s.setValue((double) v[0]); PathfinderSettings.save();
            }
        }
    }

    private void renderPacketView(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE101820);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginGroup();
        ImGui.text("Packet");

        ImBoolean enabled = new ImBoolean(PacketCaptureLog.isEnabled());
        if (ImGui.checkbox("Capture", enabled)) PacketCaptureLog.setEnabled(enabled.get());
        ImGui.sameLine();
        ImBoolean inbound = new ImBoolean(PacketCaptureLog.isCaptureInbound());
        if (ImGui.checkbox("S2C", inbound)) PacketCaptureLog.setCaptureInbound(inbound.get());
        ImGui.sameLine();
        ImBoolean outbound = new ImBoolean(PacketCaptureLog.isCaptureOutbound());
        if (ImGui.checkbox("C2S", outbound)) PacketCaptureLog.setCaptureOutbound(outbound.get());
        ImGui.sameLine();
        if (ImGui.button("Clear", 72.0f, 22.0f)) PacketCaptureLog.clear();
        ImGui.sameLine();
        ImGui.textDisabled("Inbound: " + PacketCaptureLog.inboundCount()
            + " | Outbound: " + PacketCaptureLog.outboundCount());

        ImGui.inputText("Filter", packetFilter);
        ImGui.separator();
        renderPacketRows(viewport);
        ImGui.endGroup();
    }

    private void renderPacketRows(UiRect viewport) {
        List<PacketCaptureEvent> events = PacketCaptureLog.snapshot();
        String filter = packetFilter.get().trim().toLowerCase();
        float listH = Math.max(80.0f, viewport.height() - 112.0f);
        if (ImGui.beginChild("##packet-log", viewport.width() - 28.0f, listH, true)) {
            int rendered = 0;
            for (int i = events.size() - 1; i >= 0 && rendered < 350; i--) {
                PacketCaptureEvent event = events.get(i);
                if (!matchesFilter(event, filter)) continue;
                ImGui.textDisabled(formatPacket(event));
                rendered++;
            }
            if (rendered == 0) ImGui.textDisabled("No packets captured yet.");
            ImGui.endChild();
        }
    }

    private static boolean matchesFilter(PacketCaptureEvent event, String filter) {
        if (filter.isBlank()) return true;
        return event.packetId().toLowerCase().contains(filter)
            || event.packetClass().toLowerCase().contains(filter)
            || event.direction().label().toLowerCase().contains(filter);
    }

    private static String formatPacket(PacketCaptureEvent event) {
        String flags = (event.terminal() ? " terminal" : "") + (event.skippable() ? " skippable" : "");
        return "#" + event.sequence()
            + " " + PACKET_TIME.format(Instant.ofEpochMilli(event.capturedAtMs()))
            + " " + event.direction().label()
            + " " + event.packetId()
            + " [" + event.packetClass() + "]"
            + flags;
    }

    private void renderTerminal(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0D1117);

        final float inputH    = 28.0f;
        final float sugRowH   = 20.0f;
        final int maxVisible  = 6;
        final float boxX      = viewport.x() + 8.0f;
        final float boxRight  = viewport.right() - 8.0f;
        final float boxW      = boxRight - boxX;

        if (lastSuggestInput == null) refreshSuggestions("");
        List<CommandSuggestion> snap = List.copyOf(suggestions);
        int sugCount   = snap.size();
        float sugBoxH  = sugCount > 0 ? Math.min(sugCount, maxVisible) * sugRowH + 8.0f : 0.0f;
        float logH     = viewport.height() - inputH - 16.0f - (sugCount > 0 ? sugBoxH + 4.0f : 0.0f);

        ImGui.setCursorScreenPos(boxX, viewport.y() + 8.0f);
        if (ImGui.beginChild("##terminal-log", boxW, logH, false)) {
            if (TerminalLog.consumeDirty()) terminalScrollToBottom = true;
            for (TerminalLog.LogEntry entry : TerminalLog.snapshot()) {
                switch (entry.type()) {
                    case INPUT  -> ImGui.textColored(0.45f, 0.55f, 0.65f, 1.0f, entry.text());
                    case OUTPUT -> ImGui.textColored(0.87f, 0.93f, 0.97f, 1.0f, entry.text());
                    case ERROR  -> ImGui.textColored(0.90f, 0.35f, 0.30f, 1.0f, entry.text());
                }
            }
            if (terminalScrollToBottom) { ImGui.setScrollHereY(1.0f); terminalScrollToBottom = false; }
            ImGui.endChild();
        }

        float sugY   = viewport.y() + 8.0f + logH + 4.0f;
        float inputY = sugY + sugBoxH + (sugCount > 0 ? 4.0f : 0.0f);

        if (sugCount > 0) {
            dl.addRectFilled(boxX, sugY, boxRight, sugY + sugBoxH, 0xF01A2230);
            dl.addRect(boxX, sugY, boxRight, sugY + sugBoxH, 0xFF2D4A6A, 0, 0, 1.0f);
            ImGui.setCursorScreenPos(boxX + 1, sugY + 4.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg,             0x00000000);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrab,       0xFF334455);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabHovered,0xFF4466AA);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabActive, 0xFF5588CC);
            if (ImGui.beginChild("##terminal-suggest", boxW - 2, sugBoxH - 8.0f)) {
                float cx = ImGui.getCursorScreenPosX();
                float rw = ImGui.getContentRegionAvailX();
                for (int i = 0; i < sugCount; i++) {
                    CommandSuggestion sug = snap.get(i);
                    boolean sel = i == suggestionIdx;
                    float ry = ImGui.getCursorScreenPosY();
                    if (sel && scrollSuggestToSelected) ImGui.setScrollHereY(0.5f);
                    if (sel) ImGui.getWindowDrawList().addRectFilled(cx - 2, ry, cx + rw + 2, ry + sugRowH, 0xFF1E3A55);
                    ImGui.setCursorScreenPos(cx + 6.0f, ry + 2.0f);
                    if (sel) ImGui.textColored(1.0f, 0.85f, 0.40f, 1.0f, sug.fill());
                    else     ImGui.textColored(0.70f, 0.82f, 0.95f, 1.0f, sug.fill());
                    if (!sug.hint().isEmpty()) { ImGui.sameLine(); ImGui.textColored(0.38f, 0.45f, 0.54f, 1.0f, sug.hint()); }
                    ImGui.setCursorScreenPos(cx, ry + sugRowH);
                }
                scrollSuggestToSelected = false;
                ImGui.endChild();
            }
            ImGui.popStyleColor(4);
        }

        dl.addRectFilled(boxX, inputY, boxRight, inputY + inputH, 0xFF131921);
        ImGui.setCursorScreenPos(boxX + 4.0f, inputY + 6.0f);
        ImGui.textColored(0.45f, 0.75f, 0.45f, 1.0f, ">");

        if (!terminalFocused) { ImGui.setKeyboardFocusHere(0); terminalFocused = true; }
        ImGui.setCursorScreenPos(boxX + 18.0f, inputY + 5.0f);
        ImGui.setNextItemWidth(boxW - 18.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.FrameBg, 0x00000000);
        boolean submitted = ImGui.inputText("##terminal-input", terminalInput,
            ImGuiInputTextFlags.EnterReturnsTrue
                | ImGuiInputTextFlags.CallbackAlways
                | ImGuiInputTextFlags.CallbackCompletion
                | ImGuiInputTextFlags.CallbackHistory,
            new ImGuiInputTextCallback() {
                @Override public void accept(ImGuiInputTextCallbackData data) {
                    int flag = data.getEventFlag();
                    if (flag == ImGuiInputTextFlags.CallbackAlways) {
                        String buf = data.getBuf().substring(0, data.getBufTextLen());
                        if (!buf.equals(lastSuggestInput)) refreshSuggestions(buf);
                    } else if (flag == ImGuiInputTextFlags.CallbackCompletion && !suggestions.isEmpty()) {
                        CommandSuggestion top = suggestions.get(Math.min(suggestionIdx, suggestions.size() - 1));
                        String cur = data.getBuf().substring(0, data.getBufTextLen());
                        int sp = cur.lastIndexOf(' ');
                        String next = sp < 0 ? top.fill() + " " : cur.substring(0, sp + 1) + top.fill() + " ";
                        data.deleteChars(0, data.getBufTextLen());
                        data.insertChars(0, next);
                        refreshSuggestions(next);
                    } else if (flag == ImGuiInputTextFlags.CallbackHistory) {
                        if (data.getEventKey() == ImGuiKey.UpArrow) {
                            suggestionIdx = Math.max(0, suggestionIdx - 1); scrollSuggestToSelected = true;
                        } else if (data.getEventKey() == ImGuiKey.DownArrow) {
                            suggestionIdx = Math.min(suggestions.size() - 1, suggestionIdx + 1); scrollSuggestToSelected = true;
                        }
                    }
                }
            });
        ImGui.popStyleColor();

        if (submitted && !terminalInput.get().isBlank()) {
            dispatchTerminal(terminalInput.get());
            terminalInput.set("");
            lastSuggestInput = "";
            suggestions.clear();
            terminalScrollToBottom = true;
            ImGui.setKeyboardFocusHere(-1);
        }
    }

    private void refreshSuggestions(String input) {
        lastSuggestInput = input;
        suggestions.clear();
        suggestions.addAll(CommandRegistry.suggest(input));
        if (suggestionIdx >= suggestions.size()) suggestionIdx = 0;
        scrollSuggestToSelected = true;
    }

    private static void dispatchTerminal(String input) {
        TerminalLog.addInput("> " + input);
        CommandResult result = CommandRegistry.dispatch(input);
        for (String line : result.lines()) {
            if (result.success()) TerminalLog.addOutput(line);
            else TerminalLog.addError(line);
        }
    }

    private void renderMcLog(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0A0F14);

        final float padX    = 8.0f;
        final float clearW  = 44.0f;
        final float headerH = 30.0f;

        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + headerH, 0xEE131921);
        ImGui.setCursorScreenPos(viewport.x() + padX, viewport.y() + 6.0f);
        ImGui.textColored(0.60f, 0.72f, 0.90f, 1.0f, "Game Log");
        ImGui.setCursorScreenPos(viewport.right() - clearW - 6.0f, viewport.y() + 5.0f);
        if (ImGui.button("Clear##mclog", clearW, 20.0f)) AppLog.clear();

        float listY = viewport.y() + headerH + 4.0f;
        float listH = viewport.height() - headerH - 8.0f;
        ImGui.setCursorScreenPos(viewport.x() + padX, listY);
        if (ImGui.beginChild("##mclog-scroll", viewport.width() - padX * 2, listH, false)) {
            if (AppLog.consumeDirty()) logScrollToBottom = true;
            List<AppLog.LogEntry> entries = AppLog.snapshot();
            for (AppLog.LogEntry entry : entries) {
                float[] col = levelColor(entry.level());
                ImGui.textColored(0.35f, 0.42f, 0.52f, 1.0f, entry.time());
                ImGui.sameLine();
                ImGui.textColored(col[0], col[1], col[2], 1.0f, "[" + entry.level() + "]");
                ImGui.sameLine();
                ImGui.textColored(0.50f, 0.60f, 0.72f, 1.0f, entry.logger() + ":");
                ImGui.sameLine();
                ImGui.textColored(0.85f, 0.90f, 0.95f, 1.0f, entry.text());
            }
            if (entries.isEmpty()) ImGui.textDisabled("No log entries yet.");
            if (logScrollToBottom) { ImGui.setScrollHereY(1.0f); logScrollToBottom = false; }
            ImGui.endChild();
        }
    }

    private static float[] levelColor(String level) {
        return switch (level) {
            case "ERROR", "FATAL" -> new float[]{0.90f, 0.30f, 0.25f};
            case "WARN"           -> new float[]{0.95f, 0.72f, 0.20f};
            case "DEBUG", "TRACE" -> new float[]{0.42f, 0.52f, 0.62f};
            default               -> new float[]{0.55f, 0.85f, 0.55f};
        };
    }

    private static void drawViewportFrame(UiRect viewport) {
        ImDrawList dl = ImGui.getForegroundDrawList();
        dl.addRect(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        dl.addLine(viewport.x(), viewport.y(), viewport.right(), viewport.y(), 0xFFFFFFFF, 1.0f);
        dl.addLine(viewport.x(), viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }
}
