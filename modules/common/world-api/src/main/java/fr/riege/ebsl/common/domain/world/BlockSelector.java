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

package fr.riege.ebsl.common.domain.world;

import java.util.Locale;

public final class BlockSelector {
    private final Expression expression;

    private BlockSelector(Expression expression) {
        this.expression = expression;
    }

    public static BlockSelector parse(String selector) {
        return new BlockSelector(new Parser(selector).parse());
    }

    public boolean matches(BlockId id) {
        return id != null && expression.matches(id);
    }

    /**
     * Evaluates one parsed block selector expression.

     *

     * <p>Expressions compose predicates for block identifiers without exposing parser internals.</p>

     */
    private interface Expression {
        /**
         * Returns whether the supplied value satisfies this predicate.
 *
         * @param id the block or entity identifier
         * @return true when the condition is satisfied; false otherwise
         */
        boolean matches(BlockId id);
    }

    private record Term(String token) implements Expression {
        @Override
        public boolean matches(BlockId id) {
            if (token.isBlank()) {
                return false;
            }
            String exact = id.toString().toLowerCase(Locale.ROOT);
            String normalized = normalize(token);
            if (exact.equals(normalized)) {
                return true;
            }
            if (!normalized.contains(":") && WorldRegistries.blockSelectors().matches(id, normalized)) {
                return true;
            }
            String path = id.path().toLowerCase(Locale.ROOT);
            return !normalized.contains(":") && (path.equals(normalized) || path.endsWith("_" + normalized));
        }
    }

    private record Not(Expression expression) implements Expression {
        @Override
        public boolean matches(BlockId id) {
            if (expression instanceof Term(String token) && token.isBlank()) {
                return false;
            }
            return !expression.matches(id);
        }
    }

    private record Binary(BlockSelectorOperator operator, Expression left, Expression right) implements Expression {
        @Override
        public boolean matches(BlockId id) {
            return switch (operator) {
                case OR -> left.matches(id) || right.matches(id);
                case AND -> left.matches(id) && right.matches(id);
                case NOT -> false;
            };
        }
    }

    private static final class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input == null ? "" : input;
        }

        private Expression parse() {
            Expression expression = parseOr();
            skipWhitespace();
            return expression;
        }

        private Expression parseOr() {
            Expression expression = parseAnd();
            while (match(BlockSelectorOperator.OR)) {
                expression = new Binary(BlockSelectorOperator.OR, expression, parseAnd());
            }
            return expression;
        }

        private Expression parseAnd() {
            Expression expression = parseUnary();
            while (match(BlockSelectorOperator.AND)) {
                expression = new Binary(BlockSelectorOperator.AND, expression, parseUnary());
            }
            return expression;
        }

        private Expression parseUnary() {
            if (match(BlockSelectorOperator.NOT)) {
                return new Not(parseUnary());
            }
            return parseTerm();
        }

        private Expression parseTerm() {
            skipWhitespace();
            int start = index;
            while (index < input.length() && !isOperator(input.charAt(index)) && !Character.isWhitespace(input.charAt(index))) {
                index++;
            }
            if (start == index) {
                index = Math.min(input.length(), index + 1);
                return new Term("");
            }
            return new Term(input.substring(start, index));
        }

        private boolean match(BlockSelectorOperator operator) {
            skipWhitespace();
            if (!input.startsWith(operator.token(), index)) {
                return false;
            }
            index += operator.token().length();
            return true;
        }

        private void skipWhitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        private static boolean isOperator(char value) {
            return BlockSelectorOperator.OR.token().charAt(0) == value
                || BlockSelectorOperator.AND.token().charAt(0) == value
                || BlockSelectorOperator.NOT.token().charAt(0) == value;
        }
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('\\', '/').replace('-', '_');
    }
}
