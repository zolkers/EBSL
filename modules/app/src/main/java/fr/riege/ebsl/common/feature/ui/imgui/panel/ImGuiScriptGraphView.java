package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeField;
import fr.riege.ebsl.common.feature.scripting.manager.*;
import fr.riege.ebsl.common.feature.scripting.parser.EbslSyntax;
import fr.riege.ebsl.common.feature.scripting.parser.EbslTokenizer;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.ui.imgui.EbslNodeCategoryColors;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslGraphAutoLayout;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslScriptGraphNode;
import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslScriptGraphParser;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRenderContext;
import fr.riege.ebsl.common.feature.ui.imgui.settings.ImGuiSettingRendererRegistry;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

final class ImGuiScriptGraphView {
    private static final float NODE_H = 56.0f;
    private static final float GRAPH_INSPECTOR_MIN_WIDTH = 330.0f;
    private static final float GRAPH_INSPECTOR_MAX_WIDTH = 410.0f;
    private static final float GRAPH_INSPECTOR_GAP = 10.0f;

    private final ImString source;
    private final Consumer<String> statusSink;
    private final ImString selectedCommand = new ImString("", 96);
    private final ImString selectedArgs = new ImString("", 512);
    private final Map<String, ImString> settingTextValues = new HashMap<>();
    private final Map<String, NodePosition> graphNodePositions = new HashMap<>();
    private final ScriptGraphConnectionEditor connections = new ScriptGraphConnectionEditor();
    private String loadedFile = "";
    private int selectedGraphNode = -1;
    private String selectedGraphKey = "";
    private float graphPanX = 0.0f;
    private float graphPanY = 0.0f;
    private float graphZoom = 1.0f;
    private String draggedNodeKey = "";
    private float lastMouseX;
    private float lastMouseY;

    ImGuiScriptGraphView(ImString source, Consumer<String> statusSink) {
        this.source = source;
        this.statusSink = statusSink;
    }

    void render(UiRect editor, EbslPlatform platform, boolean interactive) {
        ImDrawList dl = ImGui.getWindowDrawList();
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        float inspectorWidth = editor.width() >= 780.0f
            ? Math.clamp(editor.width() * 0.30f, GRAPH_INSPECTOR_MIN_WIDTH, GRAPH_INSPECTOR_MAX_WIDTH)
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
        if (interactive) {
            panAndZoomCanvas(canvas);
        }
        drawGrid(dl, canvas);
        List<EbslScriptGraphNode> nodes = graphNodes();
        ensureAutoLayout(nodes);
        List<ScriptGraphNodeLayout> layouts = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            EbslScriptGraphNode node = nodes.get(i);
            NodePosition position = nodePosition(node, i);
            float nodeX = canvas.x() + graphPanX + position.x() * graphZoom;
            float nodeY = canvas.y() + graphPanY + position.y() * graphZoom;
            float width = nodeWidth(node);
            layouts.add(new ScriptGraphNodeLayout(i, node, nodeX, nodeY, width, NODE_H * graphZoom));
        }
        Map<String, ScriptGraphNodeLayout> layoutByKey = layoutByKey(layouts);
        if (connections.drawEdges(dl, layouts, layoutByKey, graphZoom)) {
            selectedGraphNode = -1;
            selectedGraphKey = "";
        }
        for (ScriptGraphNodeLayout layout : layouts) {
            drawNode(dl, manager, layout, interactive);
        }
        connections.finishDrag(dl, layoutByKey, graphZoom, () -> saveGraphLayout(manager), statusSink);
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

    void loadGraphLayout(EbslScriptManager manager, String loadedFile) {
        this.loadedFile = loadedFile;
        graphNodePositions.clear();
        EbslGraphDocument document = manager.loadGraphDocument(loadedFile);
        for (Map.Entry<String, EbslGraphNodePosition> entry : document.positions().entrySet()) {
            graphNodePositions.put(entry.getKey(), new NodePosition(entry.getValue().x(), entry.getValue().y()));
        }
        connections.load(document.connections());
    }

    private void renderGraphToolbar(UiRect editor, EbslScriptManager manager) {
        ImGui.setCursorScreenPos(editor.x() + 10.0f, editor.y() + 6.0f);
        if (ImGui.button("-", 24.0f, 22.0f)) {
            graphZoom = Math.clamp(graphZoom - 0.1f, 0.65f, 1.6f);
        }
        ImGui.sameLine();
        if (ImGui.button("+", 24.0f, 22.0f)) {
            graphZoom = Math.clamp(graphZoom + 0.1f, 0.65f, 1.6f);
        }
        ImGui.sameLine();
        if (ImGui.button("Auto", 48.0f, 22.0f)) {
            autoLayoutGraph(manager);
        }
        ImGui.sameLine();
        if (ImGui.button("Run flow", 74.0f, 22.0f)) {
            manager.save(loadedFile, source.get());
            EbslScriptTask.INSTANCE.runFile(loadedFile);
            statusSink.accept("executed flow");
        }
        ImGui.sameLine();
        if (ImGui.button("Reset view", 82.0f, 22.0f)) {
            graphPanX = 0.0f;
            graphPanY = 0.0f;
            graphZoom = 1.0f;
        }
        ImGui.sameLine();
        ImGui.textDisabled("drag ports to connect | detach in inspector | right-drag canvas | wheel zoom");
    }

    private void renderFloatingNodeInspector(EbslScriptManager manager, UiRect editor, List<EbslScriptGraphNode> nodes) {
        if (!connections.hasSelectedConnection() && (selectedGraphNode < 0 || selectedGraphNode >= nodes.size())) {
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
        if (connections.hasSelectedConnection()) {
            renderEdgeInspector(manager, panel, pad);
            return;
        }
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
        if (ImGui.button("Run", 52.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.runInline(node.line());
            statusSink.accept("executed node");
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
        ImGui.sameLine();
        if (ImGui.button("Detach", 72.0f, 24.0f)) {
            connections.detachNode(node.key(), () -> saveGraphLayout(manager), statusSink);
        }
    }

    private void panAndZoomCanvas(UiRect canvas) {
        if (!ImGui.isMouseHoveringRect(canvas.x(), canvas.y(), canvas.right(), canvas.bottom(), true)) {
            return;
        }
        float wheel = ImGui.getIO().getMouseWheel();
        if (wheel != 0.0f) {
            graphZoom = Math.clamp(graphZoom + wheel * 0.08f, 0.65f, 1.7f);
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

    private float drawNode(ImDrawList dl, EbslScriptManager manager, ScriptGraphNodeLayout layout, boolean interactive) {
        int index = layout.index();
        String key = layout.node().key();
        float x = layout.x();
        float y = layout.y();
        EbslScriptGraphNode node = layout.node();
        float width = layout.width();
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

        float portHit = Math.max(18.0f, 18.0f * graphZoom);
        ImGui.setCursorScreenPos(x + portHit, y);
        if (ImGui.invisibleButton("##ebsl-graph-node-" + index, width - portHit * 2.0f, height)) {
            selectedGraphNode = index;
            selectedGraphKey = "";
            connections.clearSelection();
        }
        if (interactive) {
            dragNode(manager, key);
            connections.handlePorts(layout, graphZoom);
        }
        return width;
    }

    private void renderEdgeInspector(EbslScriptManager manager, UiRect panel, float pad) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRect(panel.x(), panel.y(), panel.right(), panel.bottom(), 0xFF2F271A, 5.0f, 0, 1.0f);
        ImGui.setCursorScreenPos(panel.x() + pad, panel.y() + 12.0f);
        connections.renderSelectedConnectionInspector(
            () -> saveGraphLayout(manager),
            statusSink,
            Math.max(120.0f, panel.width() - pad * 2.0f)
        );
    }

    private float nodeWidth(EbslScriptGraphNode node) {
        return Math.clamp(node.line().length() * 7.0f + 24.0f, 156.0f, 280.0f) * graphZoom;
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
            statusSink.accept("moved node");
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
            float px = x + 8.0f + Math.clamp(position.x() / 8.0f, 0.0f, w - 16.0f);
            float py = y + 8.0f + Math.clamp(position.y() / 8.0f, 0.0f, h - 16.0f);
            dl.addRectFilled(px, py, px + 6.0f, py + 4.0f, EbslNodeCategoryColors.body(nodes.get(i).category()));
        }
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

    private static boolean hasSettings(EbslNode node) {
        return node != null && !node.settings().isEmpty();
    }

    private void renderNodeSettings(EbslNode node, float width) {
        ImGuiSettingRenderContext context = new ImGuiSettingRenderContext(
            "ebsl-node-setting-" + node.id(),
            width,
            () -> { },
            settingTextValues
        );
        for (EbslNodeField field : node.fields()) {
            Setting<?> setting = field.setting();
            ImGui.textWrapped(field.description());
            ImGuiSettingRendererRegistry.render(setting, context);
            ImGui.textDisabled(EbslNodeFieldHelp.meta(setting));
            ImGui.spacing();
        }
    }

    private void replaceNodeLine(EbslScriptGraphNode node, String command, String args) {
        String normalizedCommand = command == null ? "" : command.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        if (normalizedCommand.isBlank()) {
            statusSink.accept("empty command");
            return;
        }
        String normalizedArgs = args == null ? "" : args.trim();
        String replacement = normalizedArgs.isBlank() ? normalizedCommand : normalizedCommand + " " + normalizedArgs;
        mutateSourceLine(node.lineNumber(), replacement, LineMutation.REPLACE);
        selectedGraphKey = "";
        statusSink.accept("updated node");
    }

    private static List<String> splitArgs(String args) {
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
        connections.shiftAfterInsert(node.lineNumber(), this::layoutLine, this::layoutKey);
        saveGraphLayout(manager);
        statusSink.accept("duplicated node");
    }

    private void deleteNodeLine(EbslScriptManager manager, EbslScriptGraphNode node) {
        mutateSourceLine(node.lineNumber(), "", LineMutation.DELETE);
        connections.removeNode(node.key());
        shiftGraphPositionsAfterDelete(node.lineNumber());
        connections.shiftAfterDelete(node.lineNumber(), this::layoutLine, this::layoutKey);
        selectedGraphNode = -1;
        selectedGraphKey = "";
        saveGraphLayout(manager);
        statusSink.accept("deleted node");
    }

    private void mutateSourceLine(int lineNumber, String value, LineMutation mutation) {
        String[] lines = source.get().split("\\R", -1);
        int index = (int) Math.clamp(lineNumber - 1L, 0L, lines.length - 1L);
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

    private void saveGraphLayout(EbslScriptManager manager) {
        Map<String, EbslGraphNodePosition> positions = new HashMap<>();
        for (Map.Entry<String, NodePosition> entry : graphNodePositions.entrySet()) {
            positions.put(entry.getKey(), new EbslGraphNodePosition(entry.getValue().x(), entry.getValue().y()));
        }
        manager.saveGraphDocument(loadedFile, new EbslGraphDocument(positions, connections.connections()));
    }

    private void autoLayoutGraph(EbslScriptManager manager) {
        graphNodePositions.clear();
        for (Map.Entry<String, EbslGraphNodePosition> entry : EbslGraphAutoLayout.layout(graphNodes()).entrySet()) {
            graphNodePositions.put(entry.getKey(), new NodePosition(entry.getValue().x(), entry.getValue().y()));
        }
        saveGraphLayout(manager);
        statusSink.accept("auto layout");
    }

    private static Map<String, ScriptGraphNodeLayout> layoutByKey(List<ScriptGraphNodeLayout> layouts) {
        Map<String, ScriptGraphNodeLayout> byKey = new HashMap<>();
        for (ScriptGraphNodeLayout layout : layouts) {
            byKey.put(layout.node().key(), layout);
        }
        return byKey;
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
