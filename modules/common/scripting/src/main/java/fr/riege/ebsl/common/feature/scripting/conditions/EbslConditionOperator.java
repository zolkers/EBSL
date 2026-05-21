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

package fr.riege.ebsl.common.feature.scripting.conditions;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
/**
 * Evaluates one binary condition operator for the EBSL runtime.
 *
 * <p>Operators receive already resolved operands and must return a deterministic boolean for the current runtime snapshot.</p>
 */
@FunctionalInterface
public interface EbslConditionOperator {
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param runtime the active script runtime
     * @param left the left operand
     * @param right the right operand
     * @return true when the condition is satisfied; false otherwise
     */
    boolean evaluate(EbslScriptRuntime runtime, Object left, Object right);
}
