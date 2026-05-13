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
package fr.riege.ebsl.common.pathfinding.quality;

/**
 * Scores one dimension of path quality.
 *
 * <p>Metrics produce named contributions that are aggregated into a quality report for planning and diagnostics.</p>
 */
public interface PathQualityMetric {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    PathQualityContribution evaluate(PathQualityContext context);
}
