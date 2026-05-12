package fr.riege.ebsl.common.feature.ui.imgui.graph;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;

public record EbslScriptGraphNode(
    int lineNumber,
    String line,
    String command,
    String args,
    EbslNodeCategory category,
    String key,
    int depth,
    boolean blockStart
) {
}
