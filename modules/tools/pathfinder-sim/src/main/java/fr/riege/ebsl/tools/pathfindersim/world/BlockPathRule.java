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

package fr.riege.ebsl.tools.pathfindersim.world;

import fr.riege.ebsl.common.domain.world.BlockId;

import java.util.Arrays;
import java.util.List;

public final class BlockPathRule {
    private final MatchMode mode;
    private final List<String> values;

    private BlockPathRule(MatchMode mode, String... values) {
        this.mode = mode;
        this.values = List.copyOf(Arrays.asList(values));
    }

    public static BlockPathRule exact(String... values) {
        return new BlockPathRule(MatchMode.EXACT, values);
    }

    public static BlockPathRule suffix(String... values) {
        return new BlockPathRule(MatchMode.SUFFIX, values);
    }

    public static BlockPathRule exactOrSuffix(String... values) {
        return new BlockPathRule(MatchMode.EXACT_OR_SUFFIX, values);
    }

    public boolean matches(BlockId id) {
        return id != null && matches(id.path());
    }

    public boolean matches(String path) {
        for (String value : values) {
            if (mode.matches(path, value)) {
                return true;
            }
        }
        return false;
    }

    private enum MatchMode {
        EXACT {
            @Override
            boolean matches(String actual, String expected) {
                return actual.equals(expected);
            }
        },
        SUFFIX {
            @Override
            boolean matches(String actual, String expected) {
                return actual.endsWith(expected);
            }
        },
        EXACT_OR_SUFFIX {
            @Override
            boolean matches(String actual, String expected) {
                return actual.equals(expected) || actual.endsWith("_" + expected);
            }
        };

        abstract boolean matches(String actual, String expected);
    }
}
