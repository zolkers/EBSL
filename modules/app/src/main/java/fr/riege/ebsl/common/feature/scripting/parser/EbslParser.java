/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.feature.scripting.parser;

import fr.riege.ebsl.common.feature.scripting.blocks.EbslBlockStatementInvocation;
import fr.riege.ebsl.common.feature.scripting.blocks.EbslBlockStatementRegistry;
import fr.riege.ebsl.common.feature.scripting.blocks.EbslBlockStatementResult;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslCommandStatement;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;

import java.util.*;

public final class EbslParser {
    private final List<String> tokens;
    private final Map<String, List<EbslStatement>> functions = new HashMap<>();
    private int index;

    public EbslParser(List<String> tokens) {
        this.tokens = tokens;
    }

    public EbslProgram parse() {
        return new EbslProgram(parseBlock(false), functions);
    }

    private List<EbslStatement> parseBlock(boolean nested) {
        List<EbslStatement> statements = new ArrayList<>();
        boolean closed = false;
        while (!eof() && !closed) {
            skipTerminators();
            if (!eof() && peek(EbslSyntax.BLOCK_CLOSE)) {
                closeBlock(nested);
                closed = true;
            } else if (!eof()) {
                parseStatement(statements);
            }
        }
        return List.copyOf(statements);
    }

    private void parseStatement(List<EbslStatement> statements) {
        String command = normalize(next());
        List<String> args = readArgs();
        if (!peek(EbslSyntax.BLOCK_OPEN)) {
            statements.add(new EbslCommandStatement(command, args));
            return;
        }
        index++;
        List<EbslStatement> body = parseBlock(true);
        addBlockStatement(statements, command, args, body);
    }

    private void addBlockStatement(List<EbslStatement> statements, String command, List<String> args, List<EbslStatement> body) {
        EbslBlockStatementResult result = EbslBlockStatementRegistry.parse(new EbslBlockStatementInvocation(command, args, body, this));
        if (result.handled()) {
            result.optionalStatement().ifPresent(statements::add);
            return;
        }
        statements.add(new EbslCommandStatement(command, args));
        statements.addAll(body);
    }

    public List<EbslStatement> readOptionalElse() {
        skipTerminators();
        if (!peek("else")) {
            return List.of();
        }
        index++;
        if (!peek(EbslSyntax.BLOCK_OPEN)) {
            throw error("Expected '{' after else");
        }
        index++;
        return parseBlock(true);
    }

    public void defineFunction(String name, List<EbslStatement> body) {
        functions.put(name.toLowerCase(Locale.ROOT), body);
    }

    private List<String> readArgs() {
        List<String> args = new ArrayList<>();
        while (!eof()
            && !peek(EbslSyntax.LINE_END)
            && !peek(EbslSyntax.STATEMENT_END)
            && !peek(EbslSyntax.BLOCK_OPEN)
            && !peek(EbslSyntax.BLOCK_CLOSE)) {
            args.add(next());
        }
        skipTerminators();
        return args;
    }

    private void closeBlock(boolean nested) {
        if (!nested) {
            throw error("Unexpected '}'");
        }
        index++;
    }

    private void skipTerminators() {
        while (!eof() && (peek(EbslSyntax.LINE_END) || peek(EbslSyntax.STATEMENT_END))) {
            index++;
        }
    }

    private boolean peek(String token) {
        return !eof() && tokens.get(index).equals(token);
    }

    private String next() {
        if (eof()) {
            throw error("Unexpected end of script");
        }
        return tokens.get(index++);
    }

    private boolean eof() {
        return index >= tokens.size();
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " near token " + index);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
