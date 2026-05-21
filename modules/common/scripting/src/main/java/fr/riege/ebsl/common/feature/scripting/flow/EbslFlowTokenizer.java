/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.scripting.flow;

import java.util.ArrayList;
import java.util.List;

final class EbslFlowTokenizer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int cursor;
    private int line = 1;

    EbslFlowTokenizer(String source) {
        this.source = source == null ? "" : source;
    }

    List<Token> tokens() {
        while (!done()) {
            char c = source.charAt(cursor);
            if (Character.isWhitespace(c)) {
                consumeWhitespace(c);
            } else if (c == '#') {
                consumeComment();
            } else if (c == '"') {
                tokens.add(new Token(TokenType.STRING, consumeString(), line));
            } else if (isIdentifierStart(c)) {
                tokens.add(new Token(TokenType.IDENTIFIER, consumeIdentifier(), line));
            } else if (c == '-' && peekNext('>')) {
                tokens.add(new Token(TokenType.SYMBOL, "->", line));
                cursor += 2;
            } else if ("{}=.;".indexOf(c) >= 0) {
                tokens.add(new Token(TokenType.SYMBOL, Character.toString(c), line));
                cursor++;
            } else {
                throw new IllegalArgumentException("Unexpected character '" + c + "' at line " + line);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return List.copyOf(tokens);
    }

    private void consumeWhitespace(char c) {
        if (c == '\n') {
            line++;
        }
        cursor++;
    }

    private void consumeComment() {
        while (!done() && source.charAt(cursor) != '\n') {
            cursor++;
        }
    }

    private String consumeString() {
        cursor++;
        StringBuilder value = new StringBuilder();
        while (!done() && source.charAt(cursor) != '"') {
            char c = source.charAt(cursor++);
            if (c == '\\' && !done()) {
                value.append(source.charAt(cursor++));
            } else {
                value.append(c);
            }
        }
        if (done()) {
            throw new IllegalArgumentException("Unterminated string at line " + line);
        }
        cursor++;
        return value.toString();
    }

    private String consumeIdentifier() {
        int start = cursor;
        while (!done() && isIdentifierPart(source.charAt(cursor))) {
            cursor++;
        }
        return source.substring(start, cursor);
    }

    private boolean peekNext(char expected) {
        return cursor + 1 < source.length() && source.charAt(cursor + 1) == expected;
    }

    private boolean done() {
        return cursor >= source.length();
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || c == '-' || c == ':' || c == '/' || c == ',' || c == '<' || c == '>';
    }
}
