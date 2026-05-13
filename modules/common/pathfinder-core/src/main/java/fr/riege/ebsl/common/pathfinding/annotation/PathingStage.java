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
package fr.riege.ebsl.common.pathfinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the pathfinding stage represented by a component.
 *
 * <p>Stage metadata helps diagnostics and code review connect behavior to planning, validation, execution, or recovery phases.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PathingStage {
    /**
     * Returns the primary metadata value declared by this annotation.
 *
     * @return the value defined by this contract
     */
    Stage value();

    enum Stage {
        GRAPH_EVALUATION,
        PATH_POST_PROCESSING,
        PATH_SMOOTHING,
        RESULT_CLASSIFICATION,
        STATE_PERSISTENCE,
        EXECUTION,
        RECOVERY,
        NAVIGATION_SERVICE
    }
}
