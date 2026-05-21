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

package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.Objects;

public record EbslGraphPort(
    String id,
    String label,
    EbslGraphPortDirection direction,
    EbslGraphPortKind kind,
    boolean multiple
) {
    public EbslGraphPort {
        id = id == null || id.isBlank() ? EbslGraphConnection.DEFAULT_FLOW_PORT : id.trim();
        label = label == null || label.isBlank() ? id : label.trim();
        Objects.requireNonNull(direction, "direction");
        kind = kind == null ? EbslGraphPortKind.FLOW : kind;
    }

    public static EbslGraphPort input(String id, String label) {
        return new EbslGraphPort(id, label, EbslGraphPortDirection.INPUT, EbslGraphPortKind.FLOW, false);
    }

    public static EbslGraphPort output(String id, String label) {
        return new EbslGraphPort(id, label, EbslGraphPortDirection.OUTPUT, EbslGraphPortKind.FLOW, true);
    }
}
