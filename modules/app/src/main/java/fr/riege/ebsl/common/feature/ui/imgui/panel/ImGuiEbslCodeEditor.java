package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorStyle;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxHighlighter;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxThemeRegistry;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslSyntaxToken;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;

import java.util.List;

final class ImGuiEbslCodeEditor {
    private final CodeEditorInputCallback inputCallback = new CodeEditorInputCallback();
    private int cursorPos;
    private int selectionStart;
    private int selectionEnd;

    void render(UiRect code, ImString source) {
        ImDrawList dl = ImGui.getWindowDrawList();
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        dl.addRectFilled(code.x(), code.y(), code.right(), code.bottom(), style.backgroundColor(), 4.0f);
        dl.addRect(code.x(), code.y(), code.right(), code.bottom(), style.borderColor(), 4.0f, 0, 1.0f);
        UiRect gutter = new UiRect(code.x(), code.y(), 52, code.height());
        dl.addRectFilled(gutter.x(), gutter.y(), gutter.right(), gutter.bottom(), style.gutterColor(), 4.0f);
        drawLineNumbers(dl, gutter, source);
        UiRect textArea = new UiRect(code.x() + 58, code.y() + 6, Math.round(code.width() - 64.0f), Math.round(code.height() - 12.0f));
        drawSyntaxHighlight(dl, textArea, source);
        ImGui.setCursorScreenPos(textArea.x(), textArea.y());
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, style.textPadding(), style.textPadding());
        ImGui.pushStyleColor(ImGuiCol.Text, style.editableTextColor());
        ImGui.pushStyleColor(ImGuiCol.FrameBg, style.frameColor());
        ImGui.inputTextMultiline("##ebsl-code-editor", source, textArea.width(), textArea.height(),
            ImGuiInputTextFlags.CallbackAlways, inputCallback);
        boolean editorActive = ImGui.isItemActive() || ImGui.isItemFocused();
        ImGui.popStyleColor(2);
        ImGui.popStyleVar();
        drawCaret(dl, textArea, source, editorActive);
        dl.addText(code.right() - 128.0f, code.y() + 8.0f, UiTheme.TEXT_DIM, lineCount(source) + " lines");
    }

    private void drawCaret(ImDrawList dl, UiRect textArea, ImString source, boolean editorActive) {
        if (!editorActive || selectionStart != selectionEnd || !caretVisible()) {
            return;
        }
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        String sourceText = source.get();
        int cursor = Math.clamp(cursorPos, 0, sourceText.length());
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

    private static void drawSyntaxHighlight(ImDrawList dl, UiRect textArea, ImString source) {
        EbslCodeEditorStyle style = EbslCodeEditorStyle.DARK;
        List<List<EbslSyntaxToken>> lines = EbslSyntaxHighlighter.highlight(source.get());
        float lineHeight = ImGui.getTextLineHeight();
        int visible = Math.clamp((int) ((textArea.height() - style.textPadding()) / lineHeight), 1, lines.size());
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

    private static void drawLineNumbers(ImDrawList dl, UiRect gutter, ImString source) {
        int count = lineCount(source);
        float lineHeight = ImGui.getTextLineHeight();
        int visible = Math.clamp((int) ((gutter.height() - 12) / lineHeight), 1, count);
        for (int i = 0; i < visible; i++) {
            dl.addText(gutter.x() + 12.0f, gutter.y() + 8.0f + i * lineHeight, UiTheme.TEXT_DIM, Integer.toString(i + 1));
        }
    }

    private static int lineCount(ImString source) {
        return source.get().isEmpty() ? 1 : source.get().split("\\R", -1).length;
    }

    private final class CodeEditorInputCallback extends ImGuiInputTextCallback {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            cursorPos = data.getCursorPos();
            selectionStart = data.getSelectionStart();
            selectionEnd = data.getSelectionEnd();
        }
    }
}
