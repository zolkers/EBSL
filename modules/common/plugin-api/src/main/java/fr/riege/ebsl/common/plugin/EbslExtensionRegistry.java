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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EbslExtensionRegistry {
    private final Map<String, List<EbslContribution<?>>> contributions = new LinkedHashMap<>();

    public <T> void register(EbslExtensionDescriptor owner, EbslExtensionPoint<T> point, T value) {
        EbslContribution<T> contribution = new EbslContribution<>(owner, point, value);
        contributions.computeIfAbsent(point.id(), ignored -> new ArrayList<>()).add(contribution);
        contributions.get(point.id()).sort(Comparator.comparingInt(item -> item.owner().order()));
    }

    public <T> List<T> contributions(EbslExtensionPoint<T> point) {
        Objects.requireNonNull(point, "point");
        return contributions.getOrDefault(point.id(), List.of()).stream()
            .map(EbslContribution::value)
            .map(point.contributionType()::cast)
            .toList();
    }

    public boolean isEmpty() {
        return contributions.isEmpty();
    }

    public int size(EbslExtensionPoint<?> point) {
        Objects.requireNonNull(point, "point");
        return contributions.getOrDefault(point.id(), List.of()).size();
    }
}
