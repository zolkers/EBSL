package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.feature.scripting.parser.EbslSyntax;

import java.util.ArrayList;
import java.util.List;

public final class EbslSyntaxHighlighter {
    private EbslSyntaxHighlighter() {
    }

    public static List<List<EbslSyntaxToken>> highlight(String source) {
        String[] lines = (source == null ? "" : source).split("\\R", -1);
        List<List<EbslSyntaxToken>> highlighted = new ArrayList<>(lines.length);
        for (String line : lines) {
            highlighted.add(highlightLine(line));
        }
        return highlighted;
    }

    private static List<EbslSyntaxToken> highlightLine(String line) {
        List<EbslSyntaxToken> tokens = new ArrayList<>();
        boolean firstToken = true;
        int index = 0;
        while (index < line.length()) {
            char c = line.charAt(index);
            if (Character.isWhitespace(c)) {
                int end = readWhile(line, index, Character::isWhitespace);
                tokens.add(new EbslSyntaxToken(EbslTokenKind.WHITESPACE, line.substring(index, end)));
                index = end;
            } else if (line.startsWith(EbslSyntax.COMMENT, index)) {
                tokens.add(new EbslSyntaxToken(EbslTokenKind.COMMENT, line.substring(index)));
                index = line.length();
            } else if (line.startsWith(EbslSyntax.QUOTE, index)) {
                int end = readString(line, index);
                tokens.add(new EbslSyntaxToken(EbslTokenKind.STRING, line.substring(index, end)));
                index = end;
                firstToken = false;
            } else if (isSingleCharToken(c)) {
                tokens.add(new EbslSyntaxToken(EbslTokenClassifierRegistry.classify(Character.toString(c), firstToken), Character.toString(c)));
                index++;
                firstToken = false;
            } else {
                int end = readWord(line, index);
                String text = line.substring(index, end);
                tokens.add(new EbslSyntaxToken(EbslTokenClassifierRegistry.classify(text, firstToken), text));
                index = end;
                firstToken = false;
            }
        }
        return List.copyOf(tokens);
    }

    private static boolean isSingleCharToken(char c) {
        return EbslSyntax.BLOCK_OPEN.charAt(0) == c
            || EbslSyntax.BLOCK_CLOSE.charAt(0) == c
            || EbslSyntax.STATEMENT_END.charAt(0) == c;
    }

    private static int readWord(String line, int start) {
        int index = start;
        while (index < line.length()
            && !Character.isWhitespace(line.charAt(index))
            && !isSingleCharToken(line.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int readString(String line, int start) {
        int index = start + EbslSyntax.QUOTE.length();
        while (index < line.length()) {
            if (line.startsWith(EbslSyntax.QUOTE, index)) {
                return index + EbslSyntax.QUOTE.length();
            }
            index++;
        }
        return line.length();
    }

    private static int readWhile(String line, int start, CharPredicate predicate) {
        int index = start;
        while (index < line.length() && predicate.test(line.charAt(index))) {
            index++;
        }
        return index;
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char value);
    }
}
