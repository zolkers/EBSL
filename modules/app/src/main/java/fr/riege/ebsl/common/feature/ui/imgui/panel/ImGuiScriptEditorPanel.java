package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeField;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDoc;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocEntry;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocGenerator;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocParameter;
import fr.riege.ebsl.common.feature.scripting.docs.EbslLanguageDocSection;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorSettings;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNodePosition;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeFieldHelp;
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
import fr.riege.ebsl.common.platform.service.EbslServices;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class ImGuiScriptEditorPanel {
    private static final int BUFFER_SIZE = 65536;
    private static final float NODE_H = 56.0f;
    private static final float GRAPH_INSPECTOR_MIN_WIDTH = 330.0f;
    private static final float GRAPH_INSPECTOR_MAX_WIDTH = 410.0f;
    private static final float GRAPH_INSPECTOR_GAP = 10.0f;
    private static final float EDGE_NODE_MARGIN = 22.0f;
    private static final float EDGE_PORT_STUB = 26.0f;
    private static final String DOC_POPUP_ID = "EBSL Language Doc##ebsl-language-doc";
    private static final String IDE_SETTINGS_POPUP_ID = "IDE Settings##ebsl-ide-settings";

    private final ImString source = new ImString(EbslScriptManager.DEFAULT_SOURCE, BUFFER_SIZE);
    private final ImString selectedCommand = new ImString("", 96);
    private final ImString selectedArgs = new ImString("", 512);
    private final Map<String, ImString> settingTextValues = new HashMap<>();
    private final Map<String, ImString> editorSettingTextValues = new HashMap<>();
    private final CodeEditorInputCallback codeEditorInputCallback = new CodeEditorInputCallback();
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
    private boolean docOpenRequested;
    private boolean ideSettingsOpenRequested;
    private int codeCursorPos;
    private int codeSelectionStart;
    private int codeSelectionEnd;

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
        renderIdeSettingsPopup(viewport);
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
            docOpenRequested = true;
        }
        ImGui.sameLine();
        if (ImGui.button("Settings", 82.0f, 22.0f)) {
            ideSettingsOpenRequested = true;
        }
        ImGui.sameLine();
        ImGui.textDisabled(status);
    }

    private void renderIdeSettingsPopup(UiRect viewport) {
        if (ideSettingsOpenRequested) {
            ImGui.openPopup(IDE_SETTINGS_POPUP_ID);
            ideSettingsOpenRequested = false;
        }
        float width = Math.min(560.0f, viewport.width() - 80.0f);
        float height = Math.min(520.0f, viewport.height() - 80.0f);
        float x = viewport.x() + (viewport.width() - width) * 0.5f;
        float y = viewport.y() + (viewport.height() - height) * 0.5f;
        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        int flags = ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoMove;
        if (ImGui.beginPopupModal(IDE_SETTINGS_POPUP_ID, flags)) {
            if (ImGui.button("Close", 72.0f, 22.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("Reset", 72.0f, 22.0f)) {
                EbslCodeEditorSettings.resetToDefaults();
                saveSettings();
            }
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-ide-settings-scroll", width - 24.0f, height - 72.0f, false)) {
                ImGuiSettingRenderContext context = new ImGuiSettingRenderContext(
                    "script-editor-setting", -1.0f, this::saveSettings, editorSettingTextValues);
                renderIdeSettingsGroup("Editor", EbslCodeEditorSettings.editorAppearanceSettings(), context);
                ImGui.spacing();
                renderIdeSettingsGroup("Language", EbslCodeEditorSettings.languageThemeSettings(), context);
                ImGui.endChild();
            }
            ImGui.endPopup();
        }
    }

    private void renderIdeSettingsGroup(String title, List<Setting<?>> settings, ImGuiSettingRenderContext context) {
        ImGui.text(title);
        ImGui.separator();
        for (Setting<?> setting : settings) {
            ImGuiSettingRendererRegistry.render(setting, context);
        }
    }

    private void renderDocPopup(UiRect viewport) {
        if (docOpenRequested) {
            ImGui.openPopup(DOC_POPUP_ID);
            docOpenRequested = false;
        }
        float width = Math.min(760.0f, viewport.width() - 80.0f);
        float height = Math.min(620.0f, viewport.height() - 80.0f);
        float x = viewport.x() + (viewport.width() - width) * 0.5f;
        float y = viewport.y() + (viewport.height() - height) * 0.5f;
        ImGui.setNextWindowPos(x, y, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        int flags = ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoMove;
        if (ImGui.beginPopupModal(DOC_POPUP_ID, flags)) {
            if (ImGui.button("Close", 72.0f, 22.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            ImGui.textDisabled("Generated from language registries");
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-doc-scroll", width - 24.0f, height - 76.0f, false)) {
                renderLanguageDoc(EbslLanguageDocGenerator.generate());
                ImGui.endChild();
            }
            ImGui.endPopup();
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
            if (!parameter.description().isBlank()) {
                ImGui.textWrapped(parameter.description());
            }
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
        List<GraphNodeLayout> layouts = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            EbslScriptGraphNode node = nodes.get(i);
            NodePosition position = nodePosition(node, i);
            float nodeX = canvas.x() + graphPanX + position.x() * graphZoom;
            float nodeY = canvas.y() + graphPanY + position.y() * graphZoom;
            float width = nodeWidth(node);
            layouts.add(new GraphNodeLayout(i, node, nodeX, nodeY, width, NODE_H * graphZoom));
        }
        for (int i = 1; i < layouts.size(); i++) {
            drawEdge(dl, layouts.get(i - 1), layouts.get(i), layouts);
        }
        for (GraphNodeLayout layout : layouts) {
            drawNode(dl, manager, layout.index(), layout.node().key(), layout.x(), layout.y(), layout.node(), layout.width());
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
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        dl.addRectFilled(code.x(), code.y(), code.right(), code.bottom(), style.backgroundColor(), 4.0f);
        dl.addRect(code.x(), code.y(), code.right(), code.bottom(), style.borderColor(), 4.0f, 0, 1.0f);
        UiRect gutter = new UiRect(code.x(), code.y(), 52, code.height());
        dl.addRectFilled(gutter.x(), gutter.y(), gutter.right(), gutter.bottom(), style.gutterColor(), 4.0f);
        drawLineNumbers(dl, gutter);
        UiRect textArea = new UiRect(code.x() + 58, code.y() + 6, Math.round(code.width() - 64.0f), Math.round(code.height() - 12.0f));
        drawSyntaxHighlight(dl, textArea);
        ImGui.setCursorScreenPos(textArea.x(), textArea.y());
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, style.textPadding(), style.textPadding());
        ImGui.pushStyleColor(ImGuiCol.Text, style.editableTextColor());
        ImGui.pushStyleColor(ImGuiCol.FrameBg, style.frameColor());
        ImGui.inputTextMultiline("##ebsl-code-editor", source, textArea.width(), textArea.height(),
            ImGuiInputTextFlags.CallbackAlways, codeEditorInputCallback);
        boolean editorActive = ImGui.isItemActive() || ImGui.isItemFocused();
        ImGui.popStyleColor(2);
        ImGui.popStyleVar();
        drawCodeCaret(dl, textArea, editorActive);
        dl.addText(code.right() - 128.0f, code.y() + 8.0f, UiTheme.TEXT_DIM, lineCount() + " lines");
    }

    private void drawCodeCaret(ImDrawList dl, UiRect textArea, boolean editorActive) {
        if (!editorActive || codeSelectionStart != codeSelectionEnd || !caretVisible()) {
            return;
        }
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        String sourceText = source.get();
        int cursor = Math.max(0, Math.min(codeCursorPos, sourceText.length()));
        int lineStart = 0;
        int line = 0;
        for (int i = 0; i < cursor; i++) {
            if (sourceText.charAt(i) == '\n') {
                line++;
                lineStart = i + 1;
            }
        }
        String beforeCursor = sourceText.substring(lineStart, cursor).replace("\r", "");
        float x = textArea.x() + style.textPadding() + textAdvance(beforeCursor);
        float y = textArea.y() + style.textPadding() + line * ImGui.getTextLineHeight();
        if (y < textArea.y() || y > textArea.bottom()) {
            return;
        }
        dl.pushClipRect(textArea.x(), textArea.y(), textArea.right(), textArea.bottom(), true);
        dl.addLine(x, y, x, y + ImGui.getTextLineHeight(), style.caretColor(), style.caretThickness());
        dl.popClipRect();
    }

    private static boolean caretVisible() {
        double blink = EbslCodeEditorStyle.DARK.caretBlinkSeconds();
        return blink <= 0.0 || ((int) (ImGui.getTime() / blink)) % 2 == 0;
    }

    private void saveSettings() {
        CommonSettingsStore.save(EbslServices.platform().storage());
    }

    private void drawSyntaxHighlight(ImDrawList dl, UiRect textArea) {
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        List<List<EbslSyntaxToken>> lines = EbslSyntaxHighlighter.highlight(source.get());
        float lineHeight = ImGui.getTextLineHeight();
        int visible = Math.min(lines.size(), Math.max(1, (int) ((textArea.height() - style.textPadding()) / lineHeight)));
        dl.pushClipRect(textArea.x(), textArea.y(), textArea.right(), textArea.bottom(), true);
        for (int lineIndex = 0; lineIndex < visible; lineIndex++) {
            float x = textArea.x() + style.textPadding();
            float y = textArea.y() + style.textPadding() + lineIndex * lineHeight;
            for (EbslSyntaxToken token : lines.get(lineIndex)) {
                int color = EbslSyntaxThemeRegistry.style(token.kind()).color();
                if (color != 0) {
                    dl.addText(x, y, color, token.text());
                }
                x += textAdvance(token.text());
            }
        }
        dl.popClipRect();
    }

    private static float textAdvance(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0f;
        }
        if (!text.isBlank()) {
            return ImGui.calcTextSizeX(text);
        }
        float spaceWidth = ImGui.calcTextSizeX("x x") - ImGui.calcTextSizeX("xx");
        if (spaceWidth <= 0.0f) {
            spaceWidth = Math.max(1.0f, ImGui.calcTextSizeX("x") * 0.5f);
        }
        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            width += text.charAt(i) == '\t' ? spaceWidth * 4.0f : spaceWidth;
        }
        return width;
    }

    private void drawLineNumbers(ImDrawList dl, UiRect gutter) {
        int count = lineCount();
        float lineHeight = ImGui.getTextLineHeight();
        int visible = Math.min(count, Math.max(1, (int) ((gutter.height() - 12) / lineHeight)));
        for (int i = 0; i < visible; i++) {
            dl.addText(gutter.x() + 12.0f, gutter.y() + 8.0f + i * lineHeight, UiTheme.TEXT_DIM, Integer.toString(i + 1));
        }
    }

    private final class CodeEditorInputCallback extends ImGuiInputTextCallback {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            codeCursorPos = data.getCursorPos();
            codeSelectionStart = data.getSelectionStart();
            codeSelectionEnd = data.getSelectionEnd();
        }
    }

    private int lineCount() {
        return source.get().isEmpty() ? 1 : source.get().split("\\R", -1).length;
    }

    private void drawEdge(ImDrawList dl, GraphNodeLayout from, GraphNodeLayout to, List<GraphNodeLayout> layouts) {
        float portInset = 10.0f * graphZoom;
        float fromX = from.right() - portInset;
        float fromY = from.centerY();
        float toX = to.x() + portInset;
        float toY = to.centerY();
        float stub = EDGE_PORT_STUB * graphZoom;
        float startLaneX = from.right() + stub;
        float endLaneX = to.x() - stub;
        float startRouteY = fromY;
        float endRouteY = toY;
        if (needsSeparatedPortLanes(fromX, startLaneX, fromY, endLaneX, toX, toY)) {
            float separation = Math.max(10.0f, 14.0f * graphZoom);
            float direction = to.centerY() >= from.centerY() ? 1.0f : -1.0f;
            startRouteY = fromY - direction * separation;
            endRouteY = toY + direction * separation;
        }
        int color = to.node().depth() > 0 ? 0xAA67B7FF : 0xCC67B7FF;
        List<EdgePoint> route = routeEdge(
            new EdgePoint(startLaneX, startRouteY),
            new EdgePoint(endLaneX, endRouteY),
            layouts);

        drawEdgeSegment(dl, fromX, fromY, startLaneX, fromY, color);
        drawEdgeSegment(dl, startLaneX, fromY, startLaneX, startRouteY, color);
        for (int i = 1; i < route.size(); i++) {
            EdgePoint a = route.get(i - 1);
            EdgePoint b = route.get(i);
            drawEdgeSegment(dl, a.x(), a.y(), b.x(), b.y(), color);
        }
        drawEdgeSegment(dl, endLaneX, endRouteY, endLaneX, toY, color);
        drawEdgeSegment(dl, endLaneX, toY, toX, toY, color);
        dl.addCircleFilled(fromX, fromY, 3.5f, 0xFF67B7FF);
        dl.addCircleFilled(toX, toY, 3.5f, 0xFF67B7FF);
    }

    private static boolean needsSeparatedPortLanes(float startA, float startB, float startY,
                                                   float endA, float endB, float endY) {
        if (Math.abs(startY - endY) > 1.0f) {
            return false;
        }
        return startB >= endA || horizontalRangesOverlap(startA, startB, endA, endB);
    }

    private static boolean horizontalRangesOverlap(float a1, float a2, float b1, float b2) {
        float aMin = Math.min(a1, a2);
        float aMax = Math.max(a1, a2);
        float bMin = Math.min(b1, b2);
        float bMax = Math.max(b1, b2);
        return Math.min(aMax, bMax) - Math.max(aMin, bMin) > 1.0f;
    }

    private List<EdgePoint> routeEdge(EdgePoint start, EdgePoint end, List<GraphNodeLayout> layouts) {
        float margin = EDGE_NODE_MARGIN * graphZoom;
        List<EdgeObstacle> obstacles = edgeObstacles(layouts, margin);
        List<Float> xs = new ArrayList<>();
        List<Float> ys = new ArrayList<>();
        addCoord(xs, start.x());
        addCoord(xs, end.x());
        addCoord(ys, start.y());
        addCoord(ys, end.y());
        float minX = Math.min(start.x(), end.x());
        float maxX = Math.max(start.x(), end.x());
        float minY = Math.min(start.y(), end.y());
        float maxY = Math.max(start.y(), end.y());
        for (EdgeObstacle obstacle : obstacles) {
            minX = Math.min(minX, obstacle.left());
            maxX = Math.max(maxX, obstacle.right());
            minY = Math.min(minY, obstacle.top());
            maxY = Math.max(maxY, obstacle.bottom());
            addCoord(xs, obstacle.left());
            addCoord(xs, obstacle.right());
            addCoord(ys, obstacle.top());
            addCoord(ys, obstacle.bottom());
        }
        addCoord(xs, minX - margin * 1.5f);
        addCoord(xs, maxX + margin * 1.5f);
        addCoord(ys, minY - margin * 1.5f);
        addCoord(ys, maxY + margin * 1.5f);
        Collections.sort(xs);
        Collections.sort(ys);

        int sx = coordIndex(xs, start.x());
        int sy = coordIndex(ys, start.y());
        int ex = coordIndex(xs, end.x());
        int ey = coordIndex(ys, end.y());
        double[][] dist = new double[xs.size()][ys.size()];
        int[][] prevX = new int[xs.size()][ys.size()];
        int[][] prevY = new int[xs.size()][ys.size()];
        for (int x = 0; x < xs.size(); x++) {
            java.util.Arrays.fill(dist[x], Double.POSITIVE_INFINITY);
            java.util.Arrays.fill(prevX[x], -1);
            java.util.Arrays.fill(prevY[x], -1);
        }
        PriorityQueue<RouteNode> queue = new PriorityQueue<>(Comparator.comparingDouble(RouteNode::cost));
        dist[sx][sy] = 0.0;
        queue.add(new RouteNode(sx, sy, 0.0));
        while (!queue.isEmpty()) {
            RouteNode current = queue.poll();
            if (current.cost() > dist[current.x()][current.y()]) {
                continue;
            }
            if (current.x() == ex && current.y() == ey) {
                break;
            }
            relaxRouteNeighbor(current.x(), current.y(), current.x() - 1, current.y(), xs, ys, obstacles, dist, prevX, prevY, queue);
            relaxRouteNeighbor(current.x(), current.y(), current.x() + 1, current.y(), xs, ys, obstacles, dist, prevX, prevY, queue);
            relaxRouteNeighbor(current.x(), current.y(), current.x(), current.y() - 1, xs, ys, obstacles, dist, prevX, prevY, queue);
            relaxRouteNeighbor(current.x(), current.y(), current.x(), current.y() + 1, xs, ys, obstacles, dist, prevX, prevY, queue);
        }
        if (!Double.isFinite(dist[ex][ey])) {
            return List.of(start, end);
        }
        List<EdgePoint> reversed = new ArrayList<>();
        int x = ex;
        int y = ey;
        while (x >= 0 && y >= 0) {
            reversed.add(new EdgePoint(xs.get(x), ys.get(y)));
            if (x == sx && y == sy) {
                break;
            }
            int px = prevX[x][y];
            int py = prevY[x][y];
            x = px;
            y = py;
        }
        Collections.reverse(reversed);
        return simplifyRoute(reversed);
    }

    private List<EdgeObstacle> edgeObstacles(List<GraphNodeLayout> layouts, float margin) {
        List<EdgeObstacle> obstacles = new ArrayList<>();
        for (GraphNodeLayout layout : layouts) {
            obstacles.add(new EdgeObstacle(layout.x() - margin, layout.y() - margin, layout.right() + margin, layout.bottom() + margin));
        }
        return obstacles;
    }

    private void relaxRouteNeighbor(int x, int y, int nx, int ny, List<Float> xs, List<Float> ys,
                                    List<EdgeObstacle> obstacles, double[][] dist, int[][] prevX,
                                    int[][] prevY, PriorityQueue<RouteNode> queue) {
        if (nx < 0 || ny < 0 || nx >= xs.size() || ny >= ys.size()) {
            return;
        }
        EdgePoint from = new EdgePoint(xs.get(x), ys.get(y));
        EdgePoint to = new EdgePoint(xs.get(nx), ys.get(ny));
        if (pointInsideObstacle(to, obstacles) || segmentIntersectsObstacle(from, to, obstacles)) {
            return;
        }
        double nextCost = dist[x][y] + Math.abs(to.x() - from.x()) + Math.abs(to.y() - from.y());
        if (nextCost >= dist[nx][ny]) {
            return;
        }
        dist[nx][ny] = nextCost;
        prevX[nx][ny] = x;
        prevY[nx][ny] = y;
        queue.add(new RouteNode(nx, ny, nextCost));
    }

    private static boolean pointInsideObstacle(EdgePoint point, List<EdgeObstacle> obstacles) {
        for (EdgeObstacle obstacle : obstacles) {
            if (point.x() > obstacle.left() && point.x() < obstacle.right()
                && point.y() > obstacle.top() && point.y() < obstacle.bottom()) {
                return true;
            }
        }
        return false;
    }

    private static boolean segmentIntersectsObstacle(EdgePoint from, EdgePoint to, List<EdgeObstacle> obstacles) {
        for (EdgeObstacle obstacle : obstacles) {
            if (Math.abs(from.y() - to.y()) < 0.5f) {
                float y = from.y();
                float minX = Math.min(from.x(), to.x());
                float maxX = Math.max(from.x(), to.x());
                if (y > obstacle.top() && y < obstacle.bottom()
                    && maxX > obstacle.left() && minX < obstacle.right()) {
                    return true;
                }
            } else if (Math.abs(from.x() - to.x()) < 0.5f) {
                float x = from.x();
                float minY = Math.min(from.y(), to.y());
                float maxY = Math.max(from.y(), to.y());
                if (x > obstacle.left() && x < obstacle.right()
                    && maxY > obstacle.top() && minY < obstacle.bottom()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<EdgePoint> simplifyRoute(List<EdgePoint> points) {
        if (points.size() <= 2) {
            return points;
        }
        List<EdgePoint> simplified = new ArrayList<>();
        simplified.add(points.getFirst());
        for (int i = 1; i < points.size() - 1; i++) {
            EdgePoint previous = simplified.getLast();
            EdgePoint current = points.get(i);
            EdgePoint next = points.get(i + 1);
            boolean horizontal = Math.abs(previous.y() - current.y()) < 0.5f && Math.abs(current.y() - next.y()) < 0.5f;
            boolean vertical = Math.abs(previous.x() - current.x()) < 0.5f && Math.abs(current.x() - next.x()) < 0.5f;
            if (!horizontal && !vertical) {
                simplified.add(current);
            }
        }
        simplified.add(points.getLast());
        return simplified;
    }

    private static void addCoord(List<Float> coords, float value) {
        for (float coord : coords) {
            if (Math.abs(coord - value) < 0.5f) {
                return;
            }
        }
        coords.add(value);
    }

    private static int coordIndex(List<Float> coords, float value) {
        for (int i = 0; i < coords.size(); i++) {
            if (Math.abs(coords.get(i) - value) < 0.5f) {
                return i;
            }
        }
        return -1;
    }

    private void drawEdgeSegment(ImDrawList dl, float x1, float y1, float x2, float y2, int color) {
        if (Math.abs(x1 - x2) < 0.5f && Math.abs(y1 - y2) < 0.5f) {
            return;
        }
        dl.addLine(x1, y1, x2, y2, color, 2.0f);
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
            renderNodeSettings(node.command(), ebslNode, Math.max(120.0f, contentWidth - 20.0f));
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

    private float drawNode(ImDrawList dl, EbslScriptManager manager, int index, String key, float x, float y, EbslScriptGraphNode node, float width) {
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

    private float nodeWidth(EbslScriptGraphNode node) {
        return Math.max(156.0f, Math.min(280.0f, node.line().length() * 7.0f + 24.0f)) * graphZoom;
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

    private void renderNodeSettings(String command, EbslNode node, float width) {
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

    private record GraphNodeLayout(int index, EbslScriptGraphNode node, float x, float y, float width, float height) {
        private float right() {
            return x + width;
        }

        private float bottom() {
            return y + height;
        }

        private float centerY() {
            return y + height * 0.5f;
        }

    }

    private record EdgeObstacle(float left, float top, float right, float bottom) {
    }

    private record EdgePoint(float x, float y) {
    }

    private record RouteNode(int x, int y, double cost) {
    }

    private enum LineMutation {
        REPLACE,
        DUPLICATE,
        DELETE
    }
}
