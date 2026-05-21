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

package fr.riege.ebsl.common.feature.scripting.brain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EntityBrainMemory {
    private final Map<String, Object> values = new HashMap<>();

    public Object get(String key) {
        return values.get(key);
    }

    public Object getOrDefault(String key, Object fallback) {
        return values.getOrDefault(key, fallback);
    }

    public void set(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(values));
    }
}
