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

public final class EbslPlatformExtensionPoints {
    public static final EbslExtensionPoint<EbslRuntimeContribution> RUNTIME =
        EbslExtensionPoint.of("ebsl.runtime", EbslRuntimeContribution.class);
    public static final EbslExtensionPoint<EbslUiContribution> UI =
        EbslExtensionPoint.of("ebsl.ui", EbslUiContribution.class);
    public static final EbslExtensionPoint<EbslScriptingContribution> SCRIPTING =
        EbslExtensionPoint.of("ebsl.scripting", EbslScriptingContribution.class);
    public static final EbslExtensionPoint<EbslEntityBrainContribution> ENTITY_BRAIN =
        EbslExtensionPoint.of("ebsl.entity_brain", EbslEntityBrainContribution.class);
    public static final EbslExtensionPoint<EbslPathfinderRegressionContribution> PATHFINDER_REGRESSION =
        EbslExtensionPoint.of("ebsl.pathfinder_regression", EbslPathfinderRegressionContribution.class);

    private EbslPlatformExtensionPoints() {
    }
}
