package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.terminal.CommandRegistry;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import fr.riege.ebsl.common.feature.terminal.CommandSuggestion;
import fr.riege.ebsl.common.feature.terminal.TerminalLog;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S107")
final class ImGuiTerminalPanel {
    private final ImString terminalInput = new ImString(256);
    private final List<CommandSuggestion> suggestions = new ArrayList<>();
    private int suggestionIdx;
    private String lastSuggestInput;
    private boolean scrollSuggestToSelected;
    private boolean scrollToBottom;
    private boolean focused;

    void resetFocus() {
        focused = false;
    }

    void render(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0D1117);

        final float inputH = 28.0f;
        final float sugRowH = 20.0f;
        final int maxVisible = 6;
        final float boxX = viewport.x() + 8.0f;
        final float boxRight = viewport.right() - 8.0f;
        final float boxW = boxRight - boxX;

        if (lastSuggestInput == null) refreshSuggestions("");
        List<CommandSuggestion> snap = List.copyOf(suggestions);
        int sugCount = snap.size();
        float sugBoxH = sugCount > 0 ? Math.min(sugCount, maxVisible) * sugRowH + 8.0f : 0.0f;
        float logH = viewport.height() - inputH - 16.0f - (sugCount > 0 ? sugBoxH + 4.0f : 0.0f);

        renderLog(boxX, viewport.y() + 8.0f, boxW, logH);

        float sugY = viewport.y() + 8.0f + logH + 4.0f;
        float inputY = sugY + sugBoxH + (sugCount > 0 ? 4.0f : 0.0f);
        if (sugCount > 0) {
            renderSuggestions(dl, snap, sugCount, sugRowH, boxX, boxRight, boxW, sugY, sugBoxH);
        }
        renderInput(dl, boxX, boxRight, boxW, inputY, inputH);
    }

    private void renderLog(float x, float y, float width, float height) {
        ImGui.setCursorScreenPos(x, y);
        if (ImGui.beginChild("##terminal-log", width, height, false)) {
            if (TerminalLog.consumeDirty()) scrollToBottom = true;
            for (TerminalLog.LogEntry entry : TerminalLog.snapshot()) {
                switch (entry.type()) {
                    case INPUT -> ImGui.textColored(0.45f, 0.55f, 0.65f, 1.0f, entry.text());
                    case OUTPUT -> ImGui.textColored(0.87f, 0.93f, 0.97f, 1.0f, entry.text());
                    case ERROR -> ImGui.textColored(0.90f, 0.35f, 0.30f, 1.0f, entry.text());
                }
            }
            if (scrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                scrollToBottom = false;
            }
            ImGui.endChild();
        }
    }

    private void renderSuggestions(ImDrawList dl, List<CommandSuggestion> snap, int count, float rowH,
                                   float boxX, float boxRight, float boxW, float y, float height) {
        dl.addRectFilled(boxX, y, boxRight, y + height, 0xF01A2230);
        dl.addRect(boxX, y, boxRight, y + height, 0xFF2D4A6A, 0, 0, 1.0f);
        ImGui.setCursorScreenPos(boxX + 1, y + 4.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg, 0x00000000);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrab, 0xFF334455);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabHovered, 0xFF4466AA);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ScrollbarGrabActive, 0xFF5588CC);
        if (ImGui.beginChild("##terminal-suggest", boxW - 2, height - 8.0f)) {
            float cx = ImGui.getCursorScreenPosX();
            float rw = ImGui.getContentRegionAvailX();
            for (int i = 0; i < count; i++) {
                renderSuggestionRow(snap.get(i), i == suggestionIdx, rowH, cx, rw);
            }
            scrollSuggestToSelected = false;
            ImGui.endChild();
        }
        ImGui.popStyleColor(4);
    }

    private void renderSuggestionRow(CommandSuggestion suggestion, boolean selected, float rowH, float x, float width) {
        float y = ImGui.getCursorScreenPosY();
        if (selected && scrollSuggestToSelected) ImGui.setScrollHereY(0.5f);
        if (selected) ImGui.getWindowDrawList().addRectFilled(x - 2, y, x + width + 2, y + rowH, 0xFF1E3A55);
        ImGui.setCursorScreenPos(x + 6.0f, y + 2.0f);
        if (selected) ImGui.textColored(1.0f, 0.85f, 0.40f, 1.0f, suggestion.fill());
        else ImGui.textColored(0.70f, 0.82f, 0.95f, 1.0f, suggestion.fill());
        if (!suggestion.hint().isEmpty()) {
            ImGui.sameLine();
            ImGui.textColored(0.38f, 0.45f, 0.54f, 1.0f, suggestion.hint());
        }
        ImGui.setCursorScreenPos(x, y + rowH);
    }

    private void renderInput(ImDrawList dl, float boxX, float boxRight, float boxW, float y, float height) {
        dl.addRectFilled(boxX, y, boxRight, y + height, 0xFF131921);
        ImGui.setCursorScreenPos(boxX + 4.0f, y + 6.0f);
        ImGui.textColored(0.45f, 0.75f, 0.45f, 1.0f, ">");

        if (!focused) {
            ImGui.setKeyboardFocusHere(0);
            focused = true;
        }
        ImGui.setCursorScreenPos(boxX + 18.0f, y + 5.0f);
        ImGui.setNextItemWidth(boxW - 18.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.FrameBg, 0x00000000);
        boolean submitted = ImGui.inputText("##terminal-input", terminalInput,
            ImGuiInputTextFlags.EnterReturnsTrue
                | ImGuiInputTextFlags.CallbackAlways
                | ImGuiInputTextFlags.CallbackCompletion
                | ImGuiInputTextFlags.CallbackHistory,
            new TerminalInputCallback());
        ImGui.popStyleColor();

        if (submitted && !terminalInput.get().isBlank()) {
            dispatchTerminal(terminalInput.get());
            terminalInput.set("");
            lastSuggestInput = "";
            suggestions.clear();
            scrollToBottom = true;
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

    private final class TerminalInputCallback extends ImGuiInputTextCallback {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            int flag = data.getEventFlag();
            if (flag == ImGuiInputTextFlags.CallbackAlways) {
                String buf = data.getBuf().substring(0, data.getBufTextLen());
                if (!buf.equals(lastSuggestInput)) refreshSuggestions(buf);
            } else if (flag == ImGuiInputTextFlags.CallbackCompletion && !suggestions.isEmpty()) {
                completeSuggestion(data);
            } else if (flag == ImGuiInputTextFlags.CallbackHistory && !suggestions.isEmpty()) {
                navigateSuggestions(data);
            }
        }

        private void completeSuggestion(ImGuiInputTextCallbackData data) {
            CommandSuggestion top = suggestions.get(Math.clamp(suggestionIdx, 0, suggestions.size() - 1));
            String cur = data.getBuf().substring(0, data.getBufTextLen());
            int sp = cur.lastIndexOf(' ');
            String next = sp < 0 ? top.fill() + " " : cur.substring(0, sp + 1) + top.fill() + " ";
            data.deleteChars(0, data.getBufTextLen());
            data.insertChars(0, next);
            refreshSuggestions(next);
        }

        private void navigateSuggestions(ImGuiInputTextCallbackData data) {
            if (data.getEventKey() == ImGuiKey.UpArrow) {
                suggestionIdx = (int) Math.clamp(suggestionIdx - 1L, 0L, suggestions.size() - 1L);
                scrollSuggestToSelected = true;
            } else if (data.getEventKey() == ImGuiKey.DownArrow) {
                suggestionIdx = (int) Math.clamp(suggestionIdx + 1L, 0L, suggestions.size() - 1L);
                scrollSuggestToSelected = true;
            }
        }
    }
}
