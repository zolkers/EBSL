package fr.riege.ebsl.common.pathfinding;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

record LongRangeQueueRequest(boolean startFromPlayer, PathPosition horizonStart) {
    static LongRangeQueueRequest fromPlayer() {
        return new LongRangeQueueRequest(true, null);
    }

    static LongRangeQueueRequest fromSegmentHorizon(PathPosition horizonStart) {
        return new LongRangeQueueRequest(false, horizonStart);
    }
}
