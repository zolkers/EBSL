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

package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.core.registry.EnumRegistry;

@SuppressWarnings("java:S107")
public enum EbslSyntaxPalette {
    DARK(
        0xFF7DD3FC,
        0xFFC084FC,
        0xFF86EFAC,
        0xFFFBBF24,
        0xFFF472B6,
        0xFFA7F3D0,
        0xFFFCA5A5,
        0xFFFDBA74,
        0xFF93C5FD,
        0xFF64748B,
        0xFFE5E7EB,
        0x00000000
    ),
    LIGHT(
        0xFF0369A1,
        0xFF7E22CE,
        0xFF15803D,
        0xFFB45309,
        0xFFBE185D,
        0xFF047857,
        0xFFDC2626,
        0xFFEA580C,
        0xFF2563EB,
        0xFF64748B,
        0xFF111827,
        0x00000000
    ),
    HIGH_CONTRAST(
        0xFF00D9FF,
        0xFFFF66FF,
        0xFF00FF66,
        0xFFFFFF00,
        0xFFFF66B3,
        0xFF00FFCC,
        0xFFFF7777,
        0xFFFFAA00,
        0xFF66A3FF,
        0xFFA3A3A3,
        0xFFFFFFFF,
        0x00000000
    );

    private final EnumRegistry<EbslTokenKind, EbslTokenStyle> styles =
        new EnumRegistry<>(EbslTokenKind.class, new EbslTokenStyle(0xFFE6EDF3));

    EbslSyntaxPalette(
        int command,
        int control,
        int sensor,
        int operator,
        int variable,
        int string,
        int number,
        int duration,
        int block,
        int comment,
        int identifier,
        int whitespace
    ) {
        register(EbslTokenKind.COMMAND, command);
        register(EbslTokenKind.CONTROL, control);
        register(EbslTokenKind.SENSOR, sensor);
        register(EbslTokenKind.OPERATOR, operator);
        register(EbslTokenKind.VARIABLE, variable);
        register(EbslTokenKind.STRING, string);
        register(EbslTokenKind.NUMBER, number);
        register(EbslTokenKind.DURATION, duration);
        register(EbslTokenKind.BLOCK, block);
        register(EbslTokenKind.COMMENT, comment);
        register(EbslTokenKind.IDENTIFIER, identifier);
        register(EbslTokenKind.WHITESPACE, whitespace);
    }

    public EbslTokenStyle style(EbslTokenKind kind) {
        return styles.get(kind);
    }

    private void register(EbslTokenKind kind, int color) {
        styles.register(kind, new EbslTokenStyle(color));
    }
}
