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

public final class EbslSyntax {
    public static final String COMMENT = "#";
    public static final String QUOTE = "\"";
    public static final String STATEMENT_END = ";";
    public static final String BLOCK_OPEN = "{";
    public static final String BLOCK_CLOSE = "}";
    public static final String LINE_END = "\n";
    public static final String VARIABLE_PREFIX = "$";

    private EbslSyntax() {
    }
}
