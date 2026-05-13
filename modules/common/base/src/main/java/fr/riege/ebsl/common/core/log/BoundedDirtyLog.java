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

package fr.riege.ebsl.common.core.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class BoundedDirtyLog<T> {
    private final int maxEntries;
    private final Deque<T> entries = new ArrayDeque<>();
    private volatile boolean dirty;

    public BoundedDirtyLog(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
    }

    public void add(T entry) {
        synchronized (entries) {
            if (entries.size() >= maxEntries) {
                entries.pollFirst();
            }
            entries.addLast(entry);
        }
        dirty = true;
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
        dirty = true;
    }

    public List<T> snapshot() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }
}
