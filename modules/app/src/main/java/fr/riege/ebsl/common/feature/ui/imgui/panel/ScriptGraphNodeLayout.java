package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.imgui.graph.EbslScriptGraphNode;

record ScriptGraphNodeLayout(int index, EbslScriptGraphNode node, float x, float y, float width, float height) {
    float right() {
        return x + width;
    }

    float bottom() {
        return y + height;
    }

    float centerY() {
        return y + height * 0.5f;
    }
}
