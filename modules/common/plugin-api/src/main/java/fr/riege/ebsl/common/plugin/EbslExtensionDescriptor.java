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

package fr.riege.ebsl.common.plugin;

import java.util.Objects;

public record EbslExtensionDescriptor(String id, String displayName, String version, int order) {
    public EbslExtensionDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Extension id must not be blank.");
        }
        displayName = normalize(displayName, id);
        version = normalize(version, "dev");
    }

    public static EbslExtensionDescriptor of(String id, String displayName) {
        return new EbslExtensionDescriptor(id, displayName, "dev", 0);
    }

    private static String normalize(String value, String fallback) {
        return Objects.toString(value, fallback).isBlank() ? fallback : value;
    }
}
