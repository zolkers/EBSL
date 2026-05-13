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
package fr.riege.ebsl.common.pathfinding.check;

/**
 * Evaluates one runtime path health check.
 *
 * <p>Checks inspect execution context and return structured actions that keep recovery behavior explicit.</p>
 */
interface PathCheck {
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    PathCheckResult evaluate(PathCheckContext context);
}
