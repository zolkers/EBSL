package fr.riege.ebsl.common.feature.ui.imgui.graph;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

public final class EbslScriptGraphParser {
    private EbslScriptGraphParser() {
    }

    public static List<EbslScriptGraphNode> parse(String source, IntFunction<String> keyFactory) {
        List<EbslScriptGraphNode> nodes = new ArrayList<>();
        String[] lines = (source == null ? "" : source).split("\\R");
        int depth = 0;
        for (int i = 0; i < lines.length; i++) {
            LineShape shape = shape(lines[i], depth);
            depth = shape.nextDepth();
            if (!shape.visible()) {
                continue;
            }
            String command = command(shape.line());
            String args = args(shape.line(), command);
            EbslNodeCategory category = EbslNodeTemplate.of(command).category();
            int lineNumber = i + 1;
            nodes.add(new EbslScriptGraphNode(
                lineNumber,
                shape.line(),
                command,
                args,
                category,
                keyFactory.apply(lineNumber),
                shape.depth(),
                shape.blockStart()
            ));
        }
        return nodes;
    }

    private static LineShape shape(String raw, int currentDepth) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isBlank() || trimmed.startsWith("#")) {
            return LineShape.hidden(currentDepth);
        }
        int depth = currentDepth;
        while (trimmed.startsWith("}")) {
            depth = Math.max(0, depth - 1);
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isBlank() || trimmed.equals("{") || trimmed.startsWith("#")) {
            return LineShape.hidden(depth);
        }
        boolean blockStart = trimmed.endsWith("{");
        if (blockStart) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (trimmed.isBlank()) {
            return LineShape.hidden(blockStart ? depth + 1 : depth);
        }
        return new LineShape(true, trimmed, depth, blockStart, blockStart ? depth + 1 : depth);
    }

    private static String command(String line) {
        return line.split("\\s+", 2)[0].toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static String args(String line, String command) {
        return line.length() > command.length()
            ? line.substring(Math.min(line.length(), command.length())).trim()
            : "";
    }

    private record LineShape(boolean visible, String line, int depth, boolean blockStart, int nextDepth) {
        private static LineShape hidden(int depth) {
            return new LineShape(false, "", depth, false, depth);
        }
    }
}
