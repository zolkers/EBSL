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

public record PathCheckResult(PathCheckAction action, int cutoffSegmentIndex, String reason) {
    private static final PathCheckResult NONE = new PathCheckResult(PathCheckAction.NONE, -1, "");

    static PathCheckResult none() {
        return NONE;
    }

    static PathCheckResult cutoff(int cutoffSegmentIndex, String reason) {
        return new PathCheckResult(PathCheckAction.CUTOFF, cutoffSegmentIndex, reason);
    }

    static PathCheckResult repairToSegment(int cutoffSegmentIndex, String reason) {
        return new PathCheckResult(PathCheckAction.REPAIR_TO_SEGMENT, cutoffSegmentIndex, reason);
    }

    static PathCheckResult forceReplan(String reason) {
        return new PathCheckResult(PathCheckAction.FORCE_REPLAN, -1, reason);
    }

    public boolean requiresAction() {
        return action != PathCheckAction.NONE;
    }

    public boolean isCutoff() {
        return action == PathCheckAction.CUTOFF;
    }

    public boolean isRepairToSegment() {
        return action == PathCheckAction.REPAIR_TO_SEGMENT;
    }

    public boolean isForceReplan() {
        return action == PathCheckAction.FORCE_REPLAN;
    }
}
