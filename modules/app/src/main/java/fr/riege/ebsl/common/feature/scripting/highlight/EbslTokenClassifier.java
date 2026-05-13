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

package fr.riege.ebsl.common.feature.scripting.highlight;

/**
 * Classifies lexical tokens for script editor highlighting.
 *
 * <p>Classifiers are intentionally small and side-effect free so multiple token rules can be composed in priority order.</p>
 */
@FunctionalInterface
public interface EbslTokenClassifier {
    /**
     * Classifies the supplied movement context into the movement type used by planning, quality, and execution.
 *
     * @param token the token value
     * @param firstToken the first token value
     * @return the value defined by this contract
     */
    EbslTokenKind classify(String token, boolean firstToken);
}
