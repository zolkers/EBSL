package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDoc;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocEntry;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocGenerator;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocParameter;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocSection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNodePosition;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeTemplate;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptDocument;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.parser.EbslSyntax;
import fr.riege.ebsl.common.feature.scripting.parser.EbslTokenizer;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorStyle;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxHighlighter;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxThemeRegistry;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxToken;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptView;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptEngine;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslGraphAutoLayout;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslScriptGraphNode;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslScriptGraphParser;
import fr.riege.ebsl.common.feature.ui.imgui.EbslNodeCategoryColors;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRenderContext;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRendererRegistry;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ImGuiScriptEditorPanel {
    private static final int BUFFER_SIZE = 65536;
    private static final float NODE_H = 56.0f;
    private static final float GRAPH_INSPECTOR_MIN_WIDTH = 330.0f;
    private static final float GRAPH_INSPECTOR_MAX_WIDTH = 410.0f;
    private static final float GRAPH_INSPECTOR_GAP = 10.0f;

    private final ImString source = new ImString(EbslScriptManager.DEFAULT_SOURCE, BUFFER_SIZE);
    private final ImString selectedCommand = new ImString("", 96);
    private final ImString selectedArgs = new ImString("", 512);
    private final Map<String, ImString> settingTextValues = new HashMap<>();
    private String loadedFile = "";
    private int loadedRevision = -1;
    private String status = "idle";
    private int selectedGraphNode = -1;
    private String selectedGraphKey = "";
    private float graphPanX = 0.0f;
    private float graphPanY = 0.0f;
    private float graphZoom = 1.0f;
    private final Map<String, NodePosition> graphNodePositions = new HashMap<>();
    private String draggedNodeKey = "";
    private float lastMouseX;
    private float lastMouseY;
    private boolean docOpen;

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
            renderGraph(editor, platform);
        } else {
            renderCode(editor);
        }
        renderDocPopup(viewport);
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
        if (ImGui.button("Doc", 54.0f, 22.0f)) {
            docOpen = true;
        }
        ImGui.sameLine();
        ImGui.textDisabled(status);
    }

    private void renderDocPopup(UiRect viewport) {
        if (!docOpen) {
            return;
        }
        float width = Math.min(760.0f, viewport.width() - 80.0f);
        float height = Math.min(620.0f, viewport.height() - 80.0f);
        float x = viewport.x() + (viewport.width() - width) * 0.5f;
        float y = viewport.y() + (viewport.height() - height) * 0.5f;
        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoResize;
        if (ImGui.begin("EBSL Language Doc##ebsl-language-doc", flags)) {
            if (ImGui.button("Close", 72.0f, 22.0f)) {
                docOpen = false;
            }
            ImGui.sameLine();
            ImGui.textDisabled("Generated from language registries");
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-doc-scroll", width - 24.0f, height - 76.0f, false)) {
                renderLanguageDoc(EbslLanguageDocGenerator.generate());
                ImGui.endChild();
            }
            ImGui.end();
        }
    }

    private void renderLanguageDoc(EbslLanguageDoc doc) {
        for (EbslLanguageDocSection section : doc.sections()) {
            ImGui.text(section.title());
            ImGui.separator();
            for (EbslLanguageDocEntry entry : section.entries()) {
                renderDocEntry(entry);
            }
            ImGui.spacing();
        }
    }

    private void renderDocEntry(EbslLanguageDocEntry entry) {
        ImGui.textColored(0.49f, 0.83f, 0.99f, 1.0f, entry.id());
        if (!entry.title().isBlank() && !entry.title().equals(entry.id())) {
            ImGui.sameLine();
            ImGui.textDisabled(entry.title());
        }
        if (!entry.description().isBlank()) {
            ImGui.textWrapped(entry.description());
        }
        docLine("Usage", entry.usage());
        docLine("Sample", entry.sample());
        if (!entry.aliases().isEmpty()) {
            docLine("Aliases", String.join(", ", entry.aliases()));
        }
        for (EbslLanguageDocParameter parameter : entry.parameters()) {
            docLine(parameter.id(), parameter.label() + " = " + parameter.defaultValue());
        }
        ImGui.spacing();
    }

    private void docLine(String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        ImGui.textDisabled(label + ":");
        ImGui.sameLine();
        ImGui.textWrapped(value);
    }

    private void renderCode(UiRect editor) {
        renderCodeEditor(editor);
    }

    private void renderGraph(UiRect editor, EbslPlatform platform) {
        ImDrawList dl = ImGui.getWindowDrawList();
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        float inspectorWidth = editor.width() >= 780.0f
            ? Math.min(GRAPH_INSPECTOR_MAX_WIDTH, Math.max(GRAPH_INSPECTOR_MIN_WIDTH, editor.width() * 0.30f))
            : 0.0f;
        UiRect graph = inspectorWidth > 0.0f
            ? new UiRect(editor.x(), editor.y(), Math.round(editor.width() - inspectorWidth - GRAPH_INSPECTOR_GAP), editor.height())
            : editor;
        UiRect inspector = inspectorWidth > 0.0f
            ? new UiRect(Math.round(graph.right() + GRAPH_INSPECTOR_GAP), editor.y(), Math.round(inspectorWidth), editor.height())
            : null;

        dl.addRectFilled(graph.x(), graph.y(), graph.right(), graph.bottom(), 0xFF0D1117);
        renderGraphToolbar(graph, manager);
        UiRect canvas = new UiRect(graph.x(), graph.y() + 34, graph.width(), graph.height() - 34);
        panAndZoomCanvas(canvas);
        drawGrid(dl, canvas);
        List<EbslScriptGraphNode> nodes = graphNodes();
        ensureAutoLayout(nodes);
        float previousOutX = 0.0f;
        float previousOutY = 0.0f;
        for (int i = 0; i < nodes.size(); i++) {
            EbslScriptGraphNode node = nodes.get(i);
            NodePosition position = nodePosition(node, i);
            float nodeX = canvas.x() + graphPanX + position.x() * graphZoom;
            float nodeY = canvas.y() + graphPanY + position.y() * graphZoom;
            float width = drawNode(dl, manager, i, node.key(), nodeX, nodeY, node);
            float inX = nodeX + 10.0f * graphZoom;
            float inY = nodeY + NODE_H * graphZoom * 0.5f;
            float outX = nodeX + width - 10.0f * graphZoom;
            float outY = inY;
            if (i > 0) {
                drawEdge(dl, previousOutX, previousOutY, inX, inY, node.depth());
            }
            previousOutX = outX;
            previousOutY = outY;
        }
        if (nodes.isEmpty()) {
            dl.addText(canvas.x() + 20.0f, canvas.y() + 20.0f, UiTheme.TEXT_MUTED, "Empty script");
        }
        drawMiniMap(graph, nodes);
        if (inspector != null) {
            renderNodeInspector(manager, inspector, nodes);
        } else {
            renderFloatingNodeInspector(manager, graph, nodes);
        }
    }

    private void renderCodeEditor(UiRect code) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(code.x(), code.y(), code.right(), code.bottom(), 0xFF0D1117, 4.0f);
        dl.addRect(code.x(), code.y(), code.right(), code.bottom(), 0xFF26313D, 4.0f, 0, 1.0f);
        UiRect gutter = new UiRect(code.x(), code.y(), 52, code.height());
        dl.addRectFilled(gutter.x(), gutter.y(), gutter.right(), gutter.bottom(), 0xFF111923, 4.0f);
        drawLineNumbers(dl, gutter);
        UiRect textArea = new UiRect(code.x() + 58, code.y() + 6, Math.round(code.width() - 64.0f), Math.round(code.height() - 12.0f));
        drawSyntaxHighlight(dl, textArea);
        ImGui.setCursorScreenPos(textArea.x(), textArea.y());
        ImGui.pushStyleColor(ImGuiCol.Text, EbslCodeEditorStyle.DARK.editableTextColor());
        ImGui.pushStyleColor(ImGuiCol.FrameBg, EbslCodeEditorStyle.DARK.frameColor());
        ImGui.inputTextMultiline("##ebsl-code-editor", source, code.width() - 64.0f, code.height() - 12.0f);
        ImGui.popStyleColor(2);
        dl.addText(code.right() - 128.0f, code.y() + 8.0f, UiTheme.TEXT_DIM, lineCount() + " lines");
    }

    private void drawSyntaxHighlight(ImDrawList dl, UiRect textArea) {
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        List<List<EbslSyntaxToken>> lines = EbslSyntaxHighlighter.highlight(source.get());
        int visible = Math.min(lines.size(), Math.max(1, (int) ((textArea.height() - style.textPadding()) / style.lineHeight())));
        for (int lineIndex = 0; lineIndex < visible; lineIndex++) {
            float x = textArea.x() + style.textPadding();
            float y = textArea.y() + style.textPadding() + lineIndex * style.lineHeight();
            for (EbslSyntaxToken token : lines.get(lineIndex)) {
                int color = EbslSyntaxThemeRegistry.style(token.kind()).color();
                if (color != 0) {
                    dl.addText(x, y, color, token.text());
                }
                x += token.text().length() * style.characterWidth();
            }
        }
    }

    private void drawLineNumbers(ImDrawList dl, UiRect gutter) {
        int count = lineCount();
        int visible = Math.min(count, Math.max(1, (gutter.height() - 12) / 16));
        for (int i = 0; i < visible; i++) {
            dl.addText(gutter.x() + 12.0f, gutter.y() + 8.0f + i * 16.0f, UiTheme.TEXT_DIM, Integer.toString(i + 1));
        }
    }

    private int lineCount() {
        return source.get().isEmpty() ? 1 : source.get().split("\\R", -1).length;
    }

    private void drawEdge(ImDrawList dl, float fromX, float fromY, float toX, float toY, int depth) {
        float lane = Math.max(42.0f, Math.abs(toX - fromX) * 0.45f);
        int color = depth > 0 ? 0xAA67B7FF : 0xCC67B7FF;
        dl.addBezierCubic(fromX, fromY, fromX + lane, fromY, toX - lane, toY, toX, toY, color, 2.0f);
        dl.addCircleFilled(fromX, fromY, 3.5f, 0xFF67B7FF);
        dl.addCircleFilled(toX, toY, 3.5f, 0xFF67B7FF);
    }

    private void renderGraphToolbar(UiRect editor, EbslScriptManager manager) {
        ImGui.setCursorScreenPos(editor.x() + 10.0f, editor.y() + 6.0f);
        if (ImGui.button("-", 24.0f, 22.0f)) {
            graphZoom = Math.max(0.65f, graphZoom - 0.1f);
        }
        ImGui.sameLine();
        if (ImGui.button("+", 24.0f, 22.0f)) {
            graphZoom = Math.min(1.6f, graphZoom + 0.1f);
        }
        ImGui.sameLine();
        if (ImGui.button("Auto", 48.0f, 22.0f)) {
            autoLayoutGraph(manager);
        }
        ImGui.sameLine();
        if (ImGui.button("Reset view", 82.0f, 22.0f)) {
            graphPanX = 0.0f;
            graphPanY = 0.0f;
            graphZoom = 1.0f;
        }
        ImGui.sameLine();
        ImGui.textDisabled("auto layout | drag nodes | right-drag canvas | wheel zoom");
    }

    private void renderFloatingNodeInspector(EbslScriptManager manager, UiRect editor, List<EbslScriptGraphNode> nodes) {
        if (selectedGraphNode < 0 || selectedGraphNode >= nodes.size()) {
            return;
        }
        UiRect panel = new UiRect(editor.right() - 318, editor.y() + 48, 306, Math.round(Math.min(360.0f, editor.height() - 60.0f)));
        renderNodeInspector(manager, panel, nodes);
    }

    private void renderNodeInspector(EbslScriptManager manager, UiRect panel, List<EbslScriptGraphNode> nodes) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(panel.x(), panel.y(), panel.right(), panel.bottom(), 0xEE121A22, 5.0f);
        dl.addRect(panel.x(), panel.y(), panel.right(), panel.bottom(), 0xFF26313D, 5.0f, 0, 1.0f);

        float pad = 12.0f;
        if (selectedGraphNode < 0 || selectedGraphNode >= nodes.size()) {
            ImGui.setCursorScreenPos(panel.x() + pad, panel.y() + pad);
            ImGui.text("Node inspector");
            ImGui.setCursorScreenPos(panel.x() + pad, panel.y() + 36.0f);
            ImGui.textDisabled("Select a graph node to edit its settings.");
            return;
        }

        EbslScriptGraphNode node = nodes.get(selectedGraphNode);
        EbslNode ebslNode = EbslNodeRegistry.get(node.command());
        EbslNodeTemplate template = EbslNodeTemplate.of(node.command());
        syncSelectedEditor(node, ebslNode);
        boolean settingBacked = hasSettings(ebslNode);
        float contentWidth = panel.width() - pad * 2.0f;
        dl.addRect(panel.x(), panel.y(), panel.right(), panel.bottom(), 0xFF67B7FF, 5.0f, 0, 1.0f);

        ImGui.setCursorScreenPos(panel.x() + pad, panel.y() + 10.0f);
        ImGui.text(template.title());
        ImGui.setCursorScreenPos(panel.x() + pad, panel.y() + 32.0f);
        ImGui.textDisabled(template.description());

        float contentTop = panel.y() + 62.0f;
        float footerHeight = 74.0f;
        float contentHeight = Math.max(96.0f, panel.height() - (contentTop - panel.y()) - footerHeight);
        ImGui.setCursorScreenPos(panel.x() + pad, contentTop);
        if (settingBacked) {
            ImGui.beginChild("##ebsl-node-settings", contentWidth, contentHeight, true);
            renderNodeSettings(ebslNode, Math.max(120.0f, contentWidth - 20.0f));
            ImGui.endChild();
        } else {
            ImGui.setNextItemWidth(contentWidth);
            ImGui.inputText("##ebsl-node-command", selectedCommand);
            ImGui.setCursorScreenPos(panel.x() + pad, contentTop + 34.0f);
            ImGui.setNextItemWidth(contentWidth);
            ImGui.inputText("##ebsl-node-args", selectedArgs);
        }

        float actionY = panel.bottom() - 60.0f;
        ImGui.setCursorScreenPos(panel.x() + pad, actionY);
        if (ImGui.button("Apply", 64.0f, 24.0f)) {
            applyNodeEdit(manager, node, ebslNode);
        }
        ImGui.sameLine();
        if (ImGui.button("Duplicate", 82.0f, 24.0f)) {
            duplicateNodeLine(manager, node);
        }
        ImGui.sameLine();
        if (ImGui.button("Delete", 64.0f, 24.0f)) {
            deleteNodeLine(manager, node);
        }
        ImGui.setCursorScreenPos(panel.x() + pad, actionY + 32.0f);
        if (ImGui.button(settingBacked ? "Load sample" : "Use template args", 132.0f, 24.0f)) {
            if (settingBacked) {
                ebslNode.loadArgs(splitArgs(template.sampleArgs()));
                settingTextValues.clear();
            } else {
                selectedArgs.set(template.sampleArgs());
            }
        }
    }

    private void panAndZoomCanvas(UiRect canvas) {
        if (!ImGui.isMouseHoveringRect(canvas.x(), canvas.y(), canvas.right(), canvas.bottom(), true)) {
            return;
        }
        float wheel = ImGui.getIO().getMouseWheel();
        if (wheel != 0.0f) {
            graphZoom = Math.max(0.65f, Math.min(1.7f, graphZoom + wheel * 0.08f));
        }
        if (ImGui.isMouseDragging(1)) {
            graphPanX += ImGui.getIO().getMouseDeltaX();
            graphPanY += ImGui.getIO().getMouseDeltaY();
        }
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

    private void ensureAutoLayout(List<EbslScriptGraphNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        Map<String, EbslGraphNodePosition> auto = EbslGraphAutoLayout.layout(nodes);
        for (EbslScriptGraphNode node : nodes) {
            EbslGraphNodePosition position = auto.get(node.key());
            if (position != null) {
                graphNodePositions.putIfAbsent(node.key(), new NodePosition(position.x(), position.y()));
            }
        }
    }

    private NodePosition nodePosition(EbslScriptGraphNode node, int index) {
        return graphNodePositions.computeIfAbsent(node.key(), ignored -> {
            EbslGraphNodePosition position = EbslGraphAutoLayout.layout(List.of(node)).get(node.key());
            return position == null
                ? new NodePosition(28.0f, 28.0f + index * 92.0f)
                : new NodePosition(position.x(), position.y());
        });
    }

    private float drawNode(ImDrawList dl, EbslScriptManager manager, int index, String key, float x, float y, EbslScriptGraphNode node) {
        float width = Math.max(156.0f, Math.min(280.0f, node.line().length() * 7.0f + 24.0f)) * graphZoom;
        float height = NODE_H * graphZoom;
        EbslNodeTemplate template = EbslNodeTemplate.of(node.command());
        int fill = selectedGraphNode == index ? 0xFF22364A : EbslNodeCategoryColors.body(node.category());
        dl.addRectFilled(x, y, x + width, y + height, fill, 6.0f);
        dl.addRect(x, y, x + width, y + height, 0xFF67B7FF, 6.0f, 0, 1.5f);
        dl.addRectFilled(x, y, x + width, y + 18.0f * graphZoom, EbslNodeCategoryColors.header(node.category()), 6.0f);
        dl.addCircleFilled(x + 10.0f * graphZoom, y + height * 0.5f, 4.0f * graphZoom, 0xFF67B7FF);
        dl.addCircleFilled(x + width - 10.0f * graphZoom, y + height * 0.5f, 4.0f * graphZoom, 0xFF67B7FF);
        dl.addText(x + 18.0f * graphZoom, y + 3.0f * graphZoom, UiTheme.TEXT, template.title());
        dl.addText(x + 20.0f * graphZoom, y + 24.0f * graphZoom, UiTheme.TEXT_MUTED, node.command());
        dl.addText(x + 20.0f * graphZoom, y + 40.0f * graphZoom, UiTheme.TEXT_DIM, node.args().isBlank() ? template.argsHint() : node.args());

        ImGui.setCursorScreenPos(x, y);
        if (ImGui.invisibleButton("##ebsl-graph-node-" + index, width, height)) {
            selectedGraphNode = index;
            selectedGraphKey = "";
        }
        dragNode(manager, key);
        return width;
    }

    private void dragNode(EbslScriptManager manager, String key) {
        if (ImGui.isItemClicked()) {
            draggedNodeKey = key;
            lastMouseX = ImGui.getMousePosX();
            lastMouseY = ImGui.getMousePosY();
        }
        if (draggedNodeKey.equals(key) && ImGui.isItemActive() && ImGui.isMouseDragging(0)) {
            float mouseX = ImGui.getMousePosX();
            float mouseY = ImGui.getMousePosY();
            float dx = (mouseX - lastMouseX) / graphZoom;
            float dy = (mouseY - lastMouseY) / graphZoom;
            graphNodePositions.computeIfPresent(key, (ignored, position) -> position.move(dx, dy));
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            status = "moved node";
        }
        if (draggedNodeKey.equals(key) && ImGui.isMouseReleased(0)) {
            draggedNodeKey = "";
            saveGraphLayout(manager);
        }
    }

    private List<EbslScriptGraphNode> graphNodes() {
        return EbslScriptGraphParser.parse(source.get(), this::layoutKey);
    }

    private void drawMiniMap(UiRect editor, List<EbslScriptGraphNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        float w = 118.0f;
        float h = 76.0f;
        float x = editor.x() + 12.0f;
        float y = editor.bottom() - h - 12.0f;
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(x, y, x + w, y + h, 0xCC101820, 4.0f);
        dl.addRect(x, y, x + w, y + h, 0x664A90E2, 4.0f);
        for (int i = 0; i < nodes.size(); i++) {
            NodePosition position = nodePosition(nodes.get(i), i);
            float px = x + 8.0f + Math.min(w - 16.0f, Math.max(0.0f, position.x() / 8.0f));
            float py = y + 8.0f + Math.min(h - 16.0f, Math.max(0.0f, position.y() / 8.0f));
            dl.addRectFilled(px, py, px + 6.0f, py + 4.0f, EbslNodeCategoryColors.body(nodes.get(i).category()));
        }
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

    private void syncSelectedEditor(EbslScriptGraphNode node, EbslNode ebslNode) {
        if (selectedGraphKey.equals(node.key())) {
            return;
        }
        selectedGraphKey = node.key();
        selectedCommand.set(node.command());
        selectedArgs.set(node.args());
        settingTextValues.clear();
        if (ebslNode != null) {
            ebslNode.loadArgs(splitArgs(node.args()));
        }
    }

    private void applyNodeEdit(EbslScriptManager manager, EbslScriptGraphNode node, EbslNode ebslNode) {
        if (hasSettings(ebslNode)) {
            replaceNodeLine(node, node.command(), ebslNode.argsFromSettings());
            saveGraphLayout(manager);
            return;
        }
        replaceNodeLine(node, selectedCommand.get(), selectedArgs.get());
        saveGraphLayout(manager);
    }

    private boolean hasSettings(EbslNode node) {
        return node != null && !node.settings().isEmpty();
    }

    private void renderNodeSettings(EbslNode node, float width) {
        ImGuiSettingRenderContext context = new ImGuiSettingRenderContext(
            "ebsl-node-setting-" + node.id(),
            width,
            () -> { },
            settingTextValues
        );
        for (Setting<?> setting : node.settings()) {
            ImGuiSettingRendererRegistry.render(setting, context);
        }
    }

    private void replaceNodeLine(EbslScriptGraphNode node, String command, String args) {
        String normalizedCommand = command == null ? "" : command.trim().toLowerCase(java.util.Locale.ROOT).replace('-', '_');
        if (normalizedCommand.isBlank()) {
            status = "empty command";
            return;
        }
        String normalizedArgs = args == null ? "" : args.trim();
        String replacement = normalizedArgs.isBlank() ? normalizedCommand : normalizedCommand + " " + normalizedArgs;
        mutateSourceLine(node.lineNumber(), replacement, LineMutation.REPLACE);
        selectedGraphKey = "";
        status = "updated node";
    }

    private List<String> splitArgs(String args) {
        if (args == null || args.isBlank()) {
            return List.of();
        }
        return EbslTokenizer.tokenize(args).stream()
            .filter(token -> !token.equals(EbslSyntax.LINE_END)
                && !token.equals(EbslSyntax.STATEMENT_END)
                && !token.equals(EbslSyntax.BLOCK_OPEN)
                && !token.equals(EbslSyntax.BLOCK_CLOSE))
            .toList();
    }

    private void duplicateNodeLine(EbslScriptManager manager, EbslScriptGraphNode node) {
        mutateSourceLine(node.lineNumber(), node.line(), LineMutation.DUPLICATE);
        shiftGraphPositionsAfterInsert(node.lineNumber());
        saveGraphLayout(manager);
        status = "duplicated node";
    }

    private void deleteNodeLine(EbslScriptManager manager, EbslScriptGraphNode node) {
        mutateSourceLine(node.lineNumber(), "", LineMutation.DELETE);
        shiftGraphPositionsAfterDelete(node.lineNumber());
        selectedGraphNode = -1;
        selectedGraphKey = "";
        saveGraphLayout(manager);
        status = "deleted node";
    }

    private void mutateSourceLine(int lineNumber, String value, LineMutation mutation) {
        String[] lines = source.get().split("\\R", -1);
        int index = Math.max(0, Math.min(lines.length - 1, lineNumber - 1));
        List<String> rewritten = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (i == index && mutation == LineMutation.DELETE) {
                continue;
            }
            rewritten.add(i == index && mutation == LineMutation.REPLACE ? value : lines[i]);
            if (i == index && mutation == LineMutation.DUPLICATE) {
                rewritten.add(value);
            }
        }
        source.set(String.join("\n", rewritten));
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
        if (!loadedFile.equals(state.selectedScriptFile()) || loadedRevision != state.scriptRevision()) {
            load(state, platform);
        }
    }

    private void load(EbslUiState state, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        EbslScriptDocument document = manager.load(state.selectedScriptFile());
        loadedFile = document.fileName();
        loadedRevision = state.scriptRevision();
        source.set(document.source());
        loadGraphLayout(manager);
        status = "loaded";
    }

    private void loadGraphLayout(EbslScriptManager manager) {
        graphNodePositions.clear();
        for (Map.Entry<String, EbslGraphNodePosition> entry : manager.loadGraphLayout(loadedFile).entrySet()) {
            graphNodePositions.put(entry.getKey(), new NodePosition(entry.getValue().x(), entry.getValue().y()));
        }
    }

    private void saveGraphLayout(EbslScriptManager manager) {
        Map<String, EbslGraphNodePosition> positions = new HashMap<>();
        for (Map.Entry<String, NodePosition> entry : graphNodePositions.entrySet()) {
            positions.put(entry.getKey(), new EbslGraphNodePosition(entry.getValue().x(), entry.getValue().y()));
        }
        manager.saveGraphLayout(loadedFile, positions);
    }

    private void autoLayoutGraph(EbslScriptManager manager) {
        graphNodePositions.clear();
        for (Map.Entry<String, EbslGraphNodePosition> entry : EbslGraphAutoLayout.layout(graphNodes()).entrySet()) {
            graphNodePositions.put(entry.getKey(), new NodePosition(entry.getValue().x(), entry.getValue().y()));
        }
        saveGraphLayout(manager);
        status = "auto layout";
    }

    private String layoutKey(int lineNumber) {
        return loadedFile + ":" + lineNumber;
    }

    private void shiftGraphPositionsAfterInsert(int lineNumber) {
        Map<String, NodePosition> shifted = new HashMap<>();
        for (Map.Entry<String, NodePosition> entry : graphNodePositions.entrySet()) {
            int existingLine = layoutLine(entry.getKey());
            String key = existingLine > lineNumber ? layoutKey(existingLine + 1) : entry.getKey();
            shifted.put(key, entry.getValue());
        }
        graphNodePositions.clear();
        graphNodePositions.putAll(shifted);
    }

    private void shiftGraphPositionsAfterDelete(int lineNumber) {
        Map<String, NodePosition> shifted = new HashMap<>();
        for (Map.Entry<String, NodePosition> entry : graphNodePositions.entrySet()) {
            int existingLine = layoutLine(entry.getKey());
            if (existingLine == lineNumber) {
                continue;
            }
            String key = existingLine > lineNumber ? layoutKey(existingLine - 1) : entry.getKey();
            shifted.put(key, entry.getValue());
        }
        graphNodePositions.clear();
        graphNodePositions.putAll(shifted);
    }

    private int layoutLine(String key) {
        String prefix = loadedFile + ":";
        if (!key.startsWith(prefix)) {
            return -1;
        }
        try {
            return Integer.parseInt(key.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private record NodePosition(float x, float y) {
        private NodePosition move(float dx, float dy) {
            return new NodePosition(x + dx, y + dy);
        }
    }

    private enum LineMutation {
        REPLACE,
        DUPLICATE,
        DELETE
    }
}
