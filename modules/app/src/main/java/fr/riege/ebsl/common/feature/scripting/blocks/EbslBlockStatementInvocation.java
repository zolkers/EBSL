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
package fr.riege.ebsl.common.feature.scripting.blocks;

import fr.riege.ebsl.common.feature.scripting.parser.EbslParser;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.List;

public record EbslBlockStatementInvocation(
    String command,
    List<String> args,
    List<EbslStatement> body,
    EbslParser parser
) {
    public EbslBlockStatementInvocation {
        args = List.copyOf(args);
        body = List.copyOf(body);
    }

    void defineFunction() {
        if (!args.isEmpty()) {
            parser.defineFunction(args.get(0), body);
        }
    }

    List<EbslStatement> readOptionalElse() {
        return parser.readOptionalElse();
    }
}
