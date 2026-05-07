package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptView;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ImGuiScriptEditorPanel {
    private static final int BUFFER_SIZE = 65536;
    private static final float NODE_H = 56.0f;

    private final ImString source = new ImString(EbslScriptManager.DEFAULT_SOURCE, BUFFER_SIZE);
    private final ImString completionFilter = new ImString("", 64);
    private String loadedFile = "";
    private String status = "idle";
    private int selectedGraphNode = -1;
    private float graphPanX = 0.0f;
    private float graphPanY = 0.0f;
    private float graphZoom = 1.0f;

    public void render(EbslUiState state, UiRect viewport, EbslPlatform platform) {
        ensureLoaded(state, platform);
        consumeRequestedInsert(state);

        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);

        ImGui.setCursorScreenPos(viewport.x() + 10.0f, viewport.y() + 7.0f);
        renderToolbar(state, platform);

        UiRect editor = new UiRect(viewport.x() + 10, viewport.y() + 44, viewport.width() - 20, viewport.height() - 54);
        if (state.scriptView() == EbslScriptView.GRAPH) {
            renderGraph(editor);
        } else {
            renderCode(editor);
        }
    }

    private void renderToolbar(EbslUiState state, EbslPlatform platform) {
        ImGui.text(state.selectedScriptFile());
        ImGui.sameLine(190.0f);
        for (EbslScriptView view : EbslScriptView.values()) {
            if (ImGui.button(view.label(), 64.0f, 22.0f)) {
                state.setScriptView(view);
            }
            ImGui.sameLine();
        }
        if (ImGui.button("Save", 64.0f, 22.0f)) {
            new EbslScriptManager(platform.storage()).save(state.selectedScriptFile(), source.get());
            status = "saved";
        }
        ImGui.sameLine();
        if (ImGui.button("Reload", 70.0f, 22.0f)) {
            load(state, platform);
        }
        ImGui.sameLine();
        if (ImGui.button("Validate", 78.0f, 22.0f)) {
            validate();
        }
        ImGui.sameLine();
        ImGui.textDisabled(status);
    }

    private void renderCode(UiRect editor) {
        UiRect completion = new UiRect(editor.x(), editor.y(), editor.width(), 84);
        renderCompletion(completion);
        UiRect code = new UiRect(editor.x(), editor.y() + 92, editor.width(), editor.height() - 92);
        ImGui.setCursorScreenPos(code.x(), code.y());
        ImGui.inputTextMultiline("##ebsl-code-editor", source, code.width(), code.height());
    }

    private void renderCompletion(UiRect rect) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(rect.x(), rect.y(), rect.right(), rect.bottom(), 0xFF101820, 4.0f);
        ImGui.setCursorScreenPos(rect.x() + 10.0f, rect.y() + 8.0f);
        ImGui.text("Autocomplete");
        ImGui.sameLine(128.0f);
        ImGui.setNextItemWidth(Math.max(180.0f, rect.width() - 150.0f));
        ImGui.inputText("##ebsl-completion-filter", completionFilter);

        ImGui.setCursorScreenPos(rect.x() + 10.0f, rect.y() + 38.0f);
        int shown = 0;
        for (EbslNodeType type : completions()) {
            if (ImGui.button(type.id(), Math.min(142.0f, Math.max(92.0f, type.id().length() * 7.0f + 16.0f)), 24.0f)) {
                insertNode(type.id());
                completionFilter.set("");
            }
            ImGui.sameLine();
            shown++;
            if (shown >= 5) {
                break;
            }
        }
    }

    private void renderGraph(UiRect editor) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(editor.x(), editor.y(), editor.right(), editor.bottom(), 0xFF0D1117);
        renderGraphToolbar(editor);
        UiRect canvas = new UiRect(editor.x(), editor.y() + 34, editor.width(), editor.height() - 34);
        drawGrid(dl, canvas);
        List<ScriptGraphNode> nodes = graphNodes();
        float previousCenterX = 0.0f;
        float previousCenterY = 0.0f;
        for (int i = 0; i < nodes.size(); i++) {
            float nodeX = canvas.x() + 28.0f + graphPanX + (i % 3) * 190.0f * graphZoom;
            float nodeY = canvas.y() + 28.0f + graphPanY + (i / 3) * 94.0f * graphZoom;
            float width = drawNode(dl, i, nodeX, nodeY, nodes.get(i));
            float centerX = nodeX + width * 0.5f;
            float centerY = nodeY + NODE_H * graphZoom * 0.5f;
            if (i > 0) {
                dl.addLine(previousCenterX, previousCenterY, centerX, centerY, 0xAA67B7FF, 2.0f);
            }
            previousCenterX = centerX;
            previousCenterY = centerY;
        }
        if (nodes.isEmpty()) {
            dl.addText(canvas.x() + 20.0f, canvas.y() + 20.0f, UiTheme.TEXT_MUTED, "Empty script");
        }
        renderSelectedNodeDetails(editor, nodes);
    }

    private void renderGraphToolbar(UiRect editor) {
        ImGui.setCursorScreenPos(editor.x() + 10.0f, editor.y() + 6.0f);
        if (ImGui.button("-", 24.0f, 22.0f)) {
            graphZoom = Math.max(0.65f, graphZoom - 0.1f);
        }
        ImGui.sameLine();
        if (ImGui.button("+", 24.0f, 22.0f)) {
            graphZoom = Math.min(1.6f, graphZoom + 0.1f);
        }
        ImGui.sameLine();
        if (ImGui.button("Reset", 58.0f, 22.0f)) {
            graphPanX = 0.0f;
            graphPanY = 0.0f;
            graphZoom = 1.0f;
        }
        ImGui.sameLine();
        if (ImGui.button("Left", 48.0f, 22.0f)) graphPanX -= 36.0f;
        ImGui.sameLine();
        if (ImGui.button("Right", 52.0f, 22.0f)) graphPanX += 36.0f;
        ImGui.sameLine();
        if (ImGui.button("Up", 40.0f, 22.0f)) graphPanY -= 36.0f;
        ImGui.sameLine();
        if (ImGui.button("Down", 54.0f, 22.0f)) graphPanY += 36.0f;
    }

    private void renderSelectedNodeDetails(UiRect editor, List<ScriptGraphNode> nodes) {
        if (selectedGraphNode < 0 || selectedGraphNode >= nodes.size()) {
            return;
        }
        ScriptGraphNode node = nodes.get(selectedGraphNode);
        float panelW = 230.0f;
        float x = editor.right() - panelW - 12.0f;
        float y = editor.y() + 48.0f;
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(x, y, x + panelW, y + 104.0f, 0xEE121A22, 5.0f);
        dl.addRect(x, y, x + panelW, y + 104.0f, 0xFF67B7FF, 5.0f, 0, 1.0f);
        dl.addText(x + 10.0f, y + 10.0f, UiTheme.TEXT, node.command());
        dl.addText(x + 10.0f, y + 32.0f, UiTheme.TEXT_MUTED, node.category().id());
        dl.addText(x + 10.0f, y + 56.0f, UiTheme.TEXT_DIM, "line " + node.lineNumber());
        dl.addText(x + 10.0f, y + 76.0f, UiTheme.TEXT_DIM, node.line());
    }

    private void drawGrid(ImDrawList dl, UiRect canvas) {
        int step = Math.max(18, Math.round(32.0f * graphZoom));
        for (int x = canvas.x() + Math.round(graphPanX) % step; x < canvas.right(); x += step) {
            dl.addLine(x, canvas.y(), x, canvas.bottom(), 0x22000000, 1.0f);
        }
        for (int y = canvas.y() + Math.round(graphPanY) % step; y < canvas.bottom(); y += step) {
            dl.addLine(canvas.x(), y, canvas.right(), y, 0x22000000, 1.0f);
        }
    }

    private float drawNode(ImDrawList dl, int index, float x, float y, ScriptGraphNode node) {
        float width = Math.max(156.0f, Math.min(280.0f, node.line().length() * 7.0f + 24.0f)) * graphZoom;
        float height = NODE_H * graphZoom;
        int fill = selectedGraphNode == index ? 0xFF22364A : color(node.category());
        dl.addRectFilled(x, y, x + width, y + height, fill, 6.0f);
        dl.addRect(x, y, x + width, y + height, 0xFF67B7FF, 6.0f, 0, 1.5f);
        dl.addCircleFilled(x + 10.0f * graphZoom, y + height * 0.5f, 4.0f * graphZoom, 0xFF67B7FF);
        dl.addCircleFilled(x + width - 10.0f * graphZoom, y + height * 0.5f, 4.0f * graphZoom, 0xFF67B7FF);
        dl.addText(x + 20.0f * graphZoom, y + 12.0f * graphZoom, UiTheme.TEXT, node.command());
        dl.addText(x + 20.0f * graphZoom, y + 32.0f * graphZoom, UiTheme.TEXT_MUTED, node.args());

        ImGui.setCursorScreenPos(x, y);
        if (ImGui.invisibleButton("##ebsl-graph-node-" + index, width, height)) {
            selectedGraphNode = index;
        }
        return width;
    }

    private List<ScriptGraphNode> graphNodes() {
        List<ScriptGraphNode> nodes = new ArrayList<>();
        String[] lines = source.get().split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.equals("{") || trimmed.equals("}")) {
                continue;
            }
            String command = trimmed.split("\\s+", 2)[0].toLowerCase(Locale.ROOT).replace('-', '_');
            String args = trimmed.length() > command.length() ? trimmed.substring(Math.min(trimmed.length(), command.length())).trim() : "";
            EbslNodeCategory category = category(command);
            nodes.add(new ScriptGraphNode(i + 1, trimmed, command, args, category));
        }
        return nodes;
    }

    private List<EbslNodeType> completions() {
        String filter = completionFilter.get().trim().toLowerCase(Locale.ROOT).replace('-', '_');
        List<EbslNodeType> matches = new ArrayList<>();
        for (EbslNodeType type : EbslNodeType.values()) {
            if (!type.executable()) {
                continue;
            }
            if (filter.isBlank() || type.id().contains(filter)) {
                matches.add(type);
            }
        }
        return matches;
    }

    private void consumeRequestedInsert(EbslUiState state) {
        String insert = state.consumeScriptInsert();
        if (!insert.isBlank()) {
            insertNode(insert);
        }
    }

    private void insertNode(String command) {
        String current = source.get();
        String separator = current.isBlank() || current.endsWith("\n") ? "" : "\n";
        source.set(current + separator + command + "\n");
        status = "inserted " + command;
    }

    private void validate() {
        try {
            EbslScriptEngine.compile(source.get());
            status = "valid";
        } catch (RuntimeException exception) {
            status = "error: " + exception.getMessage();
        }
    }

    private void ensureLoaded(EbslUiState state, EbslPlatform platform) {
        if (!loadedFile.equals(state.selectedScriptFile())) {
            load(state, platform);
        }
    }

    private void load(EbslUiState state, EbslPlatform platform) {
        EbslScriptDocument document = new EbslScriptManager(platform.storage()).load(state.selectedScriptFile());
        loadedFile = document.fileName();
        source.set(document.source());
        status = "loaded";
    }

    private static EbslNodeCategory category(String command) {
        try {
            return EbslNodeType.byId(command).category();
        } catch (RuntimeException exception) {
            return EbslNodeCategory.UTILITY;
        }
    }

    private static int color(EbslNodeCategory category) {
        return switch (category) {
            case FLOW -> 0xFF203049;
            case CONTROL -> 0xFF39294C;
            case DATA -> 0xFF263A34;
            case WORLD -> 0xFF45351F;
            case PLAYER -> 0xFF2A4052;
            case INTERFACE -> 0xFF3B3446;
            case SENSOR -> 0xFF29433E;
            case UTILITY -> 0xFF303742;
            case PARAMETER -> 0xFF3E3333;
        };
    }

    private record ScriptGraphNode(int lineNumber, String line, String command, String args, EbslNodeCategory category) {
    }
}
