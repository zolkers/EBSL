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

package fr.riege.ebsl.common.domain.analytics;

import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.platform.service.NavigationService;

public record AnalyticsSnapshot(
    NavigationStatus navigationState,
    String selectedModule,
    int jumpHeight,
    boolean visualizerEnabled
) {
    public static AnalyticsSnapshot capture(NavigationService nav, PathfinderModule selectedModule) {
        return new AnalyticsSnapshot(
            nav.pathStatus(),
            selectedModule != null ? selectedModule.displayName() : "none",
            0,
            false);
    }
}
