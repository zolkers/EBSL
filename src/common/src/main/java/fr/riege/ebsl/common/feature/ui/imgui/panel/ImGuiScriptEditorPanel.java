package fr.riege.ebsl.common.feature.ui.imgui.panel;

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

public final class ImGuiScriptEditorPanel {
    private static final int BUFFER_SIZE = 65536;
    private final ImString source = new ImString(EbslScriptManager.DEFAULT_SOURCE, BUFFER_SIZE);
    private String loadedFile = "";
    private String status = "idle";

    public void render(EbslUiState state, UiRect viewport, EbslPlatform platform) {
        ensureLoaded(state, platform);
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
        ImGui.setCursorScreenPos(editor.x(), editor.y());
        ImGui.inputTextMultiline("##ebsl-code-editor", source, editor.width(), editor.height());
    }

    private void renderGraph(UiRect editor) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(editor.x(), editor.y(), editor.right(), editor.bottom(), 0xFF0D1117);
        drawGrid(dl, editor);
        List<String> nodes = scriptNodes();
        float x = editor.x() + 28.0f;
        float y = editor.y() + 28.0f;
        float previousCenterX = 0.0f;
        float previousCenterY = 0.0f;
        for (int i = 0; i < nodes.size(); i++) {
            float nodeX = x + (i % 3) * 190.0f;
            float nodeY = y + (i / 3) * 92.0f;
            drawNode(dl, nodeX, nodeY, nodes.get(i));
            float centerX = nodeX + 78.0f;
            float centerY = nodeY + 26.0f;
            if (i > 0) {
                dl.addLine(previousCenterX, previousCenterY, centerX, centerY, 0xAA67B7FF, 2.0f);
            }
            previousCenterX = centerX;
            previousCenterY = centerY;
        }
        if (nodes.isEmpty()) {
            dl.addText(editor.x() + 20.0f, editor.y() + 20.0f, UiTheme.TEXT_MUTED, "Empty script");
        }
    }

    private void drawGrid(ImDrawList dl, UiRect editor) {
        for (int x = editor.x(); x < editor.right(); x += 32) {
            dl.addLine(x, editor.y(), x, editor.bottom(), 0x22000000, 1.0f);
        }
        for (int y = editor.y(); y < editor.bottom(); y += 32) {
            dl.addLine(editor.x(), y, editor.right(), y, 0x22000000, 1.0f);
        }
    }

    private void drawNode(ImDrawList dl, float x, float y, String label) {
        float width = Math.max(156.0f, Math.min(260.0f, label.length() * 7.0f + 24.0f));
        dl.addRectFilled(x, y, x + width, y + 52.0f, 0xFF18212C, 6.0f);
        dl.addRect(x, y, x + width, y + 52.0f, 0xFF67B7FF, 6.0f, 0, 1.5f);
        dl.addCircleFilled(x + 10.0f, y + 26.0f, 4.0f, 0xFF67B7FF);
        dl.addCircleFilled(x + width - 10.0f, y + 26.0f, 4.0f, 0xFF67B7FF);
        dl.addText(x + 20.0f, y + 18.0f, UiTheme.TEXT, label);
    }

    private List<String> scriptNodes() {
        List<String> nodes = new ArrayList<>();
        for (String line : source.get().split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.equals("{") || trimmed.equals("}")) {
                continue;
            }
            nodes.add(trimmed);
        }
        return nodes;
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
}
