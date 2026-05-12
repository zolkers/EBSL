package fr.riege.ebsl.common.feature.scripting.parser;

import java.util.ArrayList;
import java.util.List;

public final class EbslTokenizer {
    private EbslTokenizer() {
    }

    public static List<String> tokenize(String source) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (!quoted && String.valueOf(c).equals(EbslSyntax.COMMENT)) {
                i = skipComment(source, i);
                tokens.add(EbslSyntax.LINE_END);
                continue;
            }
            if (String.valueOf(c).equals(EbslSyntax.QUOTE)) {
                quoted = !quoted;
                continue;
            }
            if (quoted) {
                token.append(c);
                continue;
            }
            if (isSeparator(c)) {
                flush(token, tokens);
                addSeparator(c, tokens);
                continue;
            }
            token.append(c);
        }
        flush(token, tokens);
        return tokens;
    }

    private static int skipComment(String source, int index) {
        while (index < source.length() && source.charAt(index) != '\n') {
            index++;
        }
        return index;
    }

    private static boolean isSeparator(char c) {
        return Character.isWhitespace(c)
            || String.valueOf(c).equals(EbslSyntax.STATEMENT_END)
            || String.valueOf(c).equals(EbslSyntax.BLOCK_OPEN)
            || String.valueOf(c).equals(EbslSyntax.BLOCK_CLOSE);
    }

    private static void addSeparator(char c, List<String> tokens) {
        if (c == '\n'
            || String.valueOf(c).equals(EbslSyntax.STATEMENT_END)
            || String.valueOf(c).equals(EbslSyntax.BLOCK_OPEN)
            || String.valueOf(c).equals(EbslSyntax.BLOCK_CLOSE)) {
            tokens.add(String.valueOf(c));
        }
    }

    private static void flush(StringBuilder token, List<String> tokens) {
        if (!token.isEmpty()) {
            tokens.add(token.toString());
            token.setLength(0);
        }
    }
}
