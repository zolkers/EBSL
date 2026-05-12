package fr.riege.ebsl.common.feature.ui.imgui.graph;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNodePosition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EbslGraphAutoLayout {
    private static final float START_X = 28.0f;
    private static final float START_Y = 28.0f;
    private static final float COLUMN_WIDTH = 248.0f;
    private static final float ROW_HEIGHT = 92.0f;
    private static final float BLOCK_EXTRA_GAP = 18.0f;

    private EbslGraphAutoLayout() {
    }

    public static Map<String, EbslGraphNodePosition> layout(List<EbslScriptGraphNode> nodes) {
        Map<String, EbslGraphNodePosition> positions = new HashMap<>();
        float y = START_Y;
        int previousDepth = 0;
        for (EbslScriptGraphNode node : nodes) {
            if (!positions.isEmpty() && node.depth() < previousDepth) {
                y += BLOCK_EXTRA_GAP * (previousDepth - node.depth());
            }
            float x = START_X + node.depth() * COLUMN_WIDTH;
            positions.put(node.key(), new EbslGraphNodePosition(x, y));
            y += ROW_HEIGHT + (node.blockStart() ? BLOCK_EXTRA_GAP : 0.0f);
            previousDepth = node.depth();
        }
        return positions;
    }
}
