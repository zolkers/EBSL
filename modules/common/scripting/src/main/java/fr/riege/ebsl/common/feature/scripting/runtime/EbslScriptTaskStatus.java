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

package fr.riege.ebsl.common.feature.scripting.runtime;

enum EbslScriptTaskStatus {
    IDLE("idle"),
    RUNNING("running"),
    LOADED("loaded"),
    STOPPED("stopped"),
    NO_SCRIPT_MATCHED("no script matched"),
    SUMMARY("");

    private final String label;

    EbslScriptTaskStatus(String label) {
        this.label = label;
    }

    String message() {
        return label;
    }

    String message(String detail) {
        if (this == SUMMARY) {
            return detail == null ? "" : detail;
        }
        return detail == null || detail.isBlank() ? label : label + " " + detail;
    }
}
