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
package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Map;

/**
 * Executes a goal request assembled from UI-provided parameters.
 *
 * <p>Implementations translate validated integer inputs into navigation service calls and return command-style status codes.</p>
 */
@FunctionalInterface
public interface GoalUiExecutor {
    /**
     * Executes the operation represented by this contract.
 *
     * @param navigation the navigation service used by the component
     * @param values the validated parameter values
     * @return the value defined by this contract
     */
    int execute(NavigationService navigation, Map<String, Integer> values);
}
