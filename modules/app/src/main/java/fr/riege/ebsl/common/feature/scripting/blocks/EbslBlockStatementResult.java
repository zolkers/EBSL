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

import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.Optional;

public record EbslBlockStatementResult(boolean handled, EbslStatement statement) {
    static final EbslBlockStatementResult UNHANDLED = new EbslBlockStatementResult(false, null);

    static EbslBlockStatementResult handledWithoutStatement() {
        return new EbslBlockStatementResult(true, null);
    }

    static EbslBlockStatementResult statement(EbslStatement statement) {
        return new EbslBlockStatementResult(true, statement);
    }

    public Optional<EbslStatement> optionalStatement() {
        return Optional.ofNullable(statement);
    }
}
