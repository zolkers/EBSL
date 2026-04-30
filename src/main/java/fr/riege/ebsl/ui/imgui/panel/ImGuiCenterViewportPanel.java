package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.gui.GuiSettingsGroup;
import fr.riege.ebsl.event.events.render.RenderGameViewportEvent;
import fr.riege.ebsl.mc.McChatLog;
import fr.riege.ebsl.mc.McChatLog.McLogEntry;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.packet.PacketCaptureEvent;
import fr.riege.ebsl.packet.PacketCaptureLog;
import fr.riege.ebsl.terminal.CommandRegistry;
import fr.riege.ebsl.terminal.CommandSuggestion;
import fr.riege.ebsl.terminal.TerminalLog;
import fr.riege.ebsl.terminal.TerminalLog.LogEntry;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.DoubleSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.CenterTab;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.ImDrawList;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ImGuiCenterViewportPanel implements ImGuiUiPanel {
    private static final DateTimeFormatter PACKET_TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final ImString packetFilter = new ImString(64);
    private final ImString terminalInput = new ImString(256);
    private boolean terminalScrollToBottom = false;
    private boolean mcLogScrollToBottom = false;
    private final List<CommandSuggestion> suggestions = new ArrayList<>();
    private int suggestionIdx = 0;
    private String lastSuggestInput = null;
    private boolean shouldApplyCompletion = false;
    private boolean scrollSuggestToSelected = false;

    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
        ImGuiPanelUtil.nextFixedWindow(layout.center());
        int flags = ImGuiPanelUtil.FIXED_PANEL_FLAGS | ImGuiWindowFlags.NoBackground;
        if (ImGui.begin("##ebsl-center-viewport", flags)) {
            UiRect tabs = tabsRect(layout);
            UiRect viewport = viewportRect(layout);
            drawTabs(state, tabs);
            if (state.centerTab() == CenterTab.GAME) {
                drawGameViewportShell(viewport);
            } else if (state.centerTab() == CenterTab.PATHFINDER_SETTINGS) {
                renderPathfinderSettings(viewport);
            } else if (state.centerTab() == CenterTab.TERMINAL) {
                renderTerminal(viewport);
            } else if (state.centerTab() == CenterTab.MC_LOG) {
                renderMcLog(viewport);
            } else {
                renderPacketView(viewport);
            }
            drawViewportFrame(viewport);
            ImGui.end();
        }
    }

    private UiRect tabsRect(ViewportLayout layout) {
        UiRect center = layout.center();
        return new UiRect(center.x(), center.y(), center.width(), UiTheme.TAB_H + 8);
    }

    private UiRect viewportRect(ViewportLayout layout) {
        UiRect center = layout.center();
        int top = center.y() + UiTheme.TAB_H + 8;
        return new UiRect(center.x() + 8, top, center.width() - 16, center.bottom() - top - 8);
    }

    private void drawTabs(EbslUiState state, UiRect tabs) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(tabs.x(), tabs.y(), tabs.right(), tabs.bottom(), 0xEE10141A);
        ImGui.setCursorScreenPos(tabs.x() + 8.0f, tabs.y() + 5.0f);
        for (CenterTab tab : CenterTab.values()) {
            float width = switch (tab) {
                case GAME -> 72.0f;
                case PACKET -> 86.0f;
                case PATHFINDER_SETTINGS -> 148.0f;
                case TERMINAL -> 86.0f;
                case MC_LOG -> 72.0f;
            };
            if (ImGui.button(tab.label(), width, 22.0f)) {
                state.setCenterTab(tab);
            }
            ImGui.sameLine();
        }
    }

    private void renderPathfinderSettings(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);
        drawList.addLine(viewport.x(), viewport.y() + 34.0f, viewport.right(), viewport.y() + 34.0f, UiTheme.BORDER, 1.0f);
        drawList.addLine(viewport.x() + 1.0f, viewport.y(), viewport.x() + 1.0f, viewport.bottom(), 0x66364755, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginChild("##pathfinder-settings-scroll", viewport.width() - 28.0f, viewport.height() - 28.0f, false);
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) {
            EbslApi.settings().pathfinding().resetToDefaultsAndSave();
        }
        ImGui.spacing();
        for (GuiSettingsGroup group : EbslApi.gui().pathfinderSettingsGroups()) {
            renderSettingsCategory(group.label(), group.settings());
        }
        ImGui.endChild();
    }

    private void renderSettingsCategory(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) {
            return;
        }
        ImGui.indent(10.0f);
        for (Setting<?> setting : settings) {
            renderPathfinderSetting(setting);
        }
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    private void renderPathfinderSetting(Setting<?> setting) {
        if (setting instanceof BooleanSetting boolSetting) {
            ImBoolean value = new ImBoolean(boolSetting.value());
            if (ImGui.checkbox(setting.displayName(), value)) {
                boolSetting.setValue(value.get());
                savePathfinderSettings();
            }
        } else if (setting instanceof IntSetting intSetting) {
            int[] value = {intSetting.value()};
            if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                intSetting.setValue(value[0]);
                savePathfinderSettings();
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            float[] value = {(float) doubleSetting.value().doubleValue()};
            if (ImGui.sliderFloat(setting.displayName(), value, (float) doubleSetting.min(), (float) doubleSetting.max(), "%.2f")) {
                doubleSetting.setValue((double) value[0]);
                savePathfinderSettings();
            }
        }
    }

    private void savePathfinderSettings() {
        EbslApi.settings().pathfinding().save();
    }

    private void renderPacketView(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE101820);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginGroup();
        ImGui.text("Packet");

        ImBoolean enabled = new ImBoolean(PacketCaptureLog.isEnabled());
        if (ImGui.checkbox("Capture", enabled)) {
            PacketCaptureLog.setEnabled(enabled.get());
        }
        ImGui.sameLine();
        ImBoolean captureInbound = new ImBoolean(PacketCaptureLog.isCaptureInbound());
        if (ImGui.checkbox("S2C", captureInbound)) {
            PacketCaptureLog.setCaptureInbound(captureInbound.get());
        }
        ImGui.sameLine();
        ImBoolean captureOutbound = new ImBoolean(PacketCaptureLog.isCaptureOutbound());
        if (ImGui.checkbox("C2S", captureOutbound)) {
            PacketCaptureLog.setCaptureOutbound(captureOutbound.get());
        }
        ImGui.sameLine();
        if (ImGui.button("Clear", 72.0f, 22.0f)) {
            PacketCaptureLog.clear();
        }
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
        float listHeight = Math.max(80.0f, viewport.height() - 112.0f);
        if (ImGui.beginChild("##packet-log", viewport.width() - 28.0f, listHeight, true)) {
            int rendered = 0;
            for (int i = events.size() - 1; i >= 0 && rendered < 350; i--) {
                PacketCaptureEvent event = events.get(i);
                if (!matchesPacketFilter(event, filter)) {
                    continue;
                }
                ImGui.textDisabled(formatPacketEvent(event));
                rendered++;
            }
            if (rendered == 0) {
                ImGui.textDisabled("No packets captured yet.");
            }
            ImGui.endChild();
        }
    }

    private static boolean matchesPacketFilter(PacketCaptureEvent event, String filter) {
        if (filter.isBlank()) {
            return true;
        }
        return event.packetId().toLowerCase().contains(filter)
            || event.packetClass().toLowerCase().contains(filter)
            || event.direction().label().toLowerCase().contains(filter);
    }

    private static String formatPacketEvent(PacketCaptureEvent event) {
        String flags = (event.terminal() ? " terminal" : "") + (event.skippable() ? " skippable" : "");
        return "#" + event.sequence()
            + " " + PACKET_TIME_FORMAT.format(Instant.ofEpochMilli(event.capturedAtMs()))
            + " " + event.direction().label()
            + " " + event.packetId()
            + " [" + event.packetClass() + "]"
            + flags;
    }

    private void renderTerminal(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0D1117);

        final float inputH = 28.0f;
        final float suggestRowH = 20.0f;
        final int maxSuggestVisible = 6;
        final float boxX = viewport.x() + 8.0f;
        final float boxRight = viewport.right() - 8.0f;
        final float boxW = boxRight - boxX;

        // fallback refresh when input field is not focused (e.g. after fillCompletion click)
        String currentInput = terminalInput.get();
        if (!currentInput.equals(lastSuggestInput)) {
            refreshSuggestions(currentInput);
        }

        int suggestCount = suggestions.size();
        float suggestBoxH = suggestCount > 0
            ? Math.min(suggestCount, maxSuggestVisible) * suggestRowH + 8.0f
            : 0.0f;
        float logH = viewport.height() - inputH - 16.0f
            - (suggestCount > 0 ? suggestBoxH + 4.0f : 0.0f);

        // --- log ---
        ImGui.setCursorScreenPos(boxX, viewport.y() + 8.0f);
        if (ImGui.beginChild("##terminal-log", boxW, logH, false)) {
            if (TerminalLog.consumeDirty()) terminalScrollToBottom = true;
            for (LogEntry entry : TerminalLog.snapshot()) {
                switch (entry.type()) {
                    case INPUT  -> ImGui.textColored(0.45f, 0.55f, 0.65f, 1.0f, entry.text());
                    case OUTPUT -> ImGui.textColored(0.87f, 0.93f, 0.97f, 1.0f, entry.text());
                    case ERROR  -> ImGui.textColored(0.90f, 0.35f, 0.30f, 1.0f, entry.text());
                }
            }
            if (terminalScrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                terminalScrollToBottom = false;
            }
            ImGui.endChild();
        }

        float suggestY = viewport.y() + 8.0f + logH + 4.0f;
        float inputY  = suggestY + suggestBoxH + (suggestCount > 0 ? 4.0f : 0.0f);

        // --- suggestion list ---
        if (suggestCount > 0) {
            drawList.addRectFilled(boxX, suggestY, boxRight, suggestY + suggestBoxH, 0xF01A2230);
            drawList.addRect(boxX, suggestY, boxRight, suggestY + suggestBoxH, 0xFF2D4A6A, 0, 0, 1.0f);

            ImGui.setCursorScreenPos(boxX + 1, suggestY + 4.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg,          0x00000000);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarBg,      0x00000000);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrab,    0xFF334455);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabHovered, 0xFF4466AA);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabActive,  0xFF5588CC);
            if (ImGui.beginChild("##terminal-suggest", boxW - 2, suggestBoxH - 8.0f)) {
                float contentX = ImGui.getCursorScreenPosX();
                float rowW     = ImGui.getContentRegionAvailX();
                for (int i = 0; i < suggestCount; i++) {
                    CommandSuggestion sug = suggestions.get(i);
                    boolean selected = i == suggestionIdx;
                    float rowY = ImGui.getCursorScreenPosY();

                    if (selected && scrollSuggestToSelected) {
                        ImGui.setScrollHereY(0.5f);
                    }
                    if (selected) {
                        ImGui.getWindowDrawList().addRectFilled(
                            contentX - 2, rowY, contentX + rowW + 2, rowY + suggestRowH, 0xFF1E3A55);
                    }
                    if (ImGui.invisibleButton("##sug" + i, rowW, suggestRowH)) {
                        suggestionIdx = i;
                        fillCompletion(sug.fill());
                    }
                    if (ImGui.isItemHovered()) {
                        suggestionIdx = i;
                    }
                    ImGui.setCursorScreenPos(contentX + 6.0f, rowY + 2.0f);
                    if (selected) {
                        ImGui.textColored(1.0f, 0.85f, 0.40f, 1.0f, sug.fill());
                    } else {
                        ImGui.textColored(0.70f, 0.82f, 0.95f, 1.0f, sug.fill());
                    }
                    if (!sug.hint().isEmpty()) {
                        ImGui.sameLine();
                        ImGui.textColored(0.38f, 0.45f, 0.54f, 1.0f, sug.hint());
                    }
                    ImGui.setCursorScreenPos(contentX, rowY + suggestRowH);
                }
                scrollSuggestToSelected = false;
                ImGui.endChild();
            }
            ImGui.popStyleColor(5);
        }

        // --- input bar ---
        drawList.addRectFilled(boxX, inputY, boxRight, inputY + inputH, 0xFF131921);

        final float clearW   = 44.0f;
        final float clearPad = 6.0f;
        final float promptW  = 18.0f;

        ImGui.setCursorScreenPos(boxX + 4.0f, inputY + 6.0f);
        ImGui.textColored(0.45f, 0.75f, 0.45f, 1.0f, ">");

        ImGui.setCursorScreenPos(boxX + promptW, inputY + 5.0f);
        ImGui.setNextItemWidth(boxW - promptW - clearW - clearPad * 2);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.FrameBg, 0x00000000);
        boolean submitted = ImGui.inputText("##terminal-input", terminalInput,
            ImGuiInputTextFlags.EnterReturnsTrue
                | ImGuiInputTextFlags.CallbackAlways
                | ImGuiInputTextFlags.CallbackCompletion
                | ImGuiInputTextFlags.CallbackHistory,
            new ImGuiInputTextCallback() {
                @Override
                public void accept(ImGuiInputTextCallbackData data) {
                    int flag = data.getEventFlag();
                    if (flag == ImGuiInputTextFlags.CallbackAlways) {
                        // live buffer — update suggestions on every keystroke
                        String buf = data.getBuf().substring(0, data.getBufTextLen());
                        if (!buf.equals(lastSuggestInput)) {
                            refreshSuggestions(buf);
                        }
                    } else if (flag == ImGuiInputTextFlags.CallbackCompletion && !suggestions.isEmpty()) {
                        shouldApplyCompletion = true;
                    } else if (flag == ImGuiInputTextFlags.CallbackHistory) {
                        if (data.getEventKey() == ImGuiKey.UpArrow) {
                            suggestionIdx = Math.max(0, suggestionIdx - 1);
                            scrollSuggestToSelected = true;
                        } else if (data.getEventKey() == ImGuiKey.DownArrow) {
                            suggestionIdx = Math.min(suggestions.size() - 1, suggestionIdx + 1);
                            scrollSuggestToSelected = true;
                        }
                    }
                }
            });
        ImGui.popStyleColor();

        ImGui.setCursorScreenPos(boxRight - clearW - clearPad, inputY + 4.0f);
        if (ImGui.button("Clear", clearW, 20.0f)) {
            TerminalLog.clear();
        }

        if (shouldApplyCompletion && !suggestions.isEmpty()) {
            fillCompletion(suggestions.get(suggestionIdx).fill());
            shouldApplyCompletion = false;
        }
        if (submitted && !terminalInput.get().isBlank()) {
            CommandRegistry.dispatch(terminalInput.get(), net.minecraft.client.Minecraft.getInstance());
            terminalInput.set("");
            lastSuggestInput = null;
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

    private void fillCompletion(String suggestion) {
        String current = terminalInput.get();
        int spaceIdx = current.lastIndexOf(' ');
        String next = spaceIdx < 0 ? suggestion + " " : current.substring(0, spaceIdx + 1) + suggestion + " ";
        terminalInput.set(next);
        // force refresh on next frame (CallbackAlways will catch it once focused again)
        lastSuggestInput = null;
    }

    private void renderMcLog(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0A0F14);

        final float padX = 8.0f;
        final float clearW = 44.0f;
        final float clearPad = 6.0f;
        final float headerH = 30.0f;

        // header bar
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + headerH, 0xEE131921);
        ImGui.setCursorScreenPos(viewport.x() + padX, viewport.y() + 6.0f);
        ImGui.textColored(0.60f, 0.72f, 0.90f, 1.0f, "Game Log");
        ImGui.setCursorScreenPos(viewport.right() - clearW - clearPad, viewport.y() + 5.0f);
        if (ImGui.button("Clear##mclog", clearW, 20.0f)) {
            McChatLog.clear();
        }

        // log list
        float listY = viewport.y() + headerH + 4.0f;
        float listH = viewport.height() - headerH - 8.0f;
        ImGui.setCursorScreenPos(viewport.x() + padX, listY);
        if (ImGui.beginChild("##mclog-scroll", viewport.width() - padX * 2, listH, false)) {
            if (McChatLog.consumeDirty()) mcLogScrollToBottom = true;
            List<McLogEntry> entries = McChatLog.snapshot();
            for (McLogEntry entry : entries) {
                float[] color = levelColor(entry.level());
                ImGui.textColored(0.35f, 0.42f, 0.52f, 1.0f, entry.time());
                ImGui.sameLine();
                ImGui.textColored(color[0], color[1], color[2], 1.0f, "[" + entry.level() + "]");
                ImGui.sameLine();
                ImGui.textColored(0.50f, 0.60f, 0.72f, 1.0f, entry.logger() + ":");
                ImGui.sameLine();
                ImGui.textColored(0.85f, 0.90f, 0.95f, 1.0f, entry.text());
            }
            if (entries.isEmpty()) {
                ImGui.textDisabled("No log entries yet.");
            }
            if (mcLogScrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                mcLogScrollToBottom = false;
            }
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

    private void drawGameViewportShell(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0x18000000);
        EbslMod.postClientEvent(new RenderGameViewportEvent(ImGui.getForegroundDrawList(), viewport));
    }

    private void drawViewportFrame(UiRect viewport) {
        ImDrawList drawList = ImGui.getForegroundDrawList();
        drawList.addRect(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        drawList.addLine(viewport.x(), viewport.y(), viewport.right(), viewport.y(), 0xFFFFFFFF, 1.0f);
        drawList.addLine(viewport.x(), viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }
}
